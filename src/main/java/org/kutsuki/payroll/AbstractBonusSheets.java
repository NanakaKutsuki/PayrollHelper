package org.kutsuki.payroll;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.sheets.v4.model.ValueRange;

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
    private Map<String, String> mondayMap;
    private String payDate;

    /**
     * Keys in bonuses.
     */
    @Override
    public void keyIn() {
	Collections.sort(getEmployeeList());
	for (Employee employee : getEmployeeList()) {
	    if (!employee.isSkip() && !employee.isPartner() && employee.isBonus()) {
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
     * 
     * @throws IOException Exception from GoogleSheets API.
     */
    public void parseMondaySheet(boolean full) throws IOException {
	ValueRange response = getSheets().spreadsheets().values().get(getMondaySheetId(), MONDAY_RANGE).execute();
	List<List<Object>> rowList = response.getValues();
	this.mondayMap = new HashMap<String, String>();
	this.nextRow = rowList.size() + 1;
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
