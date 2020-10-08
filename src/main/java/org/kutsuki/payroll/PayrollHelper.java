package org.kutsuki.payroll;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.services.sheets.v4.model.ValueRange;

public class PayrollHelper extends AbstractSheets {
    private static final String ID = "1AGzsuTlo03umh2e7bGsRV0CTwo6hY9KNlViABVSjj3g";
    private static final String RANGE = "Calculator!A2:G";

    private List<Employee> employeeList;

    public PayrollHelper() {
	this.employeeList = new ArrayList<Employee>();

	// Interns
	this.employeeList.add(new Employee("Dietrich", "10.00"));
    }

    public void run() {
	try {
	    ValueRange response = getSheets().spreadsheets().values().get(ID, RANGE).execute();
	    parseResponse(response);
	    keyIn();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void parseResponse(ValueRange response) {
	List<List<Object>> values = response.getValues();

	int i = 0;
	while (i < values.size() && values.get(i).size() > 0) {
	    Employee employee = new Employee(values.get(i));

	    // Remove special case
	    if (!employee.getLastName().equals("Defenthaler")) {
		employeeList.add(employee);
	    }

	    i++;
	}
    }

    private void keyIn() {
	// inital time to alt-tab
	delay(6000);

	Collections.sort(employeeList);
	for (Employee employee : employeeList) {
	    if (!employee.isSkip()) {
		keyPress(KeyEvent.VK_TAB);
		keyPress(KeyEvent.VK_TAB);
		keyIn(employee.getRegularPay());

		if (employee.isPartner()) {
		    keyPress(KeyEvent.VK_TAB);
		    keyIn(employee.getBusinessDevelopment());

		    keyPress(KeyEvent.VK_TAB);
		    keyIn(employee.getGeneralAdmin());
		}

		keyPress(KeyEvent.VK_TAB);
		keyIn(employee.getSickPay());

		keyPress(KeyEvent.VK_TAB);
		keyPress(KeyEvent.VK_TAB);
		keyPress(KeyEvent.VK_TAB);
	    } else {
		keyPress(KeyEvent.VK_SPACE);
		keyPress(KeyEvent.VK_TAB);
		keyPress(KeyEvent.VK_TAB);
	    }
	}

    }

    public static void main(String[] args) {
	PayrollHelper helper = new PayrollHelper();
	helper.run();
    }
}
