package com.amazon.tests;

import com.amazon.tests.base.BaseTest;
import com.amazon.tests.pages.AmazonPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * AmazonSearchTests
 *
 * Test Case 1: Search for iPhone, add to cart, print price.
 * Test Case 2: Search for Galaxy, add to cart, print price.
 *
 * Both tests run in PARALLEL via testng.xml configuration.
 * ThreadLocal WebDriver in BaseTest ensures thread safety.
 */
public class AmazonSearchTest extends BaseTest {

    /**
     * Test Case 1 — iPhone
     * 1. Navigate to Amazon.com
     * 2. Search for "iPhone"
     * 3. Click the first result
     * 4. Capture and print the price
     * 5. Add the product to the cart
     */
    
    @Test(description = "TC1: Search for iPhone, add to cart/list, print price")
    public void testIPhoneSearch() {
        AmazonPage amazonPage = new AmazonPage(getDriver());
        amazonPage.navigateToAmazon();
        amazonPage.searchFor("Apple iPhone official");
        amazonPage.clickFirstProduct();

        String price = amazonPage.getProductPrice();
        System.out.println("  [iPhone] Device Price: " + price);

        amazonPage.addToCart(); // will fallback to Add to List if cart is missing

        Assert.assertNotNull(price, "Price should not be null");
        Assert.assertFalse(price.isEmpty(), "Price should not be empty");
    }


    /**
     * Test Case 2 — Samsung Galaxy
     * 1. Navigate to Amazon.com
     * 2. Search for "Samsung Galaxy"
     * 3. Click the first result
     * 4. Capture and print the price
     * 5. Add the product to the cart
     */
    @Test(description = "TC2: Search for Galaxy device, add to cart, print price")
    public void testGalaxySearch() {
        System.out.println("\n======================================");
        System.out.println("  TEST CASE 2 — Samsung Galaxy");
        System.out.println("  Thread ID: " + Thread.currentThread().getId());
        System.out.println("======================================");

        AmazonPage amazonPage = new AmazonPage(getDriver());

        // Step 1: Navigate to Amazon
        amazonPage.navigateToAmazon();

        // Step 2: Search for Galaxy
        amazonPage.searchFor("Samsung Galaxy phone");

        // Step 3: Open the first product
        amazonPage.clickFirstProduct();

        // Step 4: Get and print the price to console
        String price = amazonPage.getProductPrice();
        System.out.println("--------------------------------------");
        System.out.println("  [Galaxy] Device Price: " + price);
        System.out.println("--------------------------------------");

        // Step 5: Add to cart
        amazonPage.addToCart();

        // Assertion: Ensure a price was retrieved
        Assert.assertNotNull(price, "Price should not be null");
        Assert.assertFalse(price.isEmpty(), "Price should not be empty");
    }
}