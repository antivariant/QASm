package ua.sushi_master.test;

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
        APIClient client = new APIClient("https://antivariant.testrail.io/");
        client.setUser("antivariantum@gmail.com");
        client.setPassword("keL.U84mhUT7TBpAto6K");

        //TODO окружить все обращения к TestRail трай-кетчами
        //Перебираем все незавершенные Runs
        for (Object run : (JSONArray) client.sendGet("get_runs/2&is_completed=0")) {
            String run_id = ((JSONObject) run).get("id").toString();
            //Runs-Tests (только Untested, Retest и Fail)
            for (Object test : (JSONArray) client.sendGet("get_tests/" + run_id + "&status_id=3,4,5")) { //TODO перенести в константы, посмотреть класс com.gurock.testrail.APIClient
                //Определяем Case
                String case_id = ((JSONObject) test).get("case_id").toString();
                String test_id = ((JSONObject) test).get("id").toString();

                //А из Case имя класса с тестом этого Case'a
                JSONObject case_obj = (JSONObject) client.sendGet("get_case/" + case_id);
                String test_class_name = case_obj.get("custom_test_class_name").toString(); //Газвание класса в поле кейса

                System.out.println("Run " + run_id + " Case " + case_id + " test " + test_id + " TestClass " + "ua.sushi_master.test." + test_class_name);

                if (!case_obj.get("custom_automation_type").toString().contentEquals("2")) continue; //Только автотесты
                if (test_class_name == null) continue; //Для которых указан класс теста
                //TODO Автотесты без классов выводить в задачи Jira (написать автотест)

                //Пробуем получить класс теста из кейса
                try {
                    Class<?> testClass = Class.forName("ua.sushi_master.test." + test_class_name);
                    //Выполняем тест (все тесты внутри класса)
                    Result result = JUnitCore.runClasses(testClass);

                    //Записываем результаты в кейс
                    Map<String,String> data = new HashMap<String,String>();
                    if (result.getFailures().size() == 0) {
                        data.put("status_id", "1"); //Тест прошел
                        client.sendPost("add_result_for_case/" + run_id + "/" + case_id, data);
                    } else { //Тест не прошел
                        for (Failure failure : result.getFailures()) {
                            data.put("status_id", "5");
                            data.put("comment", "Test " + ((JSONObject) test).get("id") + " Case " + case_id + " Fails: " + failure.toString());
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