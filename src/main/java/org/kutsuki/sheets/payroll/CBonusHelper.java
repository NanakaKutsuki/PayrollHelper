package org.kutsuki.sheets.payroll;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractBonusSheets;
import org.kutsuki.sheets.model.AbstractTimesheetModel;
import org.kutsuki.sheets.model.EmployeeModel;
import org.kutsuki.sheets.model.TimesheetModel;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Automates updating biweekly payroll spreadsheets, logging changes into the
 * Monday Special sheet and keying into payroll system.
 * 
 * @author MatchaGreen
 */
public class CBonusHelper extends AbstractBonusSheets {
    private static final String BONUS = "Bonus";
    private static final String MONDAY_WRITE_RANGE = "Payroll!A10";
    private static final String PAYOUT = "Payout";

    /**
     * Top level runner
     */
    @Override
    public void run() {
	parseMondaySheet(true);
	parseEmployees();
	addPager();
	updateBonusSheets();
	updateMondaySheet();
	keyIn(0);
    }

    public void addPager() {
	// if pager is removed, remove abstracttimesheet

	try {
	    parseCsv();

	    for (AbstractTimesheetModel model : getTimesheetMap().values()) {
		TimesheetModel timesheet = (TimesheetModel) model;

		if (timesheet.getPagerDays() > 0) {

		    String id = getNameIdMap().get(timesheet.getFullName().hashCode());
		    List<List<Object>> rowList = readSheet(id, LocalDate.now().getYear() + getBonusRange());
		    int nextBonusRow = rowList.size() + 1;

		    StringBuilder pager = new StringBuilder();
		    pager.append('=');
		    pager.append(50);
		    pager.append('*');
		    pager.append(timesheet.getPagerDays());

		    List<List<Object>> writeRowList = new ArrayList<List<Object>>();

		    List<Object> bonusList = new ArrayList<Object>();
		    bonusList.add(getPayDate());
		    bonusList.add(BONUS);
		    bonusList.add(pager.toString());
		    bonusList.add(getChecksum(nextBonusRow));
		    writeRowList.add(bonusList);

		    ValueRange body = new ValueRange();
		    body.setValues(writeRowList);

		    StringBuilder range = new StringBuilder();
		    range.append(LocalDate.now().getYear());
		    range.append('!');
		    range.append('A');
		    range.append(nextBonusRow);

		    writeSheet(id, range.toString(), body);
		}
	    }
	} catch (IOException e) {
	    throw new IllegalArgumentException("Unable to parse CSV file.", e);
	}
    }

    /**
     * Keys in bonuses.
     */
    public void keyIn(int ms) {
	// inital time to alt-tab
	delay(ms);

	Collections.sort(getEmployeeList());
	for (EmployeeModel employee : getEmployeeList()) {
	    if (employee.isBonus()) {
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
     * Checks all bonus sheets and updates if necessary.
     */
    private void updateBonusSheets() {
	for (Entry<Integer, String> entry : getNameIdMap().entrySet()) {
	    EmployeeModel employee = getEmployee(entry.getKey());
	    System.out.println("Working on " + employee.getFullName() + "...");

	    List<List<Object>> rowList = readSheet(entry.getValue(), LocalDate.now().getYear() + getBonusRange());
	    int nextBonusRow = rowList.size() + 1;

	    BigDecimal owed = new BigDecimal(escapeString(rowList.get(2).get(2)));
	    if (owed.compareTo(BigDecimal.ZERO) != 0) {
		BigDecimal bonus = new BigDecimal(escapeString(employee.getBonus()));

		List<List<Object>> writeRowList = new ArrayList<List<Object>>();

		if (bonus.compareTo(BigDecimal.ZERO) == 1) {
		    List<Object> bonusList = new ArrayList<Object>();
		    bonusList.add(getPayDate());
		    bonusList.add(BONUS);
		    bonusList.add(bonus);
		    bonusList.add(getChecksum(nextBonusRow));
		    writeRowList.add(bonusList);
		}

		BigDecimal payout = bonus.add(owed);

		if (payout.compareTo(BigDecimal.ZERO) == 1) {
		    List<Object> payoutList = new ArrayList<Object>();
		    payoutList.add(getPayDate());
		    payoutList.add(PAYOUT);
		    payoutList.add(payout.negate());
		    payoutList.add(getChecksum(nextBonusRow + 1));
		    writeRowList.add(payoutList);
		} else {
		    payout = BigDecimal.ZERO;
		}

		getMondayMap().put(employee.getFullName(), payout.toString());
		employee.setBonus(payout.toString());

		ValueRange body = new ValueRange();
		body.setValues(writeRowList);

		StringBuilder range = new StringBuilder();
		range.append(LocalDate.now().getYear());
		range.append('!');
		range.append('A');
		range.append(nextBonusRow);

		writeSheet(entry.getValue(), range.toString(), body);

		System.out.println(employee.getFullName() + " updated to " + payout.toString());
	    }
	}
    }

    /**
     * Updates Monday sheet with change log.
     */
    private void updateMondaySheet() {
	List<List<Object>> rowList = new ArrayList<List<Object>>();
	for (Entry<String, String> entry : getMondayMap().entrySet()) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(entry.getKey());
	    dataList.add(entry.getValue());
	    rowList.add(dataList);
	}

	for (int i = 10 + rowList.size(); i < getMondayNextRow(); i++) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(StringUtils.EMPTY);
	    dataList.add(StringUtils.EMPTY);
	    rowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(rowList);
	writeSheet(getMondaySheetId(), MONDAY_WRITE_RANGE, body);

	System.out.println("Monday Special Sheet updated with " + getMondayMap().size() + " changes!");
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	CBonusHelper helper = new CBonusHelper();
	helper.run();

	// String name = "";
	// System.out.println(name.hashCode());
    }
}
