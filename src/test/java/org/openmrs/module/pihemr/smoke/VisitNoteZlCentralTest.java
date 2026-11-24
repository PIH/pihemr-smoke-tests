package org.openmrs.module.pihemr.smoke;

import org.openmrs.module.pihcore.apploader.CustomAppLoaderConstants;
import org.openmrs.module.pihemr.smoke.pageobjects.AppDashboard;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.LoginPage;
import org.openmrs.module.pihemr.smoke.pageobjects.loginpages.ZlCentralLoginPage;

import static org.apache.commons.lang.StringUtils.replaceChars;

public class VisitNoteZlCentralTest extends VisitNoteTest {

    // TODO this will need to be tweaked further before it works--the Mirebalais app currently doesn't have waiting for consult, etc...

    // override to use the Mirebalais vitals app
    @Override
    protected String getVitalsAppIdentifier() {
        return replaceChars(CustomAppLoaderConstants.Apps.VITALS_SEARCH, ".", "-") + AppDashboard.APP_LINK_SUFFIX;
    }

    @Override
    protected LoginPage getLoginPage() { return new ZlCentralLoginPage(driver); }

    // check-in is done by an archivist/clerk, vitals capture and note authoring by a physician,
    // matching who is actually authorized to perform each action (admin isn't set up as a
    // Provider here, and physicians aren't given access to the check-in app)
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
