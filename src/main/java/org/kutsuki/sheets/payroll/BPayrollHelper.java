package org.kutsuki.sheets.payroll;

import java.awt.event.KeyEvent;
import java.util.Collections;

import org.kutsuki.sheets.AbstractSheets;
import org.kutsuki.sheets.model.EmployeeModel;

/**
 * Automates keying into payroll system using values from the Main sheet and
 * overridden by Monday sheet.
 * 
 * @author MatchaGreen
 */
public class BPayrollHelper extends AbstractSheets {
    /**
     * Top level runner
     */
    @Override
    public void run() {
	parseEmployees();
	keyIn(5000);
    }

    /**
     * Keys in payroll
     */
    public void keyIn(int ms) {
	// inital time to alt-tab
	delay(ms);

	Collections.sort(getEmployeeList());
	for (EmployeeModel employee : getEmployeeList()) {
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

	System.out.println("Done keying in!");
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	BPayrollHelper helper = new BPayrollHelper();
	helper.run();
    }
}
