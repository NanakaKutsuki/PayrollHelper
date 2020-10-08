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

public class AbstractSheets {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String APPLICATION_NAME = "PayrollHelper";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String MAIN_SHEET_ID = "1AGzsuTlo03umh2e7bGsRV0CTwo6hY9KNlViABVSjj3g";
    private static final String MAIN_RANGE = "Calculator!A2:G";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String ZERO = "0.00";

    private Map<Integer, Employee> employeeMap;
    private Robot robot;
    private Sheets sheets;

    public AbstractSheets() {
	try {
	    this.employeeMap = new HashMap<Integer, Employee>();
	    this.robot = new Robot();

	    // Build a new authorized API client service.
	    NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	    this.sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
		    .setApplicationName(APPLICATION_NAME).build();
	} catch (IOException | GeneralSecurityException | AWTException e) {
	    e.printStackTrace();
	}

	// Interns
	this.employeeMap.put(-2144727510, new Employee("J", "Diet", "10.00", ZERO));
    }

    public void delay(int ms) {
	robot.delay(ms);
    }

    public Employee getEmployee(int key) {
	return employeeMap.get(key);
    }

    public List<Employee> getEmployeeList() {
	List<Employee> employeeList = new ArrayList<Employee>(employeeMap.values());
	Collections.sort(employeeList);
	return employeeList;
    }

    public Sheets getSheets() {
	return sheets;
    }

    public String getZero() {
	return ZERO;
    }

    public void keyPress(int key) {
	robot.keyPress(key);
	robot.keyRelease(key);
	delay(5);
    }

    public void keyIn(String s) {
	if (s != null) {
	    for (int i = 0; i < s.length(); i++) {
		keyPress(KeyEvent.getExtendedKeyCodeForChar(s.charAt(i)));
	    }
	}
    }

    public void parseEmployees() throws IOException {
	ValueRange response = getSheets().spreadsheets().values().get(MAIN_SHEET_ID, MAIN_RANGE).execute();
	List<List<Object>> rowList = response.getValues();

	int i = 0;
	while (i < rowList.size() && rowList.get(i).size() > 0) {
	    Employee employee = new Employee(rowList.get(i));

	    // TODO Remove special case
	    if (employee.getName().hashCode() != -951030221) {
		employeeMap.put(employee.getName().hashCode(), employee);
	    }

	    i++;
	}
    }

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
