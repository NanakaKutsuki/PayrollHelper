package org.kutsuki.payroll;

import java.io.IOException;
import java.util.Map.Entry;

/**
 * Automates keying into payroll system using values from the Main sheet and
 * overridden by Monday sheet.
 * 
 * @author MatchaGreen
 */
public class ShortBonusHelper extends AbstractBonusSheets {
    /**
     * Top level runner
     */
    @Override
    public void run() {
	try {
	    parseMondaySheet(false);
	    parseEmployees();
	    updateEmployeeList();
	    keyIn(5000);
	} catch (IOException e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Use values from Monday sheet to update the employee bonuses.
     */
    private void updateEmployeeList() {
	for (Entry<String, String> entry : getMondayMap().entrySet()) {
	    getEmployee(entry.getKey().hashCode()).setBonus(entry.getValue());
	}
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	ShortBonusHelper helper = new ShortBonusHelper();
	helper.run();
    }
}
