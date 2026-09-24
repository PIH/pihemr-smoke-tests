package org.openmrs.module.pihemr.smoke.pageobjects;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.pihemr.smoke.dataModel.Patient;
import org.openmrs.module.pihemr.smoke.helper.SmokeTestProperties;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openqa.selenium.support.ui.ExpectedConditions.or;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

public class PatientRegistration extends AbstractPageObject {

    private static final By SEARCH_RESULTS_TABLE = By.id("patient-search-results-table");

    public enum Gender {MALE, FEMALE}

    public PatientRegistration(WebDriver driver) {
        super(driver);
    }

    public void registerPatient(String givenName, String familyName, String nickname, Gender gender, Integer birthDay, Integer birthMonth,
                                Integer birthYear, String mothersFirstName, String birthplace, String addressSearchValue,
                                List<Map.Entry<String, String>> contactInfo, Integer insuranceName, String insuranceNumber, String otherInsurance,
                                Integer martialStatus, Integer education, String occupation, Integer religion, String contact, String relationship, String contactAddress,
                                Boolean addressUsesHierarchy, String contactPhoneNumber,
                                Boolean automaticallyEnterIdentifier, Boolean relationshipsEnabled, Boolean biometricsEnabled, Integer printIdCard,
                                Integer additionalIdentifiersCount,
                                List<String> idUuids,
                                Boolean socioInfoEnabled,
                                Boolean patientSupportEnabled,
                                String localContact, String localContactAddress, String localContactPhoneNumber,
                                By successElement) throws Exception{

        wait15seconds.until(visibilityOfElementLocated(By.id("checkbox-enable-registration-date")));
        keepCurrentRegistrationDate();
        if (biometricsEnabled) {
            skipBiometricsSection();
        }
        enterIds(idUuids);
        enterPatientName(familyName, givenName, nickname);
        enterGender(gender);
        enterBirthDate(birthDay, birthMonth, birthYear);
        enterMothersFirstName(mothersFirstName);
        enterPersonAddressViaShortcut(addressSearchValue);
        enterContactInfo(contactInfo);
        selectInsuranceName(insuranceName);
        enterInsuranceNumberAsFreeText(insuranceNumber);
        enterOtherInsuranceNameAsFreeText(otherInsurance);
        enterSocioInfo(socioInfoEnabled);

        if (addressUsesHierarchy) {
            enterBirthplaceViaShortcut(birthplace);
        }
        else {
            enterBirthplaceAsFreeText(birthplace);
        }

        selectMaritalStatus(martialStatus);
        selectLevelOfEducation(education);
        selectOccupation(occupation);
        selectReligion(religion);
        if (relationshipsEnabled) {
            skipRelationshipSection();
        }

        enterPatientSupportInfo(patientSupportEnabled);

        if (StringUtils.isNotBlank(contact)) {
            enterContactPerson(contact);
            enterContactRelationship(relationship);

            if (addressUsesHierarchy) {
                enterContactAddressViaShortcut(contactAddress);
            }
            else {
                enterContactAddressAsFreeText(contact);
            }

            enterContactPhoneNumber(contactPhoneNumber);
        }

        if (StringUtils.isNotBlank(localContact)) {
            enterLocalContactPerson(localContact);
            enterLocalContactAddressViaShortcut(localContactAddress);
            enterLocalContactPhoneNumber(localContactPhoneNumber);
        }
        automaticallyEnterIdentifier(automaticallyEnterIdentifier);

        if (additionalIdentifiersCount != null) {
            enterAdditionalIdentifiers(additionalIdentifiersCount);
        }

        printIdCard(printIdCard);

        confirm(successElement);
    }

    public void keepCurrentRegistrationDate() {
        hitEnterKey(By.id("checkbox-enable-registration-date"));
    }

    public void skipBiometricsSection() {
        List<WebElement> fingerprintSections = driver.findElements(By.className("fingerprints-field"));
        if (fingerprintSections != null) {
            for (WebElement section : fingerprintSections) {
                hitEnterKey();
            }
        }
    }

