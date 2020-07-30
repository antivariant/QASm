package ua.sushi_master.test;

import org.junit.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class SmUaLoginTest {
    @Test
    public void userCanPressLoginButton() {
        open("https://sushi-master.ua/");
        $(".backdrop-close").click();//Закрыть "Хочешь получать...)
        $(".header-enter").$(".header-enter__login").click();//Нажать кнопку вход
    }

}