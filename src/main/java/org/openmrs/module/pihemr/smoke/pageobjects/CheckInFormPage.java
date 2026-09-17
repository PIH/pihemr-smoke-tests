package org.openmrs.module.pihemr.smoke.pageobjects;

import org.openmrs.module.pihemr.smoke.helper.SmokeTestProperties;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openqa.selenium.Keys.RETURN;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;

public class CheckInFormPage extends AbstractPageObject {

	private static final String CONFIRM_TEXT = "Konfime";
	private static final String DETAILS_TEXT = "Detay";
	private static final String TYPE_OF_VISIT_TEXT = "Tip de Vizit";

    public static final By SEARCH_FIELD = By.id("patient-search");

	public CheckInFormPage(WebDriver driver) {
		super(driver);
	}

    public void findPatientAndClickOnCheckIn(String patientIdentifier) throws Exception {
        findPatient(patientIdentifier);
        clickOnCheckIn();
    }

    public void enterInfo() {
        // TODO: potential fix below when we switch to the new type of visit selector in Mirebalais
        //clickOn(By.cssSelector(".section-container > :last-child"));
        //hitEnterKey();
        tabThroughDetailsToTypeOfVisit();
        selectThirdOptionFor("typeOfVisit");
        findInputInsideSpan("paymentAmount").sendKeys("100" + Keys.RETURN);
        findInputInsideSpan("receiptNumber").sendKeys("receipt #" + Keys.RETURN);
        selectNotToPrintWristbandIfQuestionPresent();
        clickConfirm();
        wait2minutes.until(invisibilityOfElementLocated(By.className("submitButton")));  // make sure the submit is complete
    }

    public void enterInfoWithMultipleEnterKeystrokesOnSubmit()  {
        tabThroughDetailsToTypeOfVisit();
        selectThirdOptionFor("typeOfVisit");
        findInputInsideSpan("paymentAmount").sendKeys("100" + Keys.RETURN);
        findInputInsideSpan("receiptNumber").sendKeys("receipt #" + Keys.RETURN);
        selectNotToPrintWristbandIfQuestionPresent();

        // repeatedly activate the submit button to verify rapid, repeated submission attempts
        // don't create duplicate encounters; sending synthetic RETURN keystrokes to the button
        // is not reliably picked up as an activation by current Chrome/ChromeDriver, so click it
        // instead (as elsewhere in this codebase, native clicks aren't always registered either,
        // hence the JS-executed click in clickOn()). Once the submission goes through, the page
        // navigates away and further clicks on the now-stale button element throw.
        WebElement confirmButton = driver.findElement(By.id("confirmationQuestion")).findElement(By.className("confirm"));
        try {
            for (int i = 0; i < 48; i++) {
                clickOn(confirmButton);
            }
        }
        catch (org.openqa.selenium.StaleElementReferenceException e) {
            // expected once the submission succeeds and the page navigates away
        }

        wait2minutes.until(invisibilityOfElementLocated(By.className("submitButton")));  // make sure the submit is complete
    }

	public void enterInfoFillingTheFormTwice() throws Exception {
        tabThroughDetailsToTypeOfVisit();
        selectThirdOptionFor("typeOfVisit");
        findInputInsideSpan("paymentAmount").sendKeys("100" + Keys.RETURN);
        findInputInsideSpan("receiptNumber").sendKeys("receipt #" + Keys.RETURN);
        selectNotToPrintWristbandIfQuestionPresent();
        clickOnNoButton();
        tabThroughDetailsToTypeOfVisit();
        selectSecondOptionFor("typeOfVisit");
        findInputInsideSpan("paymentAmount").sendKeys("100" + Keys.RETURN);
        findInputInsideSpan("receiptNumber").sendKeys("receipt #" + Keys.RETURN);
        selectNotToPrintWristbandIfQuestionPresent();
		clickConfirm();
        wait15seconds.until(invisibilityOfElementLocated(By.className("submitButton")));  // make sure the submit is complete
	}

	private void findPatient(String patientIdentifier) throws Exception {
		super.findPatientById(patientIdentifier, SEARCH_FIELD);
	}

	private void clickOnCheckIn() {
		clickOn(By.id("pih.checkin.registrationAction"));
	}

	private void clickConfirm() {
        clickUntil(driver.findElement(By.cssSelector("#confirmationQuestion .confirm")),
                ExpectedConditions.visibilityOfElementLocated(By.className("icon-spinner")),
                15);
	}

    private void confirmDataForScheduleAppointment() {
        clickOn(By.cssSelector("#confirmationQuestion .confirm"));
    }

	private void clickOnNoButton() {
		clickOnConfirmationTab();
		clickOn(By.cssSelector("#confirmationQuestion input.cancel"));
	}

	private void clickOnConfirmationTab() {
		clickOnBreadcrumbTab(CONFIRM_TEXT);
	}

	private void clickOnDetailsTab() {
		clickOnBreadcrumbTab(DETAILS_TEXT);
	}

	private void clickOnTypeOfVisitTab() {
		clickOnBreadcrumbTab(TYPE_OF_VISIT_TEXT);
	}

	private void clickOnBreadcrumbTab(String tabText) {
		List<WebElement> elements = driver.findElements(By.cssSelector("#formBreadcrumb span"));
		for (WebElement element : elements) {
	        if(element.getText().contains(tabText)) {
	        	element.click();
	        }
	    }
	}

	// the "Tcheke" wizard starts on its first step (Detay); tab through it explicitly before
	// interacting with Tip de Vizit rather than assuming it's the step already showing
	private void tabThroughDetailsToTypeOfVisit() {
		clickOnDetailsTab();
		clickOnTypeOfVisitTab();
	}

    // TODO: revert https://github.com/PIH/mirebalais-smoke-tests/commit/e9ab41b02f4c263362b3627cd9e9b3cde951bd4f
    // TODO: after this chrome bug is fixed: https://code.google.com/p/chromium/issues/detail?id=513768
    private void selectOptionFor(String spanId, int option) {
        WebElement select = findSelectInsideSpan(spanId);
        new Select(select).selectByIndex(option);
        select.sendKeys(RETURN);
    }

    private void selectFirstOptionFor(String spanId) {
        selectOptionFor(spanId, 0);
    }

    private void selectSecondOptionFor(String spanId) {
        selectOptionFor(spanId, 1);
    }

    private void selectThirdOptionFor(String spanId) {
        selectOptionFor(spanId, 2);
    }

    private WebElement findSelectInsideSpan(String spanId) {
        return driver.findElement(By.id(spanId)).findElement(By.tagName("select"));
    }

    private WebElement findInputInsideSpan(String spanId) {
        return driver.findElement(By.cssSelector("#" + spanId + " input"));

    }

	public boolean isPatientSearchDisplayed() {
		return driver.findElement(SEARCH_FIELD).isDisplayed();
	}

    private void selectNotToPrintWristbandIfQuestionPresent() {
        try {
            driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            driver.findElement(By.id("print-wristband-question"));
            selectSecondOptionFor("print-wristband-question");
        }
        catch (NoSuchElementException e) {
            // ignore if question not present
        }
        finally {
            driver.manage().timeouts().implicitlyWait(SmokeTestProperties.IMPLICIT_WAIT_TIME, SECONDS);
        }
    }

}
