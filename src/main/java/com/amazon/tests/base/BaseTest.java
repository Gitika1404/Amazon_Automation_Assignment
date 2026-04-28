package com.amazon.tests.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * BaseTest - Sets up and tears down WebDriver for each test method.
 * Uses ThreadLocal to ensure thread-safety during parallel execution.
 */
public class BaseTest {

    // ThreadLocal ensures each parallel thread gets its own WebDriver instance
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Uncomment below line to run headless (no browser UI, useful for CI/CD)
        // options.addArguments("--headless=new");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // Disable automation detection banners
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        driverThreadLocal.set(driver);
        System.out.println("[Thread: " + Thread.currentThread().getId() + "] Browser launched.");
    }

    /**
     * Returns the WebDriver instance for the current thread.
     */
    public WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @AfterMethod
    public void tearDown() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            System.out.println("[Thread: " + Thread.currentThread().getId() + "] Browser closed.");
        }
    }
}