package ua.sushi_master.test;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.ErrorsCollector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import static com.codeborne.selenide.CollectionCondition.texts;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.url;


public class SmUaLoginTest {

    //    public void setup(){
    //        Configuration.browser="ie";
    //    }


    @Before
    public void beforeTestOpenSite(){
        Configuration.browser = "chrome";
        //Configuration.downloadsFolder = "";
        open("https://sushi-master.ua/");
    }


    @Test
      public void visibleCitySelection(){
        $(".city-accept-block-").shouldBe(visible);
    }


//    @Test
//    public void userCanPressLoginButton() {
//        open("https://sushi-master.ua/");
//        $(".backdrop-close").click();//Закрыть "Хочешь получать...)
//        $(".header-enter").$(".header-enter__login").click();//Нажать кнопку вход
//
//
//    }
//
//    @Test
//    public void userSelectCityOnEnter() {
//        open("https://sushi-master.ua/");
//        $(".backdrop-close").click();//Закрыть "Хочешь получать...)
//
//        $(byText("Так")).click();
//
//        if ($(".langsite").has(text("UA"))) {
//            $(".header-info__city-name").shouldHave(text("Київ"));
//        }
//
//        if ($(".langsite").has(text("RU"))) {
//            $(".header-info__city-name").shouldHave(text("Киев"));
//        }
//
//        //url().contains("kyiv.");
//
//    }

}