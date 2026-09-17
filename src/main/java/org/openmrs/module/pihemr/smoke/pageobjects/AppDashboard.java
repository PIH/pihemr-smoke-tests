/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.pihemr.smoke.pageobjects;

import org.openmrs.module.pihemr.smoke.dataModel.Patient;
import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigInteger;

import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class AppDashboard extends AbstractPageObject {

    public static final By SEARCH_FIELD = By.id("patient-search");
    public static final By SEARCH_RESULTS_TABLE = By.id("patient-search-results-table");

    public static final String LEGACY = "legacy-admin-app";
    public static final String APP_LINK_SUFFIX = "-appLink-app";

    public AppDashboard(WebDriver driver) {
        super(driver);
    }

    public void goToAppDashboard() {
        driver.findElement(By.className("logo")).click();
    }

    public void openActiveVisitsApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.ACTIVE_VISITS_LIST, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openMyAccountApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.MY_ACCOUNT, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openInPatientApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.INPATIENTS, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openPatientRegistrationApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.PATIENT_REGISTRATION, ".", "-") + APP_LINK_SUFFIX);
    }

    public void openSysAdminApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.SYSTEM_ADMINISTRATION, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openReportApp() {
    	 openApp(replaceChars(CustomAppLoaderConstants.Apps.REPORTS, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openAwaitingAdmissionApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.AWAITING_ADMISSION, ".", "-") + APP_LINK_SUFFIX);
    }

    public void startClinicVisit() {
		openApp(replaceChars(CustomAppLoaderConstants.Apps.CHECK_IN, ".", "-") + APP_LINK_SUFFIX);
	}

    public void openCheckinApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.CHECK_IN, ".", "-") + APP_LINK_SUFFIX);
    }

    public void openWaitingForConsultApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.WAITING_FOR_CONSULT, ".", "-") + APP_LINK_SUFFIX);
    }

    public void openEDTriageApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.ED_TRIAGE, ".", "-") + APP_LINK_SUFFIX);
    }

    public void openEDTriageQueueApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.ED_TRIAGE_QUEUE, ".", "-") + APP_LINK_SUFFIX);
    }

    public void openMedicationDispensingApp() {
        openApp(replaceChars(CustomAppLoaderConstants.Apps.MEDICATION_DISPENSING, ".", "-") + APP_LINK_SUFFIX);
    }

    public boolean isPatientRegistrationAppPresented() {
		return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.PATIENT_REGISTRATION, ".", "-") + APP_LINK_SUFFIX);
	}

	public boolean isSystemAdministrationAppPresented() {
        return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.SYSTEM_ADMINISTRATION, ".", "-") + APP_LINK_SUFFIX);
    }

    public boolean isActiveVisitsAppPresented() {
		return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.ACTIVE_VISITS_LIST, ".", "-") + APP_LINK_SUFFIX);
	}

	public boolean isUhmCaptureVitalsAppPresented() {
		return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.UHM_VITALS, ".", "-") + APP_LINK_SUFFIX);
	}

	public boolean isReportsAppPresented() {
		return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.REPORTS, ".", "-") + APP_LINK_SUFFIX);
	}

	public Boolean isStartClinicVisitAppPresented() {
		return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.CHECK_IN, ".", "-") + APP_LINK_SUFFIX);
	}

    public Boolean isAwaitingAdmissionAppPresented() {
        return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.AWAITING_ADMISSION, ".", "-") + APP_LINK_SUFFIX);
    }

    public Boolean isInpatientsAppPresented() {
        return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.INPATIENTS, ".", "-") + APP_LINK_SUFFIX);
    }

    public Boolean isSchedulingAppPresented() {
        return isAppButtonPresent(replaceChars(CustomAppLoaderConstants.Apps.APPOINTMENT_SCHEDULING_HOME, ".", "-") + APP_LINK_SUFFIX);
    }

    public Boolean isLegacyAppPresented() {
        return isAppButtonPresent(LEGACY);
    }

    public void findPatientByIdentifier(String identifier) {
        WebElement searchField = driver.findElement(SEARCH_FIELD);
        searchField.sendKeys(identifier);
        searchField.sendKeys(Keys.RETURN);
    }

    public void findPatientByName(Patient patient) {
        WebElement searchField = driver.findElement(SEARCH_FIELD);
        searchField.sendKeys(patient.getName()); // search on patient name
        searchField.sendKeys(Keys.RETURN);

        // patient should be in results list
        WebElement searchResults = driver.findElement(SEARCH_RESULTS_TABLE);
        clickOn(searchResults.findElement(By.xpath("//*[contains(text(), '" + patient.getIdentifier() + "')]")));
    }


    public void findPatientByGivenAndFamilyName(String givenName, String familyName) {
        WebElement searchField = driver.findElement(SEARCH_FIELD);
        searchField.sendKeys(givenName + " " + familyName);
        searchField.sendKeys(Keys.RETURN);

        // patient should be in results list
        try {
            WebElement searchResults = driver.findElement(SEARCH_RESULTS_TABLE);
            clickOn(searchResults.findElement(By.xpath("//*[contains(text(), '" + familyName + ", "
                    + givenName + "')]")));
        } catch (NoSuchElementException e) {
            WebElement table = driver.findElement(SEARCH_RESULTS_TABLE);
            if (table !=null && table.isEnabled()) {
                //find the row
                // Peru does not have a comma between names
                WebElement row = table.findElement(By.xpath("//tr/td[contains(text(), '" + familyName + " "
                        + givenName + "')]"));
                if (row != null) {
                    row.click();
                }
            }
        }
    }

    public void goToVisitNoteVisitList(BigInteger patientId) {
        driver.get(properties.getWebAppUrl() + "/pihcore/visit/visit.page?patient=" + patientId + "#/visitList");
        wait30seconds.until(visibilityOfElementLocated(By.id("visit-list")));
    }


    public void goToVisitNoteVisitListAndSelectFirstVisit(BigInteger patientId) {
        goToVisitNoteVisitList(patientId);

        // hack: if there is a stale element exception, just try to fetch again; see if this helps at all
        // http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp
        try {
            clickOn(driver.findElements(By.className("list-element")).get(0));
        }
        catch (StaleElementReferenceException e) {
            clickOn(driver.findElements(By.className("list-element")).get(0));
        }
    }

    public void goToWaitingForConsultPage(){
        driver.get(properties.getWebAppUrl() + "/pihcore/visit/waitingForConsult.page");
    }

    public void goToClinicianFacingDashboard(BigInteger patientId) {
        driver.get(properties.getWebAppUrl() + "/coreapps/clinicianfacing/patient.page?patientId=" + patientId);
    }

    public void openApp(String appIdentifier) {
        new HeaderPage(driver).home();
        clickOn(By.id(appIdentifier));
    }

    private boolean isAppButtonPresent(String appId) {
        try {
            return driver.findElement(By.id(appId)) != null;
        } catch (Exception ex) {
            return false;
        }
    }

}
