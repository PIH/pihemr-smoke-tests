package org.openmrs.module.pihemr.smoke.pageobjects.loginpages;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.pihemr.smoke.dataModel.User;
import org.openmrs.module.pihemr.smoke.helper.SmokeTestProperties;
import org.openmrs.module.pihemr.smoke.helper.UserDatabaseHandler;
import org.openmrs.module.pihemr.smoke.pageobjects.HeaderPage;
import org.openmrs.module.pihemr.smoke.pageobjects.TermsAndConditionsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class LoginPage {

	protected WebDriver driver;
	protected WebDriverWait wait30seconds;

	protected static SecretQuestionLoginPage secretQuestionLoginPage;
	protected static TermsAndConditionsPage termsAndConditionsPage;

	public LoginPage(WebDriver driver) {
		this.driver = driver;
		this.wait30seconds = new WebDriverWait(driver, 30);
		termsAndConditionsPage = new TermsAndConditionsPage(driver);
		secretQuestionLoginPage = new SecretQuestionLoginPage(driver);
	}

	public void logIn(String user, String password, String location) {
		ensureOnLoginPage();
		driver.findElement(By.id("username")).sendKeys(user);
		wait30seconds.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(password);
		wait30seconds.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
		secretQuestionLoginPage.enterSecretQuestion(password);
		termsAndConditionsPage.acceptTermsIfPresent();
		selectFacilityIfNeeded();
		location = (StringUtils.isBlank(location) ? getDefaultLocationName() : location);
		// scoped to the actual location list items (contains(., ...) matches the element's full text
		// content, not just a direct text-node child) rather than a bare //*[contains(text(), ...)]
		// search of the whole DOM, which can also match unrelated static text (e.g. a page heading
		// showing the same location name) elsewhere on the page
		By locationOption = By.xpath("//li[contains(@class, 'location-list-item') and contains(., '" + location + "')]");
		WebElement locationElement;
		try {
			locationElement = wait30seconds.until(ExpectedConditions.elementToBeClickable(locationOption));
		}
		catch (TimeoutException e) {
			// hack: retry once -- the location list occasionally isn't ready in time on first load
			driver.navigate().refresh();
			locationElement = wait30seconds.until(ExpectedConditions.elementToBeClickable(locationOption));
		}
		// native .click() isn't always reliably registered by Selenium/Chrome (see AbstractPageObject.clickOn()
		// for the same issue elsewhere in this codebase); use a JS-executed click on the exact element the
		// wait already confirmed clickable, rather than re-querying and native-clicking a second lookup
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", locationElement);
	}

	// the login form is normally reached via the logout redirect, which can be slow on a loaded CI runner
	// (longer than the implicit wait); if it still hasn't appeared (e.g. logout didn't take and we're sitting
	// on some other page), log out again -- which redirects to the login page -- and retry once
	private void ensureOnLoginPage() {
		By usernameField = By.id("username");
		try {
			wait30seconds.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
		}
		catch (TimeoutException e) {
			System.out.println("Login form not found (current url: " + driver.getCurrentUrl() + "), logging out and retrying");
			new HeaderPage(driver).logOut();
			wait30seconds.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
		}
	}

	// some servers show a facility-selection step (a "visit-location-select" list) that must be
	// clicked before the location-selection section becomes visible; no-op unless overridden
	protected void selectFacilityIfNeeded() {
	}

	public abstract String getLocale();

	public abstract String getDefaultLocationName();

	public void logIn(String user, String password) {
		logIn(user, password, null);
	}

	public void logInAsAdmin() {
		this.logIn("admin", new SmokeTestProperties().getAdminUserPassword());
	}

    public void logInAsAdmin(String location) {
        this.logIn("admin", new SmokeTestProperties().getAdminUserPassword(), location);
    }

	public void logInAsPhysicianUser() throws Exception {
		User clinical = UserDatabaseHandler.insertNewPhysicianUser(getLocale());
		this.logIn(clinical.getUsername(), "Admin123");
	}

    public void logInAsPhysicianUser(String location) throws Exception {
        User clinical = UserDatabaseHandler.insertNewPhysicianUser(getLocale());
        this.logIn(clinical.getUsername(), "Admin123", location);
    }

	public void logInAsPharmacyManagerUser() throws Exception {
		User pharmacist = UserDatabaseHandler.insertNewPharmacyManagerUser(getLocale());
		this.logIn(pharmacist.getUsername(), "Admin123", "Klinik Ekstèn Famasi");
	}

    public void logInAsArchivistUser() throws Exception{
        User archivist = UserDatabaseHandler.insertNewArchivistUser(getLocale());
        this.logIn(archivist.getUsername(), "Admin123");
    }

    public void logInAsSysAdminUser() throws Exception {
        User sysAdmin = UserDatabaseHandler.insertNewSysAdminUser(getLocale());
        this.logIn(sysAdmin.getUsername(), "Admin123");
    }

}
