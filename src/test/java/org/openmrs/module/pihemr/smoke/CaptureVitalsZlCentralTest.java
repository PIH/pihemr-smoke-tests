package org.openmrs.module.pihemr.smoke;

import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openmrs.module.pihemr.smoke.pageobjects.AppDashboard;
import org.openmrs.module.pihemr.smoke.pageobjects.VitalsApp;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.apache.commons.lang.StringUtils.replaceChars;

public class CaptureVitalsZlCentralTest extends CaptureVitalsTest {

	@Override
	protected String getVitalsAppIdentifier() {
		return replaceChars(CustomAppLoaderConstants.Apps.VITALS_SEARCH, ".", "-") + AppDashboard.APP_LINK_SUFFIX;
	}

	@Override
	protected void findPatient(String identifier) {
		vitals.enterPatientIdentifier(identifier);
		vitals.confirmPatient();
	}

	@Override
	protected boolean isVitalsHomePageDisplayed() {
		try {
			new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.id(VitalsApp.SEARCH_PATIENT_FIELD_ID)));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// check-in is done by an archivist/clerk, vitals capture by a physician, matching who is
	// actually authorized to perform each action (admin isn't set up as a Provider here, and
	// physicians aren't given access to the check-in app)
	@Override
	protected void checkInLogin() throws Exception {
		logInAsArchivist();
	}

	@Override
	protected void login() throws Exception {
		logInAsPhysicianUser();
	}

	@Override
	protected void prepareForVitals() throws Exception {
		logout();
		login();
	}

}
