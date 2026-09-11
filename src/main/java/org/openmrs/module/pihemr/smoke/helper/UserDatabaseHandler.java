package org.openmrs.module.pihemr.smoke.helper;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.commons.io.IOUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.openmrs.module.pihemr.smoke.dataModel.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.dbunit.operation.DatabaseOperation.DELETE;
import static org.dbunit.operation.DatabaseOperation.INSERT;

public class UserDatabaseHandler extends BaseDatabaseHandler {
	
	protected static Map<User, IDataSet> datasets = new HashMap<User, IDataSet>();

    protected static List<String> usernamesToDelete = new ArrayList<String>();
	
	public static User insertNewPhysicianUser(String locale) throws Exception {

		return createUserWithApplicationAndProviderRole("physician", "Physician", locale);
	}
	
	public static User insertNewPharmacyManagerUser(String locale) throws Exception {
		return createUserWithApplicationAndProviderRole("pharmacyManager", "Pharmacist", locale);
	}

    public static User insertNewArchivistUser(String locale) throws Exception {
        return createUserWithApplicationAndProviderRole("archivistClerk", "Archivist/Clerk", locale);
    }

    public static User insertNewSysAdminUser(String locale) throws Exception {
        return createUserWithApplicationAndProviderRole("sysAdmin", "Administrator", locale);
    }
	
	private static User createUserWithApplicationAndProviderRole(String role, String providerRole, String locale) throws Exception {
		User user;
		
		try {
			
			BigInteger userId = getNextAutoIncrementFor("users");
			String username = "smoke-test-" + role + "-" + userId;
            Integer providerRoleId = getProviderRoleId(providerRole);
			
			user = new User(getNextAutoIncrementFor("person"), UUID.randomUUID().toString(),
			        getNextAutoIncrementFor("person_name"), userId, username, role, getNextAutoIncrementFor("provider"),
			        UUID.randomUUID().toString(), providerRoleId, UUID.randomUUID().toString(), UUID.randomUUID().toString(), locale);
			
			IDataSet dataset = createDataset(user);
			datasets.put(user, dataset);
			
			INSERT.execute(connection, dataset);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new Exception("unable to create patient in database", e);
		}
		
		usernamesToDelete.add(user.getUsername());
		return user;
	}
	
	public static void deleteAllTestUsers() throws DatabaseUnitException, SQLException {

        for (String username : usernamesToDelete) {
            deleteUser(username);
        }
        // this is a static variable, so we need to clear out the values after deleting
        usernamesToDelete = new ArrayList<String>();
	}

    public static void addUserForDelete(String username) {
        usernamesToDelete.add(username);
    }
	
	public static void deleteUser(String username) throws SQLException, DataSetException, DatabaseUnitException {

		ITable userQuery = connection.createQueryTable("users", "select * from users where username = '" + username + "'");
		Integer userId = (Integer) userQuery.getValue(0, "user_id");
		Integer personId = (Integer) userQuery.getValue(0, "person_id");
		
		QueryDataSet userDataToDelete = new QueryDataSet(connection);
		userDataToDelete.addTable("person", "select * from person where person_id = " + personId);
		userDataToDelete.addTable("provider", "select * from provider where person_id = " + personId);
		userDataToDelete.addTable("person_name", "select * from person_name where person_id = " + personId);
		userDataToDelete.addTable("name_phonetics",
		    "select * from name_phonetics where person_name_id in (select person_name_id from person_name where person_id = "
		            + personId + ")");

		resetColumnThatMatchesUserId("person", "creator", userId);
		resetColumnThatMatchesUserId("person", "changed_by", userId);
		resetColumnThatMatchesUserId("patient", "creator" ,userId);
		resetColumnThatMatchesUserId("patient", "changed_by" ,userId);
		resetColumnThatMatchesUserId("users", "creator" ,userId);
		resetColumnThatMatchesUserId("users", "changed_by" ,userId);
		resetColumnThatMatchesUserId("idgen_identifier_source", "changed_by", userId);
		resetColumnThatMatchesUserId("htmlformentry_html_form", "creator", userId);
		resetColumnThatMatchesUserId("htmlformentry_html_form", "changed_by", userId);

		userDataToDelete.addTable("users", "select * from users where user_id = " + userId);
        // added after "users" -- DatabaseOperation.DELETE processes tables in reverse of
        // insertion order, so this must come after "users" here to be deleted before it,
        // respecting idgen_log_entry.generated_by's FK reference to users.user_id
        userDataToDelete.addTable("idgen_log_entry","select * from idgen_log_entry where generated_by = " + userId );
		userDataToDelete.addTable("user_role", "select * from user_role where user_id = " + userId);
		userDataToDelete.addTable("user_property", "select * from user_property where user_id = " + userId);

        DELETE.execute(connection, userDataToDelete);
	}
	
	private static IDataSet createDataset(User user) throws IOException, DataSetException {
		Handlebars handlebars = new Handlebars();
		Template template = handlebars.compile("datasets/users_dataset.xml");
		
		return new FlatXmlDataSetBuilder().build(new InputStreamReader(IOUtils.toInputStream(template.apply(user))));
	}

    private static Integer getProviderRoleId(String providerRoleName) throws SQLException, DataSetException {
        ITable providerRole = connection.createQueryTable("provider_role",
                "select * from provider_role where name = '" + providerRoleName + "'");
        return  (Integer) providerRole.getValue(0, "provider_role_id");
    }

	public static void setAdminDefaultLocale(String locale) throws SQLException {
		String upsertStmt = "insert into user_property (user_id, property, property_value) values (1, 'defaultLocale', ?) "
				+ "on duplicate key update property_value = ?";
		try (PreparedStatement statement = connection.getConnection().prepareStatement(upsertStmt)) {
			statement.setString(1, locale);
			statement.setString(2, locale);
			statement.executeUpdate();
		}
	}

	private static void resetColumnThatMatchesUserId(String tableName, String columnName, Integer userId) {
		String updateStmt = "update " + tableName + " set " + columnName + " = 1 where " + columnName + " = ?";
		try (PreparedStatement statement = connection.getConnection().prepareStatement(updateStmt)) {
			statement.setInt(1, userId);
			statement.executeUpdate();
		}
		catch (Exception e) {
			throw new RuntimeException("Error updating " + tableName + "." + columnName + " to userId: " + userId, e);
		}
	}
}