    public void enterPatientName(String familyName, String givenName, String nickname) {
        setTextToField(By.name("familyName"), familyName);
        setTextToField(By.name("givenName"), givenName);

        if (driver.findElements(By.name("middleName")).size() > 0 ) {
            setTextToField(By.name("middleName"), nickname);
        } else {
            hitTabKey();
        }
    }

    public void enterGender(Gender gender) throws Exception {
        driver.findElement(By.name("gender")).findElements(By.tagName("option")).get((gender.equals(Gender.MALE) ? 0 : 1)).click();
        hitEnterKey(By.name("gender"));
    }

    public void enterBirthDate(Integer day, Integer month, Integer year) {

        setTextToField(By.name("birthdateDay"), day.toString());

        // TODO: revert https://github.com/PIH/mirebalais-smoke-tests/commit/62837a40a4461529c45cf78645aac65a032d2c92
        // TODO: after this chrome bug is fixed: https://code.google.com/p/chromium/issues/detail?id=513768
        WebElement birthDateMonth = driver.findElement(By.name("birthdateMonth"));
        new Select(birthDateMonth).selectByIndex(month);
        birthDateMonth.sendKeys(Keys.TAB);

        setTextToField(By.name("birthdateYear"), year.toString());
    }

    public void enterIds(List<String> idUuids) {
        for (String idUuid : idUuids) {
            if (StringUtils.isNotBlank(idUuid)) {
                WebElement element = driver.findElement(By.name("patientIdentifier" + idUuid));
                if (element != null && element.isDisplayed()) {
                    hitEnterKey(By.name("patientIdentifier" + idUuid));
                }
            }
        }
    }

    public void enterSocioInfo(Boolean socioInfoEnabled) {
        if (socioInfoEnabled) {
            selectElementFromList("obs.PIH:Immigrant", 1);
            selectElementFromList("obs.PIH:Indigenous", 1);
            //Is the patient disabled?
            selectElementFromList("obs.CIEL:162558", 1);
            //literate
            selectElementFromList("obs.CIEL:159400", 1);
            //casefinding
            selectElementFromList("obs.PIH:Found through active casefinding", 1);
        }
    }

    public void enterPatientSupportInfo(Boolean patientSupportEnabled) {
        // TODO: I believe this is no longer used anywhere?
        if (patientSupportEnabled) {
            selectElementFromList("obsgroup.PIH:14493.obs.PIH:14494", 2);
            setTextToField(By.name("obsgroup.PIH:14493.obs.PIH:6402"), "The health center"); // Location
            setTextToField(By.name("obsgroup.PIH:14493.obs.PIH:13173"), "ABC123"); // ID Number
            setTextToField(By.name("obsgroup.PIH:14493.obs.CIEL:164141"), "Supporter Name"); // Name
            setTextToField(By.name("obsgroup.PIH:14493.obs.PIH:2614"), "1111-222"); // Phone
        }
    }

    public void selectElementFromList(String elementName, Integer position) {

        if (driver.findElements(By.name(elementName)).size() > 0 ) {
            selectFromDropdown(By.name(elementName), position);
            hitEnterKey(By.name(elementName));
        }
    }

    public void enterMothersFirstName(String mothersFirstName) {
        if (StringUtils.isNotBlank(mothersFirstName)) {
            setTextToField(By.name("mothersFirstName"), mothersFirstName);
        }
    }

    public void enterPersonAddressViaShortcut(String searchValue) throws Exception{
        enterAddressViaShortcut(searchValue,0);  // index=0 because person address is first address in the registration form
    }

    public void enterAddressViaShortcut(String searchValue, int index)  throws Exception {

        // hack to hopefully add a delay when opening in edit mode
        if (index == 0) {
            wait5seconds.until(visibilityOfElementLocated(By.className("address-hierarchy-shortcut")));
        }

        WebElement searchBox = driver.findElements(By.className("address-hierarchy-shortcut")).get(index);
        searchBox.sendKeys(searchValue);
        wait5seconds.until(visibilityOfElementLocated(By.partialLinkText(searchValue)));
        searchBox.sendKeys(Keys.ENTER);

        // Some forms have additional address fields that need to be filled out or skipped
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        try {
            Thread.sleep(2000); // hack delay to allow address fields time to get disabled
            if (driver.findElements(By.name("address1")).size() > 0 && driver.findElement(By.name("address1")).isEnabled() && driver.findElement(By.name("address1")).isDisplayed()) {
                enterAddressField(By.name("address1"), "123 Main St");
            }
            if (driver.findElements(By.name("cityVillage")).size() > 0 && driver.findElement(By.name("cityVillage")).isEnabled() && driver.findElement(By.name("cityVillage")).isDisplayed()) {
                hitTabKey(By.name("cityVillage"));
            }
            if (driver.findElements(By.name("address2")).size() > 0 && driver.findElement(By.name("address2")).isEnabled() && driver.findElement(By.name("address2")).isDisplayed()) {
                enterAddressField(By.name("address2"), "123 Main St");
            }
            if (driver.findElements(By.name("obsgroup.PIH:Birthplace address construct.obs.PIH:Address2")).size() > 0 && driver.findElement(By.name("obsgroup.PIH:Birthplace address construct.obs.PIH:Address2")).isEnabled() && driver.findElement(By.name("obsgroup.PIH:Birthplace address construct.obs.PIH:Address2")).isDisplayed()) {
                hitTabKey(By.name("obsgroup.PIH:Birthplace address construct.obs.PIH:Address2"));
            }
            if (driver.findElements(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:Address2")).size() > 0 && driver.findElement(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:Address2")).isEnabled() && driver.findElement(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:Address2")).isDisplayed()) {
                enterAddressField(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:Address2"), "");
            }
            if (driver.findElements(By.name("obsgroup.PIH:14704.obs.PIH:Address1")).size() > 0 && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:Address1")).isEnabled() && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:Address1")).isDisplayed()) {
                enterAddressField(By.name("obsgroup.PIH:14704.obs.PIH:Address1"), "");
            }
            if (driver.findElements(By.name("obsgroup.PIH:14704.obs.PIH:City Village")).size() > 0 && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:City Village")).isEnabled() && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:City Village")).isDisplayed()) {
                hitTabKey(By.name("obsgroup.PIH:14704.obs.PIH:City Village"));
            }
            if (driver.findElements(By.name("obsgroup.PIH:14704.obs.PIH:Address2")).size() > 0 && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:Address2")).isEnabled() && driver.findElement(By.name("obsgroup.PIH:14704.obs.PIH:Address2")).isDisplayed()) {
                enterAddressField(By.name("obsgroup.PIH:14704.obs.PIH:Address2"), "");
            }
        }
        finally {
            driver.manage().timeouts().implicitlyWait(SmokeTestProperties.IMPLICIT_WAIT_TIME, SECONDS);
        }
    }

    public void enterAddressField(By elementId, String text) {
        WebElement field = (driver.findElement(elementId));
        if (StringUtils.isNotBlank(text)) {
            field.sendKeys(text);
        }
        field.sendKeys(Keys.ENTER);
        if (field.isDisplayed()) {
            field.sendKeys(Keys.ENTER);
        }
    }
    public void enterContactInfo(List<Map.Entry<String, String>> namesToValues) {
        for (Map.Entry<String, String> nameAndValue : namesToValues) {
            setTextToField(By.name(nameAndValue.getKey()), nameAndValue.getValue());
        }
    }

    public void enterBirthplaceViaShortcut(String searchValue) throws Exception {
        if (StringUtils.isNotBlank(searchValue)) {
            enterAddressViaShortcut(searchValue, 1);  // index=1 because place of birth is the second address in the registration form
        }
    }

    public void enterBirthplaceAsFreeText(String birthplace) {
        if (StringUtils.isNotBlank(birthplace)) {
            setTextToField(By.name("obs.PIH:PLACE OF BIRTH"), birthplace);
        }
    }

    public void selectMaritalStatus(Integer option) {
        if (option != null) {
            selectFromDropdown(By.name("obs.PIH:CIVIL STATUS"), option);
            hitEnterKey(By.name("obs.PIH:CIVIL STATUS"));
        }
    }

    public void selectLevelOfEducation(Integer option) {
        if(option != null){
            selectFromDropdown(By.name("obs.PIH:HIGHEST LEVEL OF SCHOOL COMPLETED"), option);
            hitEnterKey(By.name("obs.PIH:HIGHEST LEVEL OF SCHOOL COMPLETED"));
        }
    }

    public void selectOccupation(String occupation) {
        if (StringUtils.isNotBlank(occupation)){
            try {
                wait5seconds.until(visibilityOfElementLocated(By.className("dropdown-field-textbox")));

                WebElement searchBox = driver.findElements(By.className("dropdown-field-textbox")).get(0);
                searchBox.sendKeys(occupation);
                wait5seconds.until(visibilityOfElementLocated(By.partialLinkText(occupation)));
                searchBox.findElement(By.xpath("//li/a[contains(text(), '" + occupation + "')]")).click();
                searchBox.sendKeys(Keys.ENTER);
            } catch (NoSuchElementException | TimeoutException ex) {
                // some implementations still use the dropbox list
                WebElement occupationList = driver.findElement(By.name("obs.PIH:Occupation"));
                occupationList.findElement(By.xpath("//option[contains(text(), '" + occupation + "')]")).click();
                hitEnterKey(By.name("obs.PIH:Occupation"));
            }
        }
    }

    public void skipRelationshipSection() {
        // TODO might want to implement this, but would depend on having another patient being set up
        if (driver.findElements(By.name("relationship_type")).size() > 0 && driver.findElement(By.name("relationship_type")).isDisplayed()) {
            hitTabKey(By.name("relationship_type"));
            hitTabKey(By.name("relationship_type"));
        }
        if (driver.findElements(By.className("searchablePerson")).size() > 0 && driver.findElement(By.className("searchablePerson")).isDisplayed()) {
            //for SL there are two searchable person name input boxes displayed and each one requires two tabs

            List<WebElement> elements = driver.findElements(By.className("searchablePerson"));
            for (WebElement element : elements) {
                if(element.isEnabled() && element.isDisplayed()) {
                    element.sendKeys(Keys.TAB);
                    element.sendKeys(Keys.TAB);
                    element.sendKeys(Keys.TAB);
                    driver.manage().timeouts().implicitlyWait(SmokeTestProperties.IMPLICIT_WAIT_TIME, SECONDS);
                }
            }
        }


    }

    public void enterContactPerson(String contact) {
        if (StringUtils.isNotEmpty(contact)) {
            setTextToField(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:NAMES AND FIRSTNAMES OF CONTACT"), contact);
        }
    }

    public void enterLocalContactPerson(String localContact) {
        if (StringUtils.isNotEmpty(localContact)) {
            setTextToField(By.name("obsgroup.PIH:14704.obs.PIH:NAMES AND FIRSTNAMES OF CONTACT"), localContact);
        }
    }

    public void enterContactRelationship(String relationship) {
        if (StringUtils.isNotEmpty(relationship)) {
            setTextToField(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:RELATIONSHIPS OF CONTACT"), relationship);
        }
    }

    public void enterContactAddressViaShortcut(String searchValue) throws Exception {
        if (StringUtils.isNotEmpty(searchValue)) {
            enterAddressViaShortcut(searchValue, 2);  // index=2 because contact is the third address in the registration form
        }
    }

    public void enterLocalContactAddressViaShortcut(String searchValue) throws Exception{
        if (StringUtils.isNotEmpty(searchValue)) {
            enterAddressViaShortcut(searchValue, 1);
        }
    }

    public void enterContactAddressAsFreeText(String searchValue) {
        if (StringUtils.isNotEmpty(searchValue)) {
            setTextToField(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:ADDRESS OF PATIENT CONTACT"), searchValue);
            hitTabKey(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:ADDRESS OF PATIENT CONTACT"));
        }
    }

    public void enterContactPhoneNumber(String phoneNumber) {
        if (StringUtils.isNotEmpty(phoneNumber)) {
            setTextToField(By.name("obsgroup.PIH:PATIENT CONTACTS CONSTRUCT.obs.PIH:TELEPHONE NUMBER OF CONTACT"), phoneNumber);
        }
    }

    public void enterLocalContactPhoneNumber(String phoneNumber) {
        if (StringUtils.isNotEmpty(phoneNumber)) {
            setTextToField(By.name("obsgroup.PIH:14704.obs.PIH:TELEPHONE NUMBER OF CONTACT"), phoneNumber);
        }
    }

    public void selectReligion(Integer option) {
        if (option != null) {
            selectFromDropdown(By.name("obs.PIH:Religion"), option);
            hitEnterKey(By.name("obs.PIH:Religion"));
        }
    }

    public void selectInsuranceName(Integer option) {
        if (option != null) {
            try {
                WebElement haitiInsurance = driver.findElement(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Haiti insurance company name"));
                if (haitiInsurance != null && haitiInsurance.isDisplayed()) {
                    selectFromDropdown(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Haiti insurance company name"), option);
                    hitEnterKey(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Haiti insurance company name"));
                }
            } catch(NoSuchElementException e) {
                WebElement mexicoInsurance = driver.findElement(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Mexico Insurance Coded"));
                if (mexicoInsurance != null && mexicoInsurance.isDisplayed()) {
                    selectFromDropdown(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Mexico Insurance Coded"), option);
                    hitEnterKey(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Mexico Insurance Coded"));
                }
            }
        }
    }

    public void enterInsuranceNumberAsFreeText(String insuranceNumber) {
        if (StringUtils.isNotBlank(insuranceNumber)) {
            setTextToField(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Insurance policy number"), insuranceNumber);
        }
    }

    public void enterOtherInsuranceNameAsFreeText(String otherInsurance) {
        if (StringUtils.isNotBlank(otherInsurance)) {
            setTextToField(By.name("obsgroup.PIH:Insurance CONSTRUCT.obs.PIH:Insurance company name (text)"), otherInsurance);
        }
    }

    public void automaticallyEnterIdentifier(Boolean automaticallyEnterIdentifier) {
        if (automaticallyEnterIdentifier != null && automaticallyEnterIdentifier) {
            driver.findElement(By.id("checkbox-autogenerate-identifier")).sendKeys(Keys.ENTER);
        }
        else {
            // handle manual entry case
        }
    }

    public void enterAdditionalIdentifiers(int count) {
        while (count > 0) {
            // TODO: actually test adding the identifiers instead of just skipping
            hitTabKey();
            count--;
        }
    }

    public void printIdCard(Integer option) {
        if (option != null && option != 0) {
            selectFromDropdown(By.name("obs.PIH:ID Card Printing Requested"), option);
            hitEnterKey(By.name("obs.PIH:ID Card Printing Requested"));
        }
    }

    public void manuallyEnterIdentifier(String identifier) {
        driver.findElement(By.id("checkbox-autogenerate-identifier")).sendKeys(Keys.SPACE);
        setTextToField(By.id("patient-identifier"), identifier);
    }

    public void confirm(By successElement) {
        hitEnterKey(By.className("submitButton"));
        wait30seconds.until(visibilityOfElementLocated(successElement));  // wait for reload of the landing page
    }

    public void editExistingPatient(Patient patient, String givenName, String familyName,
                                    String nickname, Gender gender, Integer birthDay, Integer birthMonth,
                                    Integer birthYear, String mothersFirstName, String addressSearchValue,
                                    String contact, String relationship, String contactPhoneNumber,
                                    Boolean placeOfBirthAndContactAddressUseHierarchy,
                                    List<Map.Entry<String, String>> contactInfo) throws Exception {

        // find the existing patient
        findExistingPatient(patient);
        editDemographics(familyName, givenName,  nickname, gender, birthDay, birthMonth, birthYear, mothersFirstName);
        editContactInfo(addressSearchValue, contactInfo);
        editRegistration();
        //editSocial(addressSearchValue, religion, placeOfBirthAndContactAddressUseHierarchy);

        if (StringUtils.isNotEmpty(contact)) {
            editContactPerson(contact, relationship, addressSearchValue, contactPhoneNumber, placeOfBirthAndContactAddressUseHierarchy);
        }
    }

    public void findExistingPatient(Patient patient) {

        setTextToField(By.id("patient-search"), patient.getName());

        // patient should be in results list
        try {
            WebElement searchResults = driver.findElement(SEARCH_RESULTS_TABLE);
            searchResults.findElement(By.xpath("/*//*[contains(text(), '" + patient.getFamily_name() + ", "
                    + patient.getGiven_name() + "')]")).click();
        } catch (NoSuchElementException e) {
            WebElement table = driver.findElement(SEARCH_RESULTS_TABLE);
            if (table !=null && table.isEnabled()) {
                //find the row
                // Peru does not have a comma between names
                WebElement row = table.findElement(By.xpath("//tr/td[contains(text(), '" + patient.getFamily_name() + " "
                        + patient.getGiven_name() + "')]"));
                if (row != null) {
                    row.click();
                }
            }
        }
    }

    public void editDemographics(String familyName, String givenName, String nickname, Gender gender, Integer birthDay,
                                 Integer birthMonth, Integer birthYear, String mothersFirstName)  throws Exception {
        clickOn(By.id("demographics-edit-link"));
        enterPatientName(familyName, givenName, nickname);
        enterGender(gender);
        enterBirthDate(birthDay, birthMonth, birthYear);
        hitTabKey(By.id("birthdateEstimated-field"));
        enterMothersFirstName(mothersFirstName);
        confirm(By.id("demographics-edit-link")); // edit-link is success element to confirm back on edit page
    }

    public void editContactInfo(String searchValue, List<Map.Entry<String, String>> contactInfo) throws Exception{
        clickOn(By.id("contactInfo-edit-link"));
        enterPersonAddressViaShortcut(searchValue);
        enterContactInfo(contactInfo);
        confirm(By.id("demographics-edit-link")); // edit-link is success element to confirm back on edit page
    }

    public void editRegistration() {
        clickOn(By.cssSelector("#coreapps-mostRecentRegistrationSummary .edit-action"));
        // just edit location  and provider for now to keep it simple--should verify that the edit page opens, can be updated, and saves
        selectFromDropdown(By.cssSelector("#patientRegistration select:nth-of-type(1)"), 1);  // kind of a hack, should add ID to encounterProviderAndRole tag
        selectFromDropdown(By.cssSelector("#encounterLocationField select"), 1);
        confirm(By.id("demographics-edit-link")); // edit-link is success element to confirm back on edit page
    }

    public void editSocial(String addressSearchValue, Integer religion, Boolean useHierarchyForPlaceOfBirth) throws Exception {
        clickOn(By.cssSelector("#coreapps-mostRecentRegistrationSocial .edit-action"));

        if (useHierarchyForPlaceOfBirth) {
            enterAddressViaShortcut(addressSearchValue, 0);
        }
        else {
            setTextToField(By.cssSelector("#placeOfBirth input"), addressSearchValue);
        }

        selectFromDropdown(By.cssSelector("#civilStatus"), 4);
        selectFromDropdown(By.cssSelector("#occupation"), 4);

        if (religion != null) {
            selectFromDropdown(By.cssSelector("#religion"), religion);
        }

        confirm(By.id("demographics-edit-link")); // edit-link is success element to confirm back on edit page
    }

    public void editContactPerson(String contact, String relationship, String addressSearchValue, String contactPhoneNumber, Boolean userHierarchyForContactPersonAddress) throws Exception{
        clickOn(By.cssSelector("#coreapps-mostRecentRegistrationContact .edit-action"));

        setTextToField(By.cssSelector("#contactName input"), contact);
        setTextToField(By.cssSelector("#contactRelationship input"), relationship);

        if (userHierarchyForContactPersonAddress) {
            enterAddressViaShortcut(addressSearchValue, 0);
        }
        else {
            setTextToField(By.cssSelector("#contactAddress textarea"), addressSearchValue);
        }

        setTextToField(By.cssSelector("#contactPhoneNumber input"), contactPhoneNumber);

        confirm(By.id("demographics-edit-link")); // edit-link is success element to confirm back on edit page
    }

}
