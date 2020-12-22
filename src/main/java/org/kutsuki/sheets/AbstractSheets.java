package org.kutsuki.sheets;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.model.EmployeeModel;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CopySheetToAnotherSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Common methods shared by all Helpers.
 * 
 * @author MatchaGreen
 */
public abstract class AbstractSheets extends AbstractGoogle {
    private static final String FORMULA_CLOSE = "\"},";
    private static final String FORMULA_OPEN = "\"formulaValue\":\"";
    private static final String MAIN_SHEET_ID = "1AGzsuTlo03umh2e7bGsRV0CTwo6hY9KNlViABVSjj3g";
    private static final String MAIN_RANGE = "Calculator!A2:G";
    private static final String TITLE = "title";
    private static final String USER_ENTERED = "USER_ENTERED";

    private Map<Integer, EmployeeModel> employeeMap;
    private Robot robot;
    private Sheets sheets;

    /**
     * Default Constructor
     */
    public AbstractSheets() {
	try {
	    this.employeeMap = new HashMap<Integer, EmployeeModel>();
	    this.robot = new Robot();
	    this.sheets = new Sheets.Builder(getTransport(), getJsonFactory(), getCredentials())
		    .setApplicationName(getApplicationName()).build();
	} catch (IOException | AWTException e) {
	    throw new IllegalArgumentException(e);
	}

	// Interns
	this.employeeMap.put(-2144727510, new EmployeeModel("J", "Diet", "10.00"));
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
    public EmployeeModel getEmployee(int key) {
	return employeeMap.get(key);
    }

    /**
     * Gets sorted list of Employees.
     * 
     * @return list of Employees.
     */
    public List<EmployeeModel> getEmployeeList() {
	List<EmployeeModel> employeeList = new ArrayList<EmployeeModel>(employeeMap.values());
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

    public String getFormulaClose() {
	return FORMULA_CLOSE;
    }

    public String getFormulaOpen() {
	return FORMULA_OPEN;
    }

    public int getSheetId(String spreadsheetId, String search) {
	int id = -1;
	int retries = 0;

	while (id == -1) {
	    try {
		Spreadsheet spreadsheet = sheets.spreadsheets().get(spreadsheetId).execute();

		Iterator<Sheet> itr = spreadsheet.getSheets().iterator();
		while (id == -1 && itr.hasNext()) {
		    Sheet sheet = itr.next();

		    if (StringUtils.contains(sheet.getProperties().getTitle(), search)) {
			id = sheet.getProperties().getSheetId();
		    }
		}
	    } catch (IOException e) {
		retries++;
		System.out.println("Retrying " + retries + "...");

		if (retries == 10) {
		    e.printStackTrace();
		}

		delay(1000);
	    }
	}

	return id;
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
	    EmployeeModel employee = new EmployeeModel(rowList.get(i));

	    // TODO Remove special case MD
	    if (employee.getFullName().hashCode() != 284331424) {
		employeeMap.put(employee.getFullName().hashCode(), employee);
	    }

	    i++;
	}

	System.out.println("Parsed " + employeeMap.size() + " employees!");
    }

    /**
     * Read Google sheet.
     * 
     * @param id    ID of Google Sheet.
     * @param range Range of Google Sheet.
     * @return cell data.
     */
    public List<List<Object>> readSheet(String id, String range) {
	int retries = 0;
	List<List<Object>> result = null;

	while (result == null) {
	    try {
		ValueRange response = sheets.spreadsheets().values().get(id, range).execute();
		result = response.getValues();
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

    /**
     * Read Google sheet.
     * 
     * @param id    ID of Google Sheet.
     * @param range Range of Google Sheet.
     * @return row data.
     */
    public List<RowData> readRowData(String id, String range) {
	int retries = 0;
	List<RowData> result = null;

	while (result == null) {
	    try {
		List<String> ranges = Collections.singletonList(range);
		Spreadsheet sheet = sheets.spreadsheets().get(id).setRanges(ranges).setIncludeGridData(true).execute();
		GridData gridData = sheet.getSheets().get(0).getData().get(0);

		result = gridData.getRowData();
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

    /**
     * Writes to Google Sheet.
     * 
     * @param id    ID of Google Sheet.
     * @param range Range of Google Sheet.
     * @param body  Data to write.
     */
    public void writeSheet(String id, String range, ValueRange body) {
	boolean completed = false;
	int retries = 0;

	while (!completed) {
	    try {
		sheets.spreadsheets().values().update(id, range, body).setValueInputOption(USER_ENTERED).execute();
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

    public void copySheet(String src, int sheetId, String dest) {
	boolean completed = false;
	int retries = 0;

	while (!completed) {
	    try {
		CopySheetToAnotherSpreadsheetRequest content = new CopySheetToAnotherSpreadsheetRequest();
		content.setDestinationSpreadsheetId(dest);
		sheets.spreadsheets().sheets().copyTo(src, sheetId, content).execute();
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

	System.out.println("Sheet Copied! " + dest);
    }

    public void updateSheetTitle(String spreadsheetId, int sheetId, String title) {
	boolean completed = false;
	int retries = 0;

	while (!completed) {
	    try {
		UpdateSheetPropertiesRequest uspr = new UpdateSheetPropertiesRequest();
		SheetProperties properties = new SheetProperties();
		properties.setSheetId(sheetId);
		properties.setTitle(title);
		uspr.setFields(TITLE);
		uspr.setProperties(properties);
		Request request = new Request();
		request.setUpdateSheetProperties(uspr);
		BatchUpdateSpreadsheetRequest busr = new BatchUpdateSpreadsheetRequest();
		busr.setRequests(Collections.singletonList(request));
		sheets.spreadsheets().batchUpdate(spreadsheetId, busr).execute();
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
}
