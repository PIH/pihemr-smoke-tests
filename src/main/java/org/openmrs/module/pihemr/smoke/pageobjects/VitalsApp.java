package org.openmrs.module.pihemr.smoke.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

// vitals.xml (v3.0, HAI-1217) is a standard, single-page form--not a one-question-per-screen simple form
public class VitalsApp extends AbstractPageObject {

	public static final String SEARCH_PATIENT_FIELD_ID = "patient-search";

	private static final By CONFIRM_PATIENT_BUTTON = By.className("icon-arrow-right");
	private static final By SUBMIT_BUTTON = By.cssSelector("#buttons input.submitButton");

	public VitalsApp(WebDriver driver) {
		super(driver);
	}

	public void enterPatientIdentifier(String patientID) {
		setTextToField(SEARCH_PATIENT_FIELD_ID, patientID);
	}

	public void confirmPatient() {
		clickOn(CONFIRM_PATIENT_BUTTON);
	}

	public void enterVitals() {
		enterBasicVitals();
		setObsValue("chief_complaint", "headache");
		submit();
	}

	public void enterVitalsForInfant() {
		enterBasicVitals();
		setObsValue("muac_mm", "100");
		setObsValue("head_cm", "100");
		setObsValue("chief_complaint", "headache");
		submit();
	}

	private void enterBasicVitals() {
		wait15seconds.until(visibilityOfElementLocated(By.id("height_cm")));
		setObsValue("height_cm", "15");
		setObsValue("weight_kg", "50");
		setObsValue("temperature_c", "36");
		setObsValue("heart_rate", "50");
		setObsValue("respiratory_rate", "50");
		setObsValue("bp_systolic", "120");
		setObsValue("bp_diastolic", "80");
		setObsValue("o2_sat", "50");
	}

	// tab (rather than enter) out of the field to fire its change handlers without triggering a form submit
	private void setObsValue(String obsId, String value) {
		WebElement field = driver.findElement(By.id(obsId)).findElement(By.cssSelector("input, textarea"));
		field.clear();
		field.sendKeys(value);
		field.sendKeys(Keys.TAB);
	}

	private void submit() {
		clickOn(SUBMIT_BUTTON);
	}
}
