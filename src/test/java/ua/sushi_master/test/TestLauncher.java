package ua.sushi_master.test;

import com.codeborne.selenide.ex.ElementNotFound;
import com.gurock.testrail.APIClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.HashMap;
import java.util.Map;


//         Case status_id
//        1	Passed
//        2	Blocked
//        3	Untested (not allowed when adding a result)
//        4	Retest
//        5	Failed

public class TestLauncher {
    public static void main(String[] args) throws Exception {

        //Авторизация в TestRail
        APIClient client = new APIClient("https://x100.testrail.io/");
        client.setUser("antivariantum@gmail.com");
        client.setPassword("keL.U84mhUT7TBpAto6K");

        //Код проекта можно передать параметром и чаще всего так и будет
        String project_id;

        if (args.length>0){
            project_id = (String)args[0]; //для универсальности тут можно сделать массив проектов, который будет заполняться из аргументов
        }
        else {
            project_id = "2"; //Пока только сайт Украины, тут можно сделать, если параметров нет, то из всех открытых проектов (сделать признак, что это Web-проект) и заполнить массив проектов
        }
        //И отсюда начать перебирать все проекты из массива

            //Все незавершенные Runs
            for (Object run : (JSONArray) client.sendGet("get_runs/" + project_id + "&is_completed=0")) {
                String run_id = ((JSONObject) run).get("id").toString();
                //Runs-Tests (только Untested, Retest и Fail)
                for (Object test : (JSONArray) client.sendGet("get_tests/" + run_id + "&status_id=" + TestRailConst.STATUS_UNTESTED + "," + TestRailConst.STATUS_RETEST + "," + TestRailConst.STATUS_FAILED)) {
                    //Определяем Case
                    String case_id = ((JSONObject) test).get("case_id").toString();
                    String test_id = ((JSONObject) test).get("id").toString();

                    //А из Case имя класса с тестом этого Case'a
                    JSONObject case_obj = (JSONObject) client.sendGet("get_case/" + case_id);


                    String test_class_name = null; //Название класса в поле кейса
                    try {
                        test_class_name = case_obj.get("custom_test_class_name").toString();
                    } catch (Exception e) {
                        //e.printStackTrace(); //TODO Автотесты без классов выводить в задачи Jira (написать автотест)
                        continue;
                    }

                    System.out.println("Run " + run_id + " Case " + case_id + " test " + test_id + " TestClass " + "ua.sushi_master.test." + test_class_name);

//                    if (!case_obj.get("custom_automation_type").toString().contentEquals(TestRailConst.TEST_TYPE_AUTOMATED))
//                        continue; //Только автотесты.


                    //Заглушка для тестирования движка на одном тесте
                    if (!test_class_name.equals("SmUaLoginTest")) continue;

                    //Пробуем получить класс теста из кейса
                    try {
                        Class<?> testClass = Class.forName("ua.sushi_master.test." + test_class_name);
                        //Выполняем тест (все тесты внутри класса)
                        Result result = JUnitCore.runClasses(testClass);

                        //Записываем результаты в кейс
                        Map<String, String> data = new HashMap<>();
                        if (result.getFailures().size() == 0) {
                            data.put("status_id", TestRailConst.STATUS_PASSED); //Тест прошел
                            client.sendPost("add_result_for_case/" + run_id + "/" + case_id, data);
                        } else { //Тест не прошел
                            for (Failure failure : result.getFailures()) {
                                //Status
                                data.put("status_id", TestRailConst.STATUS_FAILED);

                                //Screenshot (добавляю в run, потому что для его добавления в result нужно знать res_id, для этого нужно записать result, а после записи result я уже не могу его изменить для встраивания)
                                String screenshot_local_path = failure.getMessage().replaceAll("(.*\r\n)*(Screenshot: file:/)(.*png)(\r\n.*)*","$3").replace("/","\\\\");
                                JSONObject screenshot_tr = (JSONObject) client.sendPost("add_attachment_to_run/" + run_id, screenshot_local_path);
                                String screenshot_id_tr = screenshot_tr.get("attachment_id").toString();

                                //Comment
                                String comment_fail = failure.getMessage().replaceAll("(.*\r\n.*)(\r\n.*)*","$1");
                                String screenshot_in_comment = "![](index.php?/attachments/get/" + screenshot_id_tr + ")";
                                //Если отсюда буду пушить в Jira, то в коммент можно не писать, но в Jira нужно запушить
                                String preconditions =  case_obj.get("custom_preconds").toString();
                                String steps =  case_obj.get("custom_steps").toString();
                                String expected = case_obj.get("custom_expected").toString();
                                //Version
                                String milestone_id = ((JSONObject)run).get("milestone_id").toString(); //проверить на null
                                Object milestone_obj = client.sendGet("get_milestone/" + milestone_id);
                                String milestone = ((JSONObject)milestone_obj).get("name").toString();

                                data.put("comment",
                                        //"Test " + ((JSONObject) test).get("id") + " Case " + case_id + "\r\n" +
                                        "Version and build: " + milestone + "\r\n" +
                                        "Preconditions: " + preconditions + "\r\n" +
                                        "Steps to reproduce: " + steps + "\r\n" +
                                        "Expected result: " + expected + "\r\n" +
                                        "Actual result: " +  comment_fail + "\r\n" + screenshot_in_comment);

                                //Save
                                client.sendPost("add_result_for_case/" + run_id + "/" + case_id, data);

                            }

                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Class not found " + e.getMessage());
                        //TODO Если класс не найден, записать это в задачу в Jira (ошибка в автотесте)
                    }


                }


            }
    }
}