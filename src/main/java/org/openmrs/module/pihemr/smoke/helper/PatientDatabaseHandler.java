package org.openmrs.module.pihemr.smoke.helper;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.io.IOUtils;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.openmrs.module.pihemr.smoke.dataModel.Patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dbunit.operation.DatabaseOperation.DELETE;

public class PatientDatabaseHandler extends BaseDatabaseHandler {

    protected static Map<Patient, IDataSet> datasets = new HashMap<>();

	private static List<Map<String, String>> patientTablesToDelete = null;

	public static void addTestPatientForDelete(BigInteger patientId) throws IOException, DataSetException, SQLException {
		Patient patient = new Patient("123", null, null, patientId, -1, new BigInteger("-1"), new BigInteger("-1"),
                new BigInteger("-1"), new BigInteger("-1"), -1, null, null, null, null, null, null);
		
		datasets.put(patient, createDataset(patient));
	}

	public static Patient insertAdultTestPatient() throws Exception {
    	return insertTestPatient("1934-02-12");
	}

	public static Patient insertNewbornTestPatient() throws Exception {
    	return insertTestPatient(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}

	private static Patient insertTestPatient(String birthdate) throws Exception {
		try {
			String identifierTypeUuid = getPatientIdentifierTypeUuid();
			String identifier = getNextValidPatientIdentifier(identifierTypeUuid);
			String personUuid = createPatientViaApi(identifier, identifierTypeUuid, birthdate);
			BigInteger personId = getPersonIdForUuid(personUuid);
			insertRegistrationEncounter(personId);

			Patient patient = new Patient(identifier, "Crash Test", "Dummy", personId, -1,
			        new BigInteger("-1"), new BigInteger("-1"), new BigInteger("-1"), new BigInteger("-1"), -1,
			        personUuid, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
					UUID.randomUUID().toString(), UUID.randomUUID().toString(), birthdate);

			datasets.put(patient, createDataset(patient));
			return patient;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("unable to create patient via API", e);
		}
	}

	private static String getPatientIdentifierTypeUuid() throws Exception {
		try (PreparedStatement statement = connection.getConnection().prepareStatement(
		        "select metadata_uuid from metadatamapping_metadata_term_mapping where code = 'emr.primaryIdentifierType'")) {
			try (java.sql.ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new Exception("No metadata term mapping found for code 'emr.primaryIdentifierType'");
				}
				return resultSet.getString("metadata_uuid");
			}
		}
	}

	private static String getNextValidPatientIdentifier(String identifierTypeUuid) throws Exception {
		String sourceUuid;
		String sourceQuery = "select s.uuid as source_uuid from idgen_auto_generation_option a "
		        + "join idgen_identifier_source s on a.source = s.id "
		        + "where a.identifier_type = (select patient_identifier_type_id from patient_identifier_type where uuid = ?) "
		        + "and a.automatic_generation_enabled = 1";
		try (PreparedStatement statement = connection.getConnection().prepareStatement(sourceQuery)) {
			statement.setString(1, identifierTypeUuid);
			try (java.sql.ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					throw new Exception(
					        "No identifier source with auto-generation enabled found for identifier type " + identifierTypeUuid);
				}
				sourceUuid = resultSet.getString("source_uuid");
			}
		}

		String response = postJson("/ws/rest/v1/idgen/identifiersource/" + sourceUuid + "/identifier", "{}");
		return extractJsonStringField(response, "identifier");
	}

	private static String createPatientViaApi(String identifier, String identifierTypeUuid, String birthdate) throws Exception {
		String body = "{"
		        + "\"person\": {"
		        + "  \"names\": [{\"givenName\": \"Crash Test\", \"familyName\": \"Dummy\"}],"
		        + "  \"gender\": \"M\","
		        + "  \"birthdate\": \"" + birthdate + "\","
		        + "  \"addresses\": [{\"address1\": \"Cange\", \"address2\": \"cange\", "
		        + "    \"cityVillage\": \"Boucan Carr\\u00e9\", \"stateProvince\": \"Centre\", \"country\": \"Haiti\"}]"
		        + "},"
		        + "\"identifiers\": [{\"identifier\": \"" + identifier + "\", \"identifierType\": \"" + identifierTypeUuid
		        + "\", \"preferred\": true}]"
		        + "}";
		String response = postJson("/ws/rest/v1/patient", body);
		return extractJsonStringField(response, "uuid");
	}

	private static BigInteger getPersonIdForUuid(String uuid) throws Exception {
		try (PreparedStatement statement = connection.getConnection()
		        .prepareStatement("select person_id from person where uuid = ?")) {
			statement.setString(1, uuid);
			try (java.sql.ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return BigInteger.valueOf(resultSet.getLong("person_id"));
			}
		}
	}

	// kept as a plain SQL insert rather than a REST call to the encounter resource -- creating this
	// encounter via REST triggers an unrelated visit-auto-assignment bug in this OpenMRS version,
	// and unlike the patient itself, this encounter is never searched for, so there's no indexing
	// concern in leaving it as SQL
	private static void insertRegistrationEncounter(BigInteger personId) throws Exception {
		String insertStmt = "insert into encounter (encounter_id, patient_id, encounter_type, location_id, creator, "
		        + "date_created, encounter_datetime, uuid, voided) values (?, ?, ?, 1, 1, '2013-06-04 17:00:47.0', "
		        + "'2013-06-04 17:00:47.0', ?, false)";
		try (PreparedStatement statement = connection.getConnection().prepareStatement(insertStmt)) {
			statement.setLong(1, getNextAutoIncrementFor("encounter").longValue());
			statement.setLong(2, personId.longValue());
			statement.setInt(3, getEncounterTypeId());
			statement.setString(4, UUID.randomUUID().toString());
			statement.executeUpdate();
		}
	}

	private static String postJson(String path, String jsonBody) throws Exception {
		SmokeTestProperties properties = new SmokeTestProperties();
		URL url = new URL(properties.getWebAppUrl() + path);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		try {
			String credentials = Base64.getEncoder()
			        .encodeToString(("admin:" + properties.getAdminUserPassword()).getBytes(StandardCharsets.UTF_8));
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Authorization", "Basic " + credentials);
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setDoOutput(true);
			try (OutputStream out = httpConnection.getOutputStream()) {
				out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
			}

			int status = httpConnection.getResponseCode();
			InputStreamReader streamReader = new InputStreamReader(
			    status < 300 ? httpConnection.getInputStream() : httpConnection.getErrorStream(), StandardCharsets.UTF_8);
			StringBuilder responseBody = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(streamReader)) {
				String line;
				while ((line = reader.readLine()) != null) {
					responseBody.append(line);
				}
			}

			if (status >= 300) {
				throw new Exception("POST " + path + " failed with status " + status + ": " + responseBody);
			}
			return responseBody.toString();
		}
		finally {
			httpConnection.disconnect();
		}
	}

	private static String extractJsonStringField(String json, String fieldName) throws Exception {
		Matcher matcher = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
		if (matcher.find()) {
			return matcher.group(1);
		}
		throw new Exception("Field \"" + fieldName + "\" not found in response: " + json);
	}

	public static void deleteAllTestPatients() throws Exception {
		for (Patient patient : datasets.keySet()) {
			deleteTestPatient(patient);
		}
	}
	
	private static Integer getEncounterTypeId() throws SQLException, DataSetException {
		ITable patientRegistrationEncounterType = connection.createQueryTable("encounter_type",
		    "select * from encounter_type where uuid = '873f968a-73a8-4f9c-ac78-9f4778b751b6'"); // Patient Registration
		return (Integer) patientRegistrationEncounterType.getValue(0, "encounter_type_id");
	}
	
	private static BigInteger getTableId(String table, BigInteger patientId) throws DataSetException, SQLException {
		ITable queryTable = connection.createQueryTable(table, "select * from " + table + " where person_id = " + patientId);
		return queryTable.getRowCount() > 0 ? new BigInteger(queryTable.getValue(0, table + "_id").toString())
		        : new BigInteger("-1");
	}
	
	private static void deleteTestPatient(Patient patient) throws Exception {
		for (Map<String, String> tables : getPatientTablesToDelete(connection)) {
			QueryDataSet createdData = new QueryDataSet(connection);
			for (String table : tables.keySet()) {
				// Not every distro has every table (e.g. obs_reference_range is added in OpenMRS 2.7.0,
				// emr_radiology_order is dropped by radiologyapp 2.0.0)
				if (hasTable(table)) {
					createdData.addTable(table, String.format(tables.get(table), patient.getId()));
				}
			}
			DELETE.execute(connection, createdData);
		}
		
		DELETE.execute(connection, datasets.get(patient));
	}
	
	private synchronized static List<Map<String, String>> getPatientTablesToDelete(DatabaseConnection connection) throws Exception {
		if (patientTablesToDelete == null) {
			patientTablesToDelete = new LinkedList<>();

			{
				Map<String, String> m = new LinkedHashMap<>();
				m.put("obs_reference_range", "select * from obs_reference_range where obs_id in (select obs_id from obs where person_id = %d)");
				patientTablesToDelete.add(m);
			}
			{
				Map<String, String> m = new LinkedHashMap<>();
				m.put("obs", "select * from obs where encounter_id in (select encounter_id from encounter where patient_id = %d) and obs_group_id is not null");
				m.put("person_merge_log", "select * from person_merge_log where winner_person_id = %d");
				m.put("paperrecord_paper_record_request", "select * from paperrecord_paper_record_request where paper_record in (select record_id from paperrecord_paper_record where patient_identifier in (select patient_identifier_id from patient_identifier where patient_id = %d))");
				m.put("appointmentscheduling_appointment_request", "select * from appointmentscheduling_appointment_request where patient_id= %d");
				m.put("name_phonetics", "select * from name_phonetics where person_name_id in (select person_name_id from person_name where person_id = %d)");
				m.put("encounter_diagnosis", "select * from encounter_diagnosis where patient_id = %d");
				patientTablesToDelete.add(m);
			}
			{
				Map<String, String> m = new LinkedHashMap<>();
				m.put("person_merge_log", "select * from person_merge_log where loser_person_id = %d");
				m.put("person_attribute", "select * from person_attribute where person_id = %d");
				m.put("patient_identifier", "select * from patient_identifier where patient_id = %d");
				m.put("paperrecord_paper_record", "select * from paperrecord_paper_record where patient_identifier in (select patient_identifier_id from patient_identifier where patient_id = %d)");
				m.put("visit", "select * from visit where patient_id = %d");
				m.put("visit", "select * from visit where patient_id = %d");
				m.put("encounter", "select * from encounter where patient_id = %d");
				m.put("orders", "select * from orders where patient_id = %d");
				m.put("allergy", "select * from allergy where patient_id = %d");
				m.put("test_order", "select * from test_order where order_id in (select order_id from orders where patient_id = %d)");
				m.put("order_attribute", "select * from order_attribute where order_id in (select order_id from orders where patient_id = %d)");
				m.put("emr_radiology_order", "select * from emr_radiology_order where order_id in (select order_id from orders where patient_id = %d)");
				m.put("obs", "select * from obs where encounter_id in (select encounter_id from encounter where patient_id = %d)");
				m.put("encounter_provider", "select * from encounter_provider where encounter_id in (select encounter_id from encounter where patient_id = %d)");
				m.put("person_name", "select * from person_name where person_id = %d");
				m.put("person_address", "select * from person_address where person_id = %d");
				m.put("patient_identifier", "select * from patient_identifier where patient_id = %d");
				patientTablesToDelete.add(m);
			}
		}
		return patientTablesToDelete;
	}
	
	private static IDataSet createDataset(Patient patient) throws IOException, DataSetException {
		Handlebars handlebars = new Handlebars();
		Template template = handlebars.compile("datasets/patients_dataset.xml");
		
		return new FlatXmlDataSetBuilder().build(new InputStreamReader(IOUtils.toInputStream(template.apply(patient))));
	}
	
}
