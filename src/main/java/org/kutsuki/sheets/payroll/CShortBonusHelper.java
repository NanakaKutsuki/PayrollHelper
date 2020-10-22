package org.kutsuki.sheets.payroll;

import java.util.Map.Entry;

import org.kutsuki.sheets.AbstractBonusSheets;

/**
 * Automates keying into payroll system using values from the Main sheet and
 * overridden by Monday sheet.
 * 
 * @author MatchaGreen
 */
public class CShortBonusHelper extends AbstractBonusSheets {
    /**
     * Top level runner
     */
    @Override
    public void run() {
	parseMondaySheet(false);
	parseEmployees();
	updateEmployeeList();
	keyIn(5000);
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
	CShortBonusHelper helper = new CShortBonusHelper();
	helper.run();
    }
}
