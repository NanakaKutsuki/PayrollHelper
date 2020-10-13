package org.kutsuki.payroll;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.payroll.model.Employee;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Common methods shared by all Helpers.
 * 
 * @author MatchaGreen
 */
public abstract class AbstractSheets {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String APPLICATION_NAME = "PayrollHelper";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String MAIN_SHEET_ID = "1AGzsuTlo03umh2e7bGsRV0CTwo6hY9KNlViABVSjj3g";
    private static final String MAIN_RANGE = "Calculator!A2:G";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String USER_ENTERED = "USER_ENTERED";

    private Map<Integer, Employee> employeeMap;
    private Robot robot;
    private Sheets sheets;

    public abstract void keyIn(int ms);

    public abstract void run();

    /**
     * Default Constructor
     */
    public AbstractSheets() {
	try {
	    this.employeeMap = new HashMap<Integer, Employee>();
	    this.robot = new Robot();

	    // Build a new authorized API client service.
	    NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	    this.sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
		    .setApplicationName(APPLICATION_NAME).build();
	} catch (IOException | GeneralSecurityException | AWTException e) {
	    throw new IllegalStateException(e);
	}

	// Interns
	this.employeeMap.put(-2144727510, new Employee("J", "Diet", "10.00"));
    }

    /**
     * Delays execution
     * 
     * @param ms number of milliseconds of delay
     */
    public void delay(int ms) {
	robot.delay(ms);
    }

    /**
     * Parses String object, removes dollar sign and removes comma.
     * 
     * @param o text in object
     * @return String converted Object.
     */
    public String escapeString(Object o) {
	String value = String.valueOf(o);
	value = StringUtils.remove(value, '$');
	value = StringUtils.remove(value, ',');
	return value;
    }

    /**
     * Get Employee.
     * 
     * @param key the Employee key
     * @return the Employee
     */
    public Employee getEmployee(int key) {
	return employeeMap.get(key);
    }

    /**
     * Gets sorted list of Employees.
     * 
     * @return list of Employees.
     */
    public List<Employee> getEmployeeList() {
	List<Employee> employeeList = new ArrayList<Employee>(employeeMap.values());
	Collections.sort(employeeList);
	return employeeList;
    }

    /**
     * Get Main sheet ID
     * 
     * @return The Main sheet ID.
     */
    public String getMainSheetId() {
	return MAIN_SHEET_ID;
    }

    /**
     * Press and releases key.
     * 
     * @param key key to be emulated.
     */
    public void keyPress(int key) {
	robot.keyPress(key);
	robot.keyRelease(key);
	delay(5);
    }

    /**
     * Types keys from String.
     * 
     * @param s String to be typed
     */
    public void keyIn(String s) {
	if (s != null) {
	    for (int i = 0; i < s.length(); i++) {
		keyPress(KeyEvent.getExtendedKeyCodeForChar(s.charAt(i)));
	    }
	}
    }

    /**
     * Parses Employees from Main sheet. One Employee is currently deactivated.
     */
    public void parseEmployees() {
	List<List<Object>> rowList = readSheet(getMainSheetId(), MAIN_RANGE);

	int i = 0;
	while (i < rowList.size() && rowList.get(i).size() > 0) {
	    Employee employee = new Employee(rowList.get(i));

	    // TODO Remove special case
	    if (employee.getFullName().hashCode() != 284331424) {
		employeeMap.put(employee.getFullName().hashCode(), employee);
	    }

	    i++;
	}

	System.out.println("Parsed " + employeeMap.size() + " employees!");
    }

    public List<List<Object>> readSheet(String id, String range) {
	int retries = 0;
	List<List<Object>> result = null;

	while (result == null) {
	    try {
		ValueRange response = sheets.spreadsheets().values().get(id, range).execute();
		result = response.getValues();
		delay(1000);
	    } catch (IOException e) {
		retries++;
		System.out.println("Retrying " + retries + "...");

		if (retries == 10) {
		    e.printStackTrace();
		}

		delay(1000);
	    }
	}

	return result;
    }

    public void writeSheet(String id, String range, ValueRange body) {
	boolean completed = false;
	int retries = 0;

	while (!completed) {
	    try {
		sheets.spreadsheets().values().update(id, range, body).setValueInputOption(USER_ENTERED).execute();
		delay(1000);
		completed = true;
	    } catch (IOException e) {
		retries++;
		System.out.println("Retrying " + retries + "...");

		if (retries == 10) {
		    e.printStackTrace();
		}

		delay(1000);
	    }
	}
    }

    /**
     * Accesses Google Sheets
     * 
     * @param HTTP_TRANSPORT Google HTTP Transport
     * @return the Google Credential
     * @throws IOException Errors from Google Sheets API
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	// Load client secrets.
	InputStream in = AbstractSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	if (in == null) {
	    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	}

	GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	// Build flow and trigger user authorization request.
	GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
		clientSecrets, SCOPES)
			.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
			.setAccessType("offline").build();
	LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

	return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
