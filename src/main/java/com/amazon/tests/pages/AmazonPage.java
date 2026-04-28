package com.amazon.tests.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * AmazonPage - Page Object Model for Amazon.com
 * Encapsulates all interactions: search, select product, add to cart, fetch price.
 */
public class AmazonPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ---------- Locators ----------
    private final By searchBox        = By.id("twotabsearchtextbox");
    private final By searchButton     = By.id("nav-search-submit-button");
    private final By addToCartButton  = By.id("add-to-cart-button");
    private final By cartConfirmTitle = By.cssSelector("#NATC_SMART_WAGON_CONF_MSG_SUCCESS, #sw-atc-confirmation, .a-alert-success");

    // Product price selectors (Amazon uses several depending on page type)
    private final By priceWhole       = By.cssSelector("span.a-price-whole");
    private final By priceFraction    = By.cssSelector("span.a-price-fraction");
    private final By priceOffscreen   = By.cssSelector("span.a-offscreen");
    private final By corePriceBlock   = By.id("corePriceDisplay_desktop_feature_div");

    // Variant selectors (color, size, storage, etc.)
    private final By variantOptions   = By.cssSelector("div#variation_color_name li, div#variation_size_name li, div#variation_storage li");

    public AmazonPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /** Navigate to Amazon homepage */
    public void navigateToAmazon() {
        driver.get("https://www.amazon.com");
        System.out.println("[Thread " + Thread.currentThread().getId() + "] Navigated to Amazon.com");
    }

    /** Search for a product by keyword */
    public void searchFor(String keyword) {
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        driver.findElement(searchButton).click();
        System.out.println("[Thread " + Thread.currentThread().getId() + "] Searched for: " + keyword);
    }

    /** Clicks the first product on the search results page. */
    public void clickFirstProduct() {
        By firstResult = By.cssSelector(
            "div[data-component-type='s-search-result'] h2 a.a-link-normal, " +
            "div[data-component-type='s-search-result'] a.s-no-outline"
        );

        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(firstResult));
        String originalWindow = driver.getWindowHandle();

        firstProduct.click();

        // Switch to new tab if Amazon opened one
        if (driver.getWindowHandles().size() > 1) {
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
        }

        System.out.println("[Thread " + Thread.currentThread().getId() + "] Clicked first product. URL: " + driver.getCurrentUrl());
    }

    /** Selects the first variant if product requires configuration (e.g., iPhone storage/color). */
    public void selectFirstVariantIfPresent() {
        try {
            List<WebElement> variants = driver.findElements(variantOptions);
            if (!variants.isEmpty()) {
                WebElement firstVariant = wait.until(ExpectedConditions.elementToBeClickable(variants.get(0)));
                firstVariant.click();
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Selected first variant option.");
            }
        } catch (Exception ignored) {
            System.out.println("[Thread " + Thread.currentThread().getId() + "] No variant selection required.");
        }
    }

    /** Extracts the product price from the product detail page. */
    public String getProductPrice() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(priceOffscreen));
            for (WebElement el : driver.findElements(priceOffscreen)) {
                String text = el.getText().trim();
                if (!text.isEmpty() && text.startsWith("$")) {
                    return text;
                }
            }
        } catch (TimeoutException ignored) {}

        try {
            WebElement whole = driver.findElement(priceWhole);
            String wholeText = whole.getText().replace(",", "").trim();
            String fractionText = "00";
            try {
                fractionText = driver.findElement(priceFraction).getText().trim();
            } catch (NoSuchElementException ignored) {}
            // Ensure proper formatting
            if (!fractionText.matches("\\d{2}")) {
                fractionText = "00";
            }
            return "$" + wholeText + "." + fractionText;
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement coreBlock = driver.findElement(corePriceBlock);
            return coreBlock.getText().lines()
                    .filter(line -> line.startsWith("$"))
                    .findFirst()
                    .orElse("Price not found");
        } catch (NoSuchElementException ignored) {}

        return "Price not found";
    }

    /** Adds the current product to the cart, handling variants if needed. */
    public void addToCart() {
        // Ensure variant is selected if required
        selectFirstVariantIfPresent();
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        // Check if Add to Cart exists
        List<WebElement> addButtons = driver.findElements(addToCartButton);
        if (addButtons.isEmpty()) {
            System.out.println("[Thread " + Thread.currentThread().getId() + "] Add to Cart not available. Using Add to List instead.");

            // Try Add to List as fallback
            try {
                WebElement addToListBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("add-to-wishlist-button-submit")
                ));
                addToListBtn.click();
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Clicked 'Add to List'.");
            } catch (Exception e) {
                System.out.println("[Thread " + Thread.currentThread().getId() + "] Neither Add to Cart nor Add to List available.");
            }
            return;
        }

        // Normal Add to Cart flow
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        addBtn.click();
        System.out.println("[Thread " + Thread.currentThread().getId() + "] Clicked 'Add to Cart'.");

        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(cartConfirmTitle),
            ExpectedConditions.urlContains("cart")
        ));
        System.out.println("[Thread " + Thread.currentThread().getId() + "] Product added to cart successfully.");
        
        try {
            Thread.sleep(3000); // 3 seconds pause
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}
