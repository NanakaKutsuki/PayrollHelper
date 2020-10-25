package org.kutsuki.sheets;

import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kutsuki.sheets.model.EmployeeModel;

/**
 * Common methods shared by Bonus Helpers.
 * 
 * @author MatchaGreen
 */
public abstract class AbstractBonusSheets extends AbstractSheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String MONDAY_SHEET_ID = "1u45jYrE1NeVAMq3TvakW-ZJgW8cN_v7jB1qlTEFIPC8";
    private static final String MONDAY_RANGE = "Payroll!A:B";

    private int nextRow;
    private LocalDate endDate;
    private Map<String, String> mondayMap;
    private String payDate;

    /**
     * Keys in bonuses.
     */
    public void keyIn(int ms) {
	// inital time to alt-tab
	delay(ms);

	Collections.sort(getEmployeeList());
	for (EmployeeModel employee : getEmployeeList()) {
	    if (!employee.isPartner() && employee.isBonus()) {
		keyPress(KeyEvent.VK_TAB);
		keyPress(KeyEvent.VK_TAB);

		keyIn(employee.getBonus());

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
     * Get end date from Monday Sheet.
     * 
     * @return end date.
     */
    public LocalDate getEndDate() {
	return endDate;
    }

    /**
     * Get bonus change log.
     * 
     * @return monday map.
     */
    public Map<String, String> getMondayMap() {
	return mondayMap;
    }

    /**
     * Get Monday sheet id.
     * 
     * @return monday sheet id.
     */
    public String getMondaySheetId() {
	return MONDAY_SHEET_ID;
    }

    /**
     * Get next available row on Monday sheet.
     * 
     * @return next available row.
     */
    public int getMondayNextRow() {
	return nextRow;
    }

    /**
     * Get pay date from Monday Sheet.
     * 
     * @return pay date.
     */
    public String getPayDate() {
	return payDate;
    }

    /**
     * Gets pay date from Monday sheet and finds next available row.
     */
    public void parseMondaySheet(boolean full) {
	List<List<Object>> rowList = readSheet(getMondaySheetId(), MONDAY_RANGE);
	this.mondayMap = new HashMap<String, String>();
	this.nextRow = rowList.size() + 1;
	this.endDate = LocalDate.parse(String.valueOf(rowList.get(1).get(1)), DTF);
	this.payDate = String.valueOf(rowList.get(2).get(1));

	// check if date is too old
	LocalDate date = LocalDate.parse(payDate, DTF);
	if (date.isBefore(LocalDate.now())) {
	    throw new IllegalArgumentException("Parsed pay date is too old! " + date);
	}

	if (!full) {
	    for (int i = 9; i < rowList.size(); i++) {
		mondayMap.put(escapeString(rowList.get(i).get(0)), escapeString(rowList.get(i).get(1)));
	    }
	}

	System.out.println("Got Pay Date! " + date);
    }
}
