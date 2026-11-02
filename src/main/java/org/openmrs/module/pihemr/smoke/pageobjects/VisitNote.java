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

import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.AdmissionNoteForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.ChiefComplaintForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.ConsultNoteForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.DiagnosisForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.EmergencyDepartmentNoteForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.ExamForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.FeedingForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.HistoryForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.PlanForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.RetroConsultNoteForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.SupplementsForm;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.XRayForm;
import org.openmrs.module.pihemr.smoke.pageobjects.sections.AllergiesSection;
import org.openmrs.module.pihemr.smoke.pageobjects.sections.VaccinationsSection;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class VisitNote extends AbstractPageObject {

	public static final String CHECKIN_CREOLE_NAME = "Tcheke";

	public static final String CONSULTATION_CREOLE_NAME = "Konsiltasyon";

	public static final String VITALS_CREOLE_NAME = "Siy Vito";

	public static final String RADIOLOGY_CREOLE_NAME = "Preskripsyon Radyoloji";

    public static final String ADMISSION_CREOLE_NAME = "Admisyon";

    public static final String TRANSFER_CREOLE_NAME= "Transfè";

    public static final String DISCHARGE_CREOLE_NAME= "Soti Nan Swen Entèn";

	public static final String ACTIVE_VISIT_CREOLE_MESSAGE = "aktif";

    public static final String ADULT_INITIAL_OUTPATIENT_CREOLE_NAME = "Externe Initiale Adulte";

    public static final String ADULT_FOLLOWUP_OUTPATIENT_CREOLE_NAME = "Externe de Suivi Adulte";

    public static final String PEDS_INITIAL_OUTPATIENT_CREOLE_NAME = "Externe Initiale Pédiatrique";

    public static final String PEDS_FOLLOWUP_OUTPATIENT_CREOLE_NAME = "Externe de Suivi Pédiatrique";

    private static final By home = By.className("logo");

    private static final By encounterDetails = By.className("encounter-summary-long");

    public static final By consultNoteSectionHeader = By.className("consult-header");

    private static final By expandEncounter = By.className("expand-encounter");

    private static final By editEncounter = By.className("edit-encounter");

    private static final By deleteEncounter = By.className("delete-encounter");

    private static final By goToAnotherVisit = By.cssSelector("#choose-another-visit button");

    private static final By firstEncounterDetails = By.className("expand-encounter");

    private static final By firstEncounterContractDetails = By.className("contract-encounter");

    private ConsultNoteForm consultNoteForm;

	private EmergencyDepartmentNoteForm eDNoteForm;

	private RetroConsultNoteForm retroConsultNoteForm;

    private AdmissionNoteForm admissionNoteForm;

	private XRayForm xRayForm;

    private ChiefComplaintForm chiefComplaintForm;

    private HistoryForm historyForm;

    private DiagnosisForm diagnosisForm;

    private ExamForm examForm;

    private PlanForm planForm;

    private SupplementsForm supplementsForm;

    private FeedingForm feedingForm;

    private VaccinationsSection vaccinationsSection;

    private AllergiesSection allergiesSection;

	private HashMap<String, By> formList;


	public VisitNote(WebDriver driver) {
		super(driver);
		consultNoteForm = new ConsultNoteForm(driver);
        chiefComplaintForm = new ChiefComplaintForm(driver);
		eDNoteForm = new EmergencyDepartmentNoteForm(driver);
		retroConsultNoteForm = new RetroConsultNoteForm(driver);
		xRayForm = new XRayForm(driver);
        admissionNoteForm = new AdmissionNoteForm(driver);
        vaccinationsSection = new VaccinationsSection(driver);
        allergiesSection = new AllergiesSection(driver);
        historyForm = new HistoryForm(driver);
        diagnosisForm = new DiagnosisForm(driver);
        examForm = new ExamForm(driver);
        planForm = new PlanForm(driver);
        supplementsForm = new SupplementsForm(driver);
        feedingForm = new FeedingForm(driver);
		createFormsMap();
	}

	public boolean verifyIfSuccessfulMessageIsDisplayed() {
		return (driver.findElement(By.className("icon-ok")) != null);
	}

    public void waitUntilVisitNoteOpen() {
        wait15seconds.until(visibilityOfElementLocated(By.id("visit-app")));
    }

	public boolean hasActiveVisit() {
        wait5seconds.until(visibilityOfElementLocated(By.className("active")));
        return driver.findElement(By.className("active")).getText().toLowerCase().contains(ACTIVE_VISIT_CREOLE_MESSAGE);
	}

    public void expandFirstEncounter() {clickOn(expandEncounter);}

    public void editFirstEncounter() {
	    clickOnFirst(editEncounter);
    }

	public void deleteFirstEncounter() {
		clickOnFirst(deleteEncounter);
		clickOn(By.className("delete-encounter-button") );
	}

	public Integer countEncountersOfType(String encounterName) throws Exception {

        try {
            wait15seconds.until(presenceOfElementLocated(By.className("encounter-name")));
            Thread.sleep(2000);  // hack, sleep 2 seconds to let *all* encounter names load
        }
        catch (TimeoutException e) {
            // for the case when there are no encounters of *any* type
            return 0;
        }

		int count = 0;

        List<WebElement> encounters = driver.findElements(By.className("encounter-name"));

        if (!encounters.isEmpty()) {
            for (WebElement encounter : encounters) {
                if (encounterNameWithoutTags(encounter).equals(encounterName))
                    count++;
            }
        }

		return count;
	}

    // the encounter heading also shows a "no data entered" tag next to the name of an encounter without obs or orders
    // (e.g. a multi-section encounter whose sections haven't been filled in yet); getText() only includes it when visible
    private String encounterNameWithoutTags(WebElement encounter) {
        String name = encounter.getText();
        for (WebElement tag : encounter.findElements(By.className("no-data-tag"))) {
            String tagText = tag.getText();
            if (!tagText.isEmpty()) {
                name = name.replace(tagText, "");
            }
        }
        return name.trim();
    }

	public void addConsultNoteWithDischarge(String primaryDiagnosis) throws Exception {
		openForm(formList.get("Consult Note"));
		consultNoteForm.fillFormWithDischarge(primaryDiagnosis);
	}

    public void addConsultNoteWithAdmissionToLocation(String primaryDiagnosis, String nonCodedDiagnosis, String locationName) throws Exception {
        openForm(formList.get("Consult Note"));
        consultNoteForm.fillFormWithAdmission(primaryDiagnosis, nonCodedDiagnosis, locationName);
    }

	public void addConsultNoteWithTransferToLocation(String primaryDiagnosis, String nonCodedDiagnosis, String locationName) throws Exception {
		openForm(formList.get("Consult Note"));
		consultNoteForm.fillFormWithTransfer(primaryDiagnosis, nonCodedDiagnosis, locationName);
	}

	public void addConsultNoteWithDeath(String primaryDiagnosis) throws Exception {
		openForm(formList.get("Consult Note"));
		consultNoteForm.fillFormWithDeath(primaryDiagnosis);
	}

	public void editExistingConsultNote(String primaryDiagnosis) throws Exception {
		editFirstEncounter();
		consultNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
        consultNoteForm.confirmData();
	}

	public void addRetroConsultNoteWithDischarge(String primaryDiagnosis) throws Exception {
		openForm(formList.get("Consult Note"));
		retroConsultNoteForm.fillFormWithDischarge(primaryDiagnosis);
	}

	public void addRetroConsultNoteWithAdmissionToLocation(String primaryDiagnosis, String nonCodedDiagnosis, String locationName) throws Exception {
		openForm(formList.get("Consult Note"));
		retroConsultNoteForm.fillFormWithAdmission(primaryDiagnosis, nonCodedDiagnosis, locationName);
	}

	public void addRetroConsultNoteWithTransferToLocation(String primaryDiagnosis, String nonCodedDiagnosis, String locationName) throws Exception {
		openForm(formList.get("Consult Note"));
		retroConsultNoteForm.fillFormWithTransfer(primaryDiagnosis, nonCodedDiagnosis, locationName);
	}

	public void addEDNote(String primaryDiagnosis) throws Exception {
		openForm(formList.get("ED Note"));
		eDNoteForm.fillFormWithDischarge(primaryDiagnosis);
	}

	public void editExistingEDNote(String primaryDiagnosis) throws Exception {
		editFirstEncounter();
		eDNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
        eDNoteForm.confirmData();
	}

    public void addAdmissionNote(String primaryDiagnosis) throws Exception {
        openForm(formList.get("Admission Note"));
        admissionNoteForm.fillFormWithDiagnosis(primaryDiagnosis);
    }

    public void addAdmissionNoteWithDefaultLocation(String primaryDiagnosis) throws Exception {
        openForm(formList.get("Admission Note"));
        admissionNoteForm.fillFormWithDiagnosisAndDefaultLocation(primaryDiagnosis);
    }

    public void addAdmissionNoteAsAdminUser(String primaryDiagnosis) throws Exception {
        openForm(formList.get("Admission Note"));
        admissionNoteForm.fillFormWithBasicEncounterInfoAndDiagnosis(primaryDiagnosis);
    }

    public void editExistingAdmissionNote(String primaryDiagnosis) throws Exception {
        editFirstEncounter();
        admissionNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
        admissionNoteForm.confirmData();
    }

    public void editExistingAdmissionNote(String primaryDiagnosis, int location, int provider) throws Exception {
        editFirstEncounter();
        admissionNoteForm.selectLocation(location);
        admissionNoteForm.selectProvider(provider);
        admissionNoteForm.editPrimaryDiagnosis(primaryDiagnosis);
        admissionNoteForm.confirmData();
    }

    public void addAdultInitialOutpatient() throws Exception {
        openForm(formList.get("Adult Initial Outpatient"));
    }

    public void addAdultFollowupOutpatient() throws Exception {
        openForm(formList.get("Adult Followup Outpatient"));
    }

    public void addPedsInitialOutpatient() throws Exception {
        openForm(formList.get("Peds Initial Outpatient"));
    }

    public void addPedsFollowupOutpatient() throws Exception {
        openForm(formList.get("Peds Followup Outpatient"));
    }

    public void orderXRay(String study1, String study2) throws Exception {
        openForm(formList.get("Order X-Ray"));
        xRayForm.fillForm(study1, study2);
    }

	public void openForm(By formIdentification) {
        waitUntilVisitNoteOpen();
        wait15seconds.until(visibilityOfElementLocated(By.id("visit-actions-button")));
        clickOn(By.id("visit-actions-button"));
		clickOn(formIdentification);
	}

    public void addAndRemoveVaccine(Integer row, Integer column) {
        vaccinationsSection.clickOnVaccinationSection();
        vaccinationsSection.addVaccination(row, column);
        vaccinationsSection.deleteVaccination(row, column);
    }

    public void openAllergiesSection() {
        allergiesSection.openAllergiesSection();
    }

    public void expandAllergiesSection() {
        allergiesSection.expandAllergiesSection();
    }

    public void addAllergy(Integer typesIndex, Integer reactionsIndex, Integer severitiesIndex) {
        allergiesSection.addAllergy(typesIndex, reactionsIndex, severitiesIndex);
    }

    public void removeAllergy(Integer index) {
        allergiesSection.removeAllergy(index);
    }

    public void returnFromAllergiesPage() {
        allergiesSection.returnFromAllergiesPage();
    }

    public Integer countOfAllergies() {
        return allergiesSection.countOfAllergies();
    }

    public void expandSection(String id) {
        try {
            clickOn(By.cssSelector("#" + id + " .expand-encounter"));
        }
        // try twice in case of stale element exception, which may be caused by interaction with angular?
        catch (StaleElementReferenceException e) {
            clickOn(By.cssSelector("#" + id + " .expand-encounter"));
        }
    }

    public void editSection(String id) {
        clickOn(By.cssSelector("#" + id + " .edit-encounter"));
    }

    public void viewConsultationDetails() {
		clickFirstEncounterDetails();
        wait15seconds.until(visibilityOfElementLocated(encounterDetails));
	}

	public void fillOutChiefComplaint(String chiefComplaintText) {
	    chiefComplaintForm.fillFormWithBasicInfo(chiefComplaintText);
    }

    public void fillOutHistoryForm(String historyText) {
        historyForm.fillFormWithBasicInfo(historyText);
    }

    public void fillOutExamForm(String comment) {
        examForm.fillFormWithBasicInfo(comment);
    }

    public void fillOutDiagnosisForm(String diagnosis) {
        diagnosisForm.fillFormWithBasicInfo(diagnosis);
    }

    public void fillOutPlanForm(String planText, String dispositionText) throws Exception {
        planForm.fillFormWithBasicInfo(planText, dispositionText);
    }

    public void fillOutSupplementsForm() throws Exception {
         supplementsForm.fillFormWithBasicInfo();
    }

    public void fillOutFeedingForm() throws Exception {
        feedingForm.fillFormWithBasicInfo();
    }

	public Boolean containsText(String text) {
		List<WebElement> list = driver.findElements(By.xpath("//*[contains(text(),'" + text + "')]"));
        return list.size() > 0;
	}

	public List<WebElement> getVisits() {
		return driver.findElements(By.cssSelector("#visit-list-dropdown .list-element"));
	}

    public Integer countVisits() {

        clickOn(goToAnotherVisit);

        try {
            wait5seconds.until(presenceOfElementLocated(By.id("visit-list-dropdown")));
        }
        catch (TimeoutException e) {
            return 0;
        }

        return getVisits().size() - 1;
    }

	public String providerForFirstEncounter() {
		return driver.findElement(By.className("encounter-provider")).getText();
	}

	public String locationForFirstAdmission() {
		return driver.findElement(By.className("admission-location")).getText();
	}

	public void clickFirstEncounterDetails() {
        clickOn(firstEncounterDetails);
    }

    public void gotoAppDashboard() {
        driver.findElement(home).click();
    }

	private void createFormsMap() {
		formList = new HashMap<String, By>();
		formList.put("Consult Note", By.id(CustomAppLoaderConstants.Extensions.CONSULT_NOTE_VISIT_ACTION));
		formList.put("Surgical Note", By.id(CustomAppLoaderConstants.Extensions.SURGICAL_NOTE_VISIT_ACTION));
		formList.put("Order X-Ray", By.id(CustomAppLoaderConstants.Extensions.ORDER_XRAY_VISIT_ACTION));
		formList.put("ED Note", By.id(CustomAppLoaderConstants.Extensions.ED_CONSULT_NOTE_VISIT_ACTION));
        formList.put("Admission Note", By.id(CustomAppLoaderConstants.Extensions.ADMISSION_NOTE_VISIT_ACTION));
        formList.put("Adult Initial Outpatient", By.id(CustomAppLoaderConstants.Extensions.PRIMARY_CARE_ADULT_INITIAL_VISIT_ACTION));
        formList.put("Adult Followup Outpatient", By.id(CustomAppLoaderConstants.Extensions.PRIMARY_CARE_ADULT_FOLLOWUP_VISIT_ACTION));
        formList.put("Peds Initial Outpatient", By.id(CustomAppLoaderConstants.Extensions.PRIMARY_CARE_PEDS_INITIAL_VISIT_ACTION));
        formList.put("Peds Followup Outpatient", By.id(CustomAppLoaderConstants.Extensions.PRIMARY_CARE_PEDS_FOLLOWUP_VISIT_ACTION));
    }

    public ConsultNoteForm getConsultNoteForm() {
        return consultNoteForm;
    }

    public EmergencyDepartmentNoteForm getEDNoteForm() {
        return eDNoteForm;
    }

    public RetroConsultNoteForm getRetroConsultNoteForm() {
        return retroConsultNoteForm;
    }

    public AdmissionNoteForm getAdmissionNoteForm() {
        return admissionNoteForm;
    }

    public MedicationDispensed firstMedication() {
        wait15seconds.until(visibilityOfElementLocated(By.className("dispensing-form")));
        WebElement first = driver.findElement(By.className("dispensing-form")).findElements(By.className("medication")).get(0);
        WebElement dispensingInformation = driver.findElement(By.className("dispensing-form"));
        return new MedicationDispensed(dispensingInformation, first, 1);
    }

    public boolean canDispenseMedication() {
        try {
            driver.findElement(By.id(CustomAppLoaderConstants.Extensions.DISPENSE_MEDICATION_VISIT_ACTION));
            return true;
        }
        catch (NoSuchElementException e) {
            return false;
        }
    }

    public class MedicationDispensed {

        private WebElement dispensingInformation;

        private WebElement medication;

        private int order;

        public MedicationDispensed(WebElement dispensingInformation, WebElement medication, int order) {
            this.dispensingInformation = dispensingInformation;
            this.medication = medication;
            this.order = order;
        }

        public String getTypeOfPrescription(){
            return dispensingInformation.findElement(typeOfPrescription()).getText();
        }

        public String getDischargeLocation(){
            return dispensingInformation.findElement(dischargeLocation()).getText();
        }

        public String getName() {
            return medication.findElement(name()).getText();
        }

        public String getFrequency() {
            return medication.findElement(frequency()).getText();
        }

        public String getDose() {
            return medication.findElement(dose()).getText();
        }

        public String getDoseUnit() {
            return medication.findElement(doseUnit()).getText();
        }

        public String getDuration() {
            return medication.findElement(duration()).getText();
        }

        public String getDurationUnit() {
            return medication.findElement(durationUnit()).getText();
        }

        public String getAmount() {
            return medication.findElement(amount()).getText();
        }

        private By typeOfPrescription() {
            return By.cssSelector("[id='Timing of hospital prescription']");
        }

        private By dischargeLocation() {
            return By.cssSelector("[id='Discharge location']");
        }

        private By name() {
            return By.cssSelector("#name" + order);
        }

        private By frequency() {
            return By.cssSelector("#frequencyCoded" + order);
        }

        private By dose() {
            return By.cssSelector("#dose" + order);
        }

        private By doseUnit() {
            return By.cssSelector("#doseUnit" + order);
        }

        private By duration() {
            return By.cssSelector("#duration" + order);
        }

        private By durationUnit() {
            return By.cssSelector("#durationUnit" + order);
        }

        private By amount() {
            return By.cssSelector("#amount" + order);
        }

    }
}
