
package com.softwaretesting;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AutoSuggestSearchTest {

    WebDriver driver;
    WebDriverWait wait;

    By searchBox = By.id("searchBox");
    By suggestions = By.cssSelector("#suggestions li");
    By dropdown = By.id("suggestions");
    By result = By.id("result");

    @BeforeMethod
    public void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        URL page = getClass().getClassLoader()
                .getResource("autosuggest.html");

        Assert.assertNotNull(page,
                "autosuggest.html is missing from src/test/resources");

        driver.get(Path.of(page.toURI()).toUri().toString());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void verifySearchBoxIsVisible() {
        WebElement input = driver.findElement(searchBox);

        Assert.assertTrue(input.isDisplayed());
        Assert.assertTrue(input.isEnabled());
    }

    @Test
    public void verifySuggestionsAppear() {
        driver.findElement(searchBox).sendKeys("selenium");

        wait.until(ExpectedConditions
                .visibilityOfElementLocated(suggestions));

        Assert.assertTrue(driver.findElements(suggestions).size() > 0);
    }

    @Test
    public void verifyPartialTextSuggestions() {
        driver.findElement(searchBox).sendKeys("sele");

        wait.until(ExpectedConditions
                .visibilityOfElementLocated(suggestions));

        List<WebElement> options = driver.findElements(suggestions);

        Assert.assertTrue(options.size() > 0);

        for (WebElement option : options) {
            Assert.assertTrue(option.getText()
                    .toLowerCase(Locale.ROOT).contains("sele"));
        }
    }

    @Test
    public void verifyMouseSelection() {
        driver.findElement(searchBox).sendKeys("selenium");

        By option = By.xpath(
                "//ul[@id='suggestions']/li"
                + "[normalize-space()='Selenium WebDriver']");

        wait.until(ExpectedConditions
                .elementToBeClickable(option)).click();

        Assert.assertEquals(
                driver.findElement(searchBox).getAttribute("value"),
                "Selenium WebDriver");
    }

    @Test
    public void verifyKeyboardSelection() {
        WebElement input = driver.findElement(searchBox);
        input.sendKeys("selenium");

        wait.until(ExpectedConditions
                .visibilityOfElementLocated(suggestions));

        input.sendKeys(Keys.ARROW_DOWN);
        input.sendKeys(Keys.ENTER);

        Assert.assertEquals(input.getAttribute("value"), "Selenium");
    }

    @Test
    public void verifyUppercaseQuery() {
        driver.findElement(searchBox).sendKeys("SELENIUM");

        wait.until(ExpectedConditions
                .visibilityOfElementLocated(suggestions));

        List<WebElement> options = driver.findElements(suggestions);

        Assert.assertTrue(options.size() > 0);

        for (WebElement option : options) {
            Assert.assertTrue(option.getText()
                    .toLowerCase(Locale.ROOT).contains("selenium"));
        }
    }

    @Test
    public void verifyEmptyInput() {
        driver.findElement(searchBox).clear();

        Assert.assertTrue(driver.findElements(suggestions).isEmpty());

        Assert.assertEquals(
                driver.findElement(dropdown).getCssValue("display"),
                "none");
    }

    @Test
    public void verifyUnmatchedQuery() {
        driver.findElement(searchBox).sendKeys("xyz123nomatch");

        Assert.assertTrue(driver.findElements(suggestions).isEmpty());

        Assert.assertEquals(
                driver.findElement(dropdown).getCssValue("display"),
                "none");
    }

    @Test
    public void verifyDropdownClosesAfterSelection() {
        driver.findElement(searchBox).sendKeys("selenium");

        By option = By.xpath(
                "//ul[@id='suggestions']/li"
                + "[normalize-space()='Selenium testing']");

        wait.until(ExpectedConditions
                .elementToBeClickable(option)).click();

        Assert.assertEquals(
                driver.findElement(dropdown).getCssValue("display"),
                "none");

        Assert.assertEquals(
                driver.findElement(searchBox).getAttribute("value"),
                "Selenium testing");
    }
}

