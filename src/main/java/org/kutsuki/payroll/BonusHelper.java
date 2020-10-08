package org.kutsuki.payroll;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.sheets.v4.model.ValueRange;

public class BonusHelper extends AbstractBonusSheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String BONUS = "Bonus";
    private static final String BONUS_RANGE = LocalDate.now().getYear() + "!A:D";
    private static final String CHECKSUM = "=IF(OR(AND(BBB<>\"Bonus\",CCC<0),AND(OR(BBB=\"Bonus\",BBB=\"Carryover\",BBB=\"Sick Leave\"),CCC>0)),\"OK\",\"BAD\")";
    private static final String MONDAY_SHEET_ID = "1u45jYrE1NeVAMq3TvakW-ZJgW8cN_v7jB1qlTEFIPC8";
    private static final String MONDAY_RANGE = "Payroll!A:B";
    private static final String MONDAY_WRITE_RANGE = "Payroll!A10";
    private static final String PAYOUT = "Payout";
    private static final String USER_ENTERED = "USER_ENTERED";

    private int nextRow;
    private String payDate;
    private Map<String, String> mondayMap;

    // run
    @Override
    public void run() {
	try {
	    parsePayDate();
	    parseEmployees();
	    updateBonusSheets();
	    updateMondaySheet();
	    keyIn();
	} catch (IOException e) {
	    throw new IllegalStateException(e);
	}
    }

    // gets paydate and next available row
    private void parsePayDate() throws IOException {
	ValueRange response = getSheets().spreadsheets().values().get(MONDAY_SHEET_ID, MONDAY_RANGE).execute();
	List<List<Object>> rowList = response.getValues();
	this.mondayMap = new HashMap<String, String>();
	this.nextRow = rowList.size() + 1;
	this.payDate = String.valueOf(rowList.get(2).get(1));

	// check if date is too old
	LocalDate date = LocalDate.parse(payDate, DTF);
	if (date.isBefore(LocalDate.now())) {
	    throw new IllegalArgumentException("Parsed pay date is too old! " + date);
	}

	System.out.println("Got Pay Date! " + date);
    }

    // checks all bonus sheets and updates if necessary
    private void updateBonusSheets() throws IOException {
	for (Entry<Integer, String> entry : getNameIdMap().entrySet()) {
	    System.out.println(getEmployee(entry.getKey()).getName() + " Checking...");
	    ValueRange response = getSheets().spreadsheets().values().get(entry.getValue(), BONUS_RANGE).execute();
	    List<List<Object>> rowList = response.getValues();
	    int nextBonusRow = rowList.size() + 1;

	    BigDecimal owed = parseBigDecimal(rowList.get(2).get(2));
	    if (owed.compareTo(BigDecimal.ZERO) != 0) {
		BigDecimal bonus = parseBigDecimal(getEmployee(entry.getKey()).getBonus());

		String checksum = StringUtils.replace(CHECKSUM, "BBB", "B" + nextBonusRow);
		checksum = StringUtils.replace(checksum, "CCC", "C" + nextBonusRow);

		List<List<Object>> writeRowList = new ArrayList<List<Object>>();
		List<Object> bonusList = new ArrayList<Object>();
		bonusList.add(payDate);
		bonusList.add(BONUS);
		bonusList.add(bonus);
		bonusList.add(checksum);
		writeRowList.add(bonusList);

		BigDecimal payout = bonus.add(owed);
		if (payout.compareTo(BigDecimal.ZERO) == 1 && entry.getKey() != 1373398698
			&& entry.getKey() != 1649047938) {
		    List<Object> payoutList = new ArrayList<Object>();
		    payoutList.add(payDate);
		    payoutList.add(PAYOUT);
		    payoutList.add(payout.negate());
		    payoutList.add(checksum);
		    writeRowList.add(payoutList);
		} else {
		    payout = BigDecimal.ZERO;
		}

		mondayMap.put(getEmployee(entry.getKey()).getName(), payout.toString());
		getEmployee(entry.getKey()).setBonus(payout.toString());

		ValueRange body = new ValueRange();
		body.setValues(writeRowList);
		getSheets().spreadsheets().values()
			.update(entry.getValue(), LocalDate.now().getYear() + "!A" + nextBonusRow, body)
			.setValueInputOption(USER_ENTERED).execute();

		System.out.println(entry.getKey() + " updated to " + payout.toString());
	    }

	    delay(200);
	}
    }

    // parses String object into BigDecimal
    private BigDecimal parseBigDecimal(Object o) {
	String value = String.valueOf(o);
	value = StringUtils.remove(value, '$');
	value = StringUtils.remove(value, ',');
	return new BigDecimal(value);
    }

    // updates monday sheet with change log
    private void updateMondaySheet() throws IOException {
	List<List<Object>> rowList = new ArrayList<List<Object>>();
	for (Entry<String, String> entry : mondayMap.entrySet()) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(entry.getKey());
	    dataList.add(entry.getValue());
	    rowList.add(dataList);
	}

	for (int i = 10 + rowList.size(); i < nextRow; i++) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(StringUtils.EMPTY);
	    dataList.add(StringUtils.EMPTY);
	    rowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(rowList);

	getSheets().spreadsheets().values().update(MONDAY_SHEET_ID, MONDAY_WRITE_RANGE, body)
		.setValueInputOption(USER_ENTERED).execute();

	System.out.println("Monday Special Sheet updated with " + mondayMap.size() + " changes!");
    }

    // keys in bonuses
    @Override
    public void keyIn() {
	Collections.sort(getEmployeeList());
	for (Employee employee : getEmployeeList()) {
	    if (!employee.isSkip() && !employee.isPartner() && isBonus(employee)) {
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

    // isBonus
    private boolean isBonus(Employee employee) {
	return !employee.getBonus().equals(getZero());
    }

    // main
    public static void main(String[] args) {
	BonusHelper helper = new BonusHelper();
	helper.run();
    }
}
