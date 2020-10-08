package org.kutsuki.payroll;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;

public class PayrollHelper extends AbstractSheets {
    public void run() {
	try {
	    parseEmployees();
	    keyIn();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void keyIn() {
	// inital time to alt-tab
	delay(6000);

	Collections.sort(getEmployeeList());
	for (Employee employee : getEmployeeList()) {
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
