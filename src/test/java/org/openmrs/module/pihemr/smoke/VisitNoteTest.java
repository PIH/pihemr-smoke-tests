package org.openmrs.module.pihemr.smoke;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.ZlCentralLoginPage;
import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openmrs.module.pihemr.smoke.pageobjects.AppDashboard;
import org.openmrs.module.pihemr.smoke.pageobjects.CheckInFormPage;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.LoginPage;
import org.openmrs.module.pihemr.smoke.pageobjects.VisitNote;
import org.openmrs.module.pihemr.smoke.pageobjects.VitalsApp;
import org.openmrs.module.pihemr.smoke.pageobjects.forms.BaseHtmlForm;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class VisitNoteTest extends DbTest {

    public static final By SEARCH_FIELD = By.id("patient-search");

    VitalsApp vitals;
    CheckInFormPage newCheckIn;

    @Before
    public void setupApps() {
        vitals = new VitalsApp(driver);
        newCheckIn = new CheckInFormPage(driver);
    }

    @Test
    public void testCreateAdultInitialOutpatientEncounter() throws Exception{

        checkInLogin();

        appDashboard.startClinicVisit();
        newCheckIn.findPatientAndClickOnCheckIn(adultTestPatient.getIdentifier());
        newCheckIn.enterInfo();

        prepareForVitals();

        header.home();
        appDashboard.openApp(getVitalsAppIdentifier());
        vitals.findPatientById(adultTestPatient.getIdentifier(), SEARCH_FIELD);
        Thread.sleep(2000);
        vitals.hitEnterKey();
        vitals.enterVitals();

        header.home();
        appDashboard.goToVisitNoteVisitListAndSelectFirstVisit(adultTestPatient.getId());

        visitNote.addAdultInitialOutpatient();
        assertThat(visitNote.countEncountersOfType(VisitNote.ADULT_INITIAL_OUTPATIENT_CREOLE_NAME), is(1));

        // chief complaint section
     /*   visitNote.editSection("pihcore-chief-complaint");
        visitNote.fillOutChiefComplaint("some complaint");
        visitNote.expandSection("pihcore-chief-complaint");
        assertTrue(visitNote.containsText("some complaint"));
*/
        // allergies section
/*        visitNote.openAllergiesSection();
        visitNote.addAllergy(1, 1, 1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.addAllergy(2, 2, 2);
        assertThat(visitNote.countOfAllergies(), is(2));
        visitNote.removeAllergy(1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.returnFromAllergiesPage();
        visitNote.waitUntilVisitNoteOpen();
        Thread.sleep(2000);  // hack, seems to be needed for some reason,
        visitNote.expandFirstEncounter();*/

        // history section
        visitNote.editSection("pihcore-history");
        visitNote.fillOutHistoryForm("Some history");
        visitNote.expandSection("pihcore-history");
        assertTrue(visitNote.containsText("Some history"));

        // exam section
        visitNote.editSection("physical-exam");
        visitNote.fillOutExamForm("Some comment");
        visitNote.expandSection("physical-exam");
        assertTrue(visitNote.containsText("Some comment"));

        // diagnosis section
        visitNote.editSection("pihcore-diagnosis");
        visitNote.fillOutDiagnosisForm("Douleur");
        visitNote.expandSection("pihcore-diagnosis");
        assertTrue(visitNote.containsText("Douleur"));

        // plan section
        visitNote.editSection("pihcore-plan");
        visitNote.fillOutPlanForm("Some plan", BaseHtmlForm.DISCHARGE);
        visitNote.expandSection("pihcore-plan");
        assertTrue(visitNote.containsText("Some plan"));
    }

    @Test
    public void testCreateAdultFollowupOutpatientEncounter() throws Exception{

        checkInLogin();

        appDashboard.startClinicVisit();
        newCheckIn.findPatientAndClickOnCheckIn(adultTestPatient.getIdentifier());
        newCheckIn.enterInfo();

        prepareForVitals();

        header.home();
        appDashboard.openApp(getVitalsAppIdentifier());
        vitals.findPatientById(adultTestPatient.getIdentifier(), SEARCH_FIELD);
        Thread.sleep(2000);
        vitals.hitEnterKey();
        vitals.enterVitals();

        header.home();
        appDashboard.goToVisitNoteVisitListAndSelectFirstVisit(adultTestPatient.getId());

        visitNote.addAdultFollowupOutpatient();
        assertThat(visitNote.countEncountersOfType(VisitNote.ADULT_FOLLOWUP_OUTPATIENT_CREOLE_NAME), is(1));

        // chief complaint section
        visitNote.editSection("pihcore-chief-complaint");
        visitNote.fillOutChiefComplaint("some complaint");
        visitNote.expandSection("pihcore-chief-complaint");
        assertTrue(visitNote.containsText("some complaint"));

/*        // allergies section
        visitNote.openAllergiesSection();
        visitNote.addAllergy(1,1,1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.addAllergy(2,2,2);
        assertThat(visitNote.countOfAllergies(), is(2));
        visitNote.removeAllergy(1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.returnFromAllergiesPage();
        visitNote.waitUntilVisitNoteOpen();
        Thread.sleep(2000);  // hack, seems to be needed for some reason,
        visitNote.expandFirstEncounter();*/

        // exam section
        visitNote.editSection("physical-exam");
        visitNote.fillOutExamForm("Some comment");
        visitNote.expandSection("physical-exam");
        assertTrue(visitNote.containsText("Some comment"));

        // diagnosis section
        visitNote.editSection("pihcore-diagnosis");
        visitNote.fillOutDiagnosisForm("Douleur");
        visitNote.expandSection("pihcore-diagnosis");
        assertTrue(visitNote.containsText("Douleur"));

        // plan section
        visitNote.editSection("pihcore-plan");
        visitNote.fillOutPlanForm("Some plan", BaseHtmlForm.DISCHARGE);
        visitNote.expandSection("pihcore-plan");
        assertTrue(visitNote.containsText("Some plan"));

    }

    @Test
    public void testCreatePedsInitialOutpatientEncounter() throws Exception{

        checkInLogin();

        appDashboard.startClinicVisit();
        newCheckIn.findPatientAndClickOnCheckIn(newbornTestPatient.getIdentifier());
        newCheckIn.enterInfo();

        prepareForVitals();

        header.home();
        appDashboard.openApp(getVitalsAppIdentifier());
        vitals.findPatientById(newbornTestPatient.getIdentifier(), SEARCH_FIELD);
        Thread.sleep(2000);
        vitals.hitEnterKey();
        vitals.enterVitalsForInfant();

        header.home();
        appDashboard.goToVisitNoteVisitListAndSelectFirstVisit(newbornTestPatient.getId());

        visitNote.addPedsInitialOutpatient();
        assertThat(visitNote.countEncountersOfType(VisitNote.PEDS_INITIAL_OUTPATIENT_CREOLE_NAME), is(1));

        // chief complaint section
    /*    visitNote.editSection("pihcore-chief-complaint");
        visitNote.fillOutChiefComplaint("some complaint");
        visitNote.expandSection("pihcore-chief-complaint");
        assertTrue(visitNote.containsText("some complaint"));*/

        // vaccine section
        visitNote.addAndRemoveVaccine(2,2);

        // allergies section
       /* visitNote.openAllergiesSection();
        visitNote.addAllergy(1,1,1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.addAllergy(2,2,2);
        assertThat(visitNote.countOfAllergies(), is(2));
        visitNote.removeAllergy(1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.returnFromAllergiesPage();
        visitNote.waitUntilVisitNoteOpen();
        Thread.sleep(2000);  // hack, seems to be needed for some reason,
        visitNote.expandFirstEncounter();*/

        // TODO commenting this out for now because it's been very flaky and we haven't been able to fix it--skipping it seems the lesser of two evils
        // supplements section
   /*     visitNote.editSection("pihcore-supplements");
        visitNote.fillOutSupplementsForm();
        visitNote.expandSection("pihcore-supplements");
        assertTrue(visitNote.containsText("[X]"));*/  // bit of a hack

        // history section
        visitNote.editSection("pihcore-history");
        visitNote.fillOutHistoryForm("Some history");
        visitNote.expandSection("pihcore-history");
        assertTrue(visitNote.containsText("Some history"));

        // feeding section
/*        visitNote.editSection("pihcore-feeding");
        visitNote.fillOutFeedingForm();
        visitNote.expandSection("pihcore-feeding");
        assertTrue(visitNote.containsText("[X]"));  // bit of a hack*/

        // exam section
        visitNote.editSection("physical-exam");
        visitNote.fillOutExamForm("Some comment");
        visitNote.expandSection("physical-exam");
        assertTrue(visitNote.containsText("Some comment"));

        // diagnosis section
        visitNote.editSection("pihcore-diagnosis");
        visitNote.fillOutDiagnosisForm("Douleur");
        visitNote.expandSection("pihcore-diagnosis");
        assertTrue(visitNote.containsText("Douleur"));

        // plan section
        visitNote.editSection("pihcore-plan");
        visitNote.fillOutPlanForm("Some plan", BaseHtmlForm.DISCHARGE);
        visitNote.expandSection("pihcore-plan");
        assertTrue(visitNote.containsText("Some plan"));

    }

    @Test
    public void testCreatePedsFollowupOutpatientEncounter() throws Exception{

        checkInLogin();

        appDashboard.startClinicVisit();
        newCheckIn.findPatientAndClickOnCheckIn(newbornTestPatient.getIdentifier());
        newCheckIn.enterInfo();

        prepareForVitals();

        header.home();
        appDashboard.openApp(getVitalsAppIdentifier());
        vitals.findPatientById(newbornTestPatient.getIdentifier(), SEARCH_FIELD);
        Thread.sleep(2000);
        vitals.hitEnterKey();
        vitals.enterVitalsForInfant();

        header.home();
        appDashboard.goToVisitNoteVisitListAndSelectFirstVisit(newbornTestPatient.getId());

        visitNote.addPedsFollowupOutpatient();
        assertThat(visitNote.countEncountersOfType(VisitNote.PEDS_FOLLOWUP_OUTPATIENT_CREOLE_NAME), is(1));

        // chief complaint section
        visitNote.editSection("pihcore-chief-complaint");
        visitNote.fillOutChiefComplaint("some complaint");
        visitNote.expandSection("pihcore-chief-complaint");
        assertTrue(visitNote.containsText("some complaint"));

        // vaccine section
        visitNote.addAndRemoveVaccine(2, 2);

      /*  // allergies section
        visitNote.openAllergiesSection();
        visitNote.addAllergy(1, 1, 1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.addAllergy(2, 2, 2);
        assertThat(visitNote.countOfAllergies(), is(2));
        visitNote.removeAllergy(1);
        assertThat(visitNote.countOfAllergies(), is(1));
        visitNote.returnFromAllergiesPage();
        visitNote.waitUntilVisitNoteOpen();
        Thread.sleep(2000);  // hack, seems to be needed for some reason,
        visitNote.expandFirstEncounter();*/

        // TODO commenting this out for now because it's been very flaky and we haven't been able to fix it--skipping it seems the lesser of two evils
        // supplements section
   /*     visitNote.editSection("pihcore-supplements");
        visitNote.fillOutSupplementsForm();
        visitNote.expandSection("pihcore-supplements");
        assertTrue(visitNote.containsText("[X]"));  */// bit of a hack

        // feeding section
/*        visitNote.editSection("pihcore-feeding");
        visitNote.fillOutFeedingForm();
        visitNote.expandSection("pihcore-feeding");
        assertTrue(visitNote.containsText("[X]"));  // bit of a hack*/

        // exam section
        visitNote.editSection("physical-exam");
        visitNote.fillOutExamForm("Some comment");
        visitNote.expandSection("physical-exam");
        assertTrue(visitNote.containsText("Some comment"));

        // diagnosis section
        visitNote.editSection("pihcore-diagnosis");
        visitNote.fillOutDiagnosisForm("Douleur");
        visitNote.expandSection("pihcore-diagnosis");
        assertTrue(visitNote.containsText("Douleur"));

        // plan section
        visitNote.editSection("pihcore-plan");
        visitNote.fillOutPlanForm("Some plan", BaseHtmlForm.DISCHARGE);
        visitNote.expandSection("pihcore-plan");
        assertTrue(visitNote.containsText("Some plan"));

    }

    protected LoginPage getLoginPage(WebDriver driver) { return new ZlCentralLoginPage(driver); }

    // who checks the patient in; defaults to the same login used for the rest of the flow
    protected void checkInLogin() throws Exception {
        login();
    }

    // hook for switching users between check-in and vitals/note authoring; no-op by default
    // since the same login normally continues for the whole flow
    protected void prepareForVitals() throws Exception {
    }

    protected String getVitalsAppIdentifier() {
        return replaceChars(CustomAppLoaderConstants.Apps.VITALS, ".", "-") + AppDashboard.APP_LINK_SUFFIX;
    }

    protected void findPatient(String identifier) {
        driver.findElement(By.partialLinkText(identifier)).click();
    }

}
