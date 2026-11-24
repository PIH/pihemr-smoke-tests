package org.openmrs.module.pihemr.smoke;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openmrs.module.pihemr.smoke.helper.NameGenerator;
import org.openmrs.module.pihemr.smoke.pageobjects.HeaderPage;
import org.openmrs.module.pihemr.smoke.pageobjects.MyAccountApp;
import org.openmrs.module.pihemr.smoke.pageobjects.SysAdminPage;
import org.openmrs.module.pihemr.smoke.pageobjects.UserAdmin;
import org.openqa.selenium.By;

import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UserAdminTest extends BasicSmokeTest {

	private static final String DEFAULT_PASSWORD = "Teste123";

	private UserAdmin userAdmin;

	private HeaderPage header;

	private MyAccountApp myAccountApp;

	private String username;

	@Before
	public void setUp() throws Exception {
		userAdmin = new UserAdmin(driver);
		header = new HeaderPage(driver);
		myAccountApp = new MyAccountApp(driver);
		username = createUser();
	}

	@Test
	@Ignore
	// OpenMRS does not support deleting a user that has changed their password due to FOREIGN KEY self-reference
	// See https://issues.openmrs.org/browse/TRUNK-5736
	public void createUserWithPhysicianRoleAndUserChangesOwnPassword() throws Exception {

        login();
        appDashboard.openSysAdminApp();

        userAdmin.createPhysicianAccount(NameGenerator.getUserFirstName(), NameGenerator.getUserLastName(), username,
                DEFAULT_PASSWORD);
		logOutAndLogInWithNewUser(username);

		appDashboard.openMyAccountApp();
		myAccountApp.openChangePassword();
		String newPassword = "something else!";
		myAccountApp.changePassword(DEFAULT_PASSWORD, newPassword);
		logOutAndLogInWithNewUser(username, newPassword);

		assertThat(appDashboard.isActiveVisitsAppPresented(), is(true));

        turnOffImplicitWaits(); // once we've found one element, assume that all are present
        try {
            assertThat(appDashboard.isVitalsSearchAppPresented(), is(true));
            assertThat(appDashboard.isAwaitingAdmissionAppPresented(), is(true));
            assertThat(appDashboard.isInpatientsAppPresented(), is(true));

            assertThat(appDashboard.isSchedulingAppPresented(), is(false));
            assertThat(appDashboard.isSystemAdministrationAppPresented(), is(false));
            assertThat(appDashboard.isPatientRegistrationAppPresented(), is(false));
            assertThat(appDashboard.isReportsAppPresented(), is(false));
            assertThat(appDashboard.isStartClinicVisitAppPresented(), is(false));
            assertThat(appDashboard.isLegacyAppPresented(), is(false));
        } finally {
            turnOnImplicitWait();
        }

        logout();
	}

	@Test
	public void createUserWithScheduleManagerRole() throws Exception {

        login();
        appDashboard.openSysAdminApp();

		userAdmin.createScheduleManagerAccount(NameGenerator.getUserFirstName(), NameGenerator.getUserLastName(), username,
                DEFAULT_PASSWORD);

		logOutAndLogInWithNewUser(username);

        assertThat(appDashboard.isActiveVisitsAppPresented(), is(true));

        turnOffImplicitWaits(); // once we've found one element, assume that all are present
        try {
            assertThat(appDashboard.isAwaitingAdmissionAppPresented(), is(true));
            assertThat(appDashboard.isInpatientsAppPresented(), is(true));
            assertThat(appDashboard.isPatientRegistrationAppPresented(), is(true));
            assertThat(appDashboard.isReportsAppPresented(), is(true));
            assertThat(appDashboard.isSchedulingAppPresented(), is(false)); // appointment scheduling functionality was removed (a02ba0b)

            assertThat(appDashboard.isStartClinicVisitAppPresented(), is(false));
            assertThat(appDashboard.isVitalsSearchAppPresented(), is(false));
            assertThat(appDashboard.isSystemAdministrationAppPresented(), is(false));
            assertThat(appDashboard.isLegacyAppPresented(), is(false));
        } finally {
            turnOnImplicitWait();
        }

        logout();
	}

	@Test
	public void createUserWithArchivistClerkRole() throws Exception {

        login();
        appDashboard.openSysAdminApp();

		userAdmin.createArchivistClerkAccount(NameGenerator.getUserFirstName(), NameGenerator.getUserLastName(), username,
                DEFAULT_PASSWORD);

		logOutAndLogInWithNewUser(username);

        assertThat(appDashboard.isPatientRegistrationAppPresented(), is(true));
        turnOffImplicitWaits(); // once we've found one element, assume that all are present
        try {
            assertThat(appDashboard.isStartClinicVisitAppPresented(), is(true));
            assertThat(appDashboard.isReportsAppPresented(), is(true));

            assertThat(appDashboard.isSchedulingAppPresented(), is(false));
            assertThat(appDashboard.isVitalsSearchAppPresented(), is(false));
            assertThat(appDashboard.isActiveVisitsAppPresented(), is(false));
            assertThat(appDashboard.isSystemAdministrationAppPresented(), is(false));
            assertThat(appDashboard.isLegacyAppPresented(), is(false));
            assertThat(appDashboard.isAwaitingAdmissionAppPresented(), is(false));
            assertThat(appDashboard.isInpatientsAppPresented(), is(false));
        } finally {
            turnOnImplicitWait();
        }

        logout();

    }

	@Test
	public void createUserWithSysAdminRoleWithEnglishAsDesiredLanguage() throws Exception {

        login();
        appDashboard.openSysAdminApp();

		userAdmin.createSysAdminAccount(NameGenerator.getUserFirstName(), NameGenerator.getUserLastName(), username,
		    DEFAULT_PASSWORD, "en");

		logOutAndLogInWithNewUser(username);

        assertThat(appDashboard.isSchedulingAppPresented(), is(false)); // appointment scheduling functionality was removed (a02ba0b)
        turnOffImplicitWaits(); // once we've found one element, assume that all are present
        try {
            assertThat(appDashboard.isActiveVisitsAppPresented(), is(true));
            assertThat(appDashboard.isSystemAdministrationAppPresented(), is(true));
            assertThat(appDashboard.isReportsAppPresented(), is(true));
            assertThat(appDashboard.isInpatientsAppPresented(), is(true));
            assertThat(appDashboard.isPatientRegistrationAppPresented(), is(true));
            assertThat(appDashboard.isStartClinicVisitAppPresented(), is(true));
            assertThat(appDashboard.isVitalsSearchAppPresented(), is(true));

            assertThat(appDashboard.isLegacyAppPresented(), is(false));
        } finally {
            turnOnImplicitWait();
        }

		appDashboard.openSysAdminApp();
        // confirm localized in English
		String text = driver.findElement(By.id(replaceChars(CustomAppLoaderConstants.Apps.MANAGE_ACCOUNTS, ".", "-") + SysAdminPage.SYSTEM_ADMINISTRATION_APP_LINK_SUFFIX)).getText();
		assertFalse(text.contains("Jere Kont"));
		assertTrue(text.contains("Manage Accounts"));

        logout();

    }

	private String createUser() {
		return new String("user" + System.currentTimeMillis());
	}

	private void logOutAndLogInWithNewUser(String username) throws InterruptedException {
		logOutAndLogInWithNewUser(username, DEFAULT_PASSWORD);
	}

	private void logOutAndLogInWithNewUser(String username, String password) throws InterruptedException {
		Thread.sleep(5000);
        header.logOut();
		loginPage.logIn(username, password);
	}

}
