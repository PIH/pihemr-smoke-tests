package org.openmrs.module.pihemr.smoke;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openmrs.module.pihemr.smoke.helper.SmokeTestDriver;
import org.openmrs.module.pihemr.smoke.helper.SmokeTestProperties;
import org.openmrs.module.pihemr.smoke.helper.UserDatabaseHandler;
import org.openmrs.module.pihemr.smoke.pageobjects.AppDashboard;
import org.openmrs.module.pihemr.smoke.pageobjects.ClinicianDashboard;
import org.openmrs.module.pihemr.smoke.pageobjects.HeaderPage;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.LoginPage;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.ZlCentralLoginPage;
import org.openmrs.module.pihemr.smoke.pageobjects.VisitNote;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class BasicSmokeTest {

	protected static LoginPage loginPage;

	protected static HeaderPage header;

	protected static WebDriver driver;

    protected AppDashboard appDashboard;

    protected VisitNote visitNote;

    protected ClinicianDashboard clinicianDashboard;

    protected static boolean createdOwnDriver;

	@Rule
	public TestRule testWatcher = new TestWatcher() {

		private long startTime;

		private boolean testFailed;

		private String testName(Description test) {
			return test.getTestClass().getSimpleName() + "." + test.getMethodName();
		}

		@Override
		protected void starting(Description test) {
			startTime = System.currentTimeMillis();
			testFailed = false;
			System.out.println("===== " + new Date() + " STARTING " + testName(test) + " =====");
		}

		@Override
		protected void succeeded(Description test) {
			System.out.println("===== " + new Date() + " PASSED " + testName(test) + " (" + (System.currentTimeMillis() - startTime) + "ms) =====");
		}

		@Override
		public void failed(Throwable t, Description test) {
			testFailed = true;
			System.out.println("===== " + new Date() + " FAILED " + testName(test) + " (" + (System.currentTimeMillis() - startTime) + "ms): " + t + " =====");

			// best-effort screenshot only -- must never mask the real failure (t) with a screenshot-capture problem
			try {
				File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(tempFile, new File("screenshots/" + test.getDisplayName() + ".png"));
			}
			catch (Exception e) {
				System.out.println("Failed to capture screenshot for " + test.getDisplayName() + ": " + e);
			}

			// likewise best-effort: record where the browser actually was, to help diagnose transient failures
			try {
				System.out.println("Page at failure: url=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
				FileUtils.writeStringToFile(new File("screenshots/" + test.getDisplayName() + ".html"), driver.getPageSource(), "UTF-8");
			}
			catch (Exception e) {
				System.out.println("Failed to capture page source for " + test.getDisplayName() + ": " + e);
			}
		}

		// teardown runs here rather than in an @After method: rules wrap @After, so an @After logout would
		// run *before* failed() and the screenshot/page capture would always show the login page
		@Override
		protected void finished(Description test) {
			try {
				teardown();
			}
			catch (Exception e) {
				// never mask the real failure with a teardown problem (this runs in a finally block)
				if (!testFailed) {
					throw new RuntimeException("teardown failed", e);
				}
				System.out.println("Teardown also failed for " + testName(test) + ": " + e);
			}
		}
	};

	@BeforeClass
	public static void getWebDriver() {

        // when running as a suite, ZlCentralSmokeTestSuite should inject the driver into the context
        // if not running as a suite, we create our own driver (and flag createdOwnDriver as true so that we know we need to do teardown)
        if (driver == null) {
            System.out.println("Initializing new Chrome Driver");
            driver = new SmokeTestDriver().getDriver();
            createdOwnDriver = true;
        }
        else {
            createdOwnDriver = false;
        }
	}

	@Before
    public void initPageObjects() {
        // clear any stale alerts from previous test
        dismissAlertIfPresent();
        // start each test from a known implicit-wait state, in case a previous test left it turned off
        turnOnImplicitWait();

        header = new HeaderPage(driver);
        loginPage = getLoginPage();
        visitNote = new VisitNote(driver);
        appDashboard = new AppDashboard(driver);
        clinicianDashboard = new ClinicianDashboard(driver);
    }

    // defaults to Haiti Multi Location Login (ie Mirebalais, Thomonde), must be specifically overridden by other tests
    protected LoginPage getLoginPage() { return new ZlCentralLoginPage(driver); }

    // invoked by testWatcher.finished(), after any failure diagnostics have been captured
    public void teardown() throws Exception {
        // clear any stale alers
        dismissAlertIfPresent();
        turnOnImplicitWait();
        logout();
    }

    protected static void dismissAlertIfPresent() {
        try {
            driver.switchTo().alert().dismiss();
        }
        catch (NoAlertPresentException e) {
            // no-op, nothing to clean up
        }
    }

    @AfterClass
    public static void after() throws Exception {
        try {
            UserDatabaseHandler.deleteAllTestUsers();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("tear down failed", e);
        }

        if (header == null) {
            header = new HeaderPage(driver);
        }

        // back to home page
        header.home();

        // log out if necessary
        try {
            header.logOut();
        }
        catch (TimeoutException ex) {
            // do nothing, assume we are already logged out
        }

        if (createdOwnDriver) {
            System.out.println("Quitting Created Chrome Driver");
            driver.quit();
        }
    }

	protected static void logInAsPhysicianUser() throws Exception {
		loginPage.logInAsPhysicianUser();
		header.home();
	}

    protected static void logInAsPhysicianUser(String location) throws Exception {
        loginPage.logInAsPhysicianUser(location);
        header.home();
    }

	protected static void logInAsPharmacyManagerUser() throws Exception {
        loginPage.logInAsPharmacyManagerUser();
        header.home();
	}

    protected static void logInAsArchivist() throws Exception{
        loginPage.logInAsArchivistUser();
        header.home();
    }

    protected static void logInAsSysAdmin() throws Exception{
        loginPage.logInAsSysAdminUser();
        header.home();
    }

    protected static void logInAsAdmin() throws Exception {
        loginPage.logInAsAdmin();
        header.home();
    }

    protected static void logInAsAdmin(String location) throws Exception {
        loginPage.logInAsAdmin(location);
        header.home();
    }

    protected void login() throws Exception {
        loginPage.logInAsAdmin();
        header.home();
    }

    protected void home() {
	    header.home();
    }

	protected void logout() {
		new HeaderPage(driver).logOut();
	}

    protected void turnOffImplicitWaits() {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    }

    protected void turnOnImplicitWait() {
        driver.manage().timeouts().implicitlyWait(SmokeTestProperties.IMPLICIT_WAIT_TIME, TimeUnit.SECONDS);
    }

    public static void setDriver(WebDriver driver) {
        BasicSmokeTest.driver = driver;
    }

    public static void setHeader(HeaderPage header) {
        BasicSmokeTest.header = header;
    }

    public void log(String message) {
        System.out.println(new Date() + " - " + getClass().getSimpleName() + ": " + message);
    }
}
