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

    public BonusHelper() {
	this.mondayMap = new HashMap<String, String>();
    }

    public void run() {

	try {
	    // parsePayDate();
	    parseEmployees();
	    updateBonusSheets();
	    // writeStuff();
	    // keyIn();
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private void parsePayDate() throws IOException {
	ValueRange response = getSheets().spreadsheets().values().get(MONDAY_SHEET_ID, MONDAY_RANGE).execute();
	List<List<Object>> rowList = response.getValues();
	this.nextRow = rowList.size() + 1;
	this.payDate = String.valueOf(rowList.get(2).get(1));

	LocalDate date = LocalDate.parse(payDate, DTF);
	if (date.isBefore(LocalDate.now())) {
	    throw new IllegalArgumentException("Parsed pay date is too old! " + date);
	}
    }

    private void updateBonusSheets() throws IOException {
	for (Entry<Integer, String> entry : getNameIdMap().entrySet()) {
	    System.out.println(entry.getKey() + " Checking...");
	    ValueRange response = getSheets().spreadsheets().values().get(entry.getValue(), BONUS_RANGE).execute();
	    List<List<Object>> rowList = response.getValues();
	    int nextBousRow = rowList.size() + 1;

	    BigDecimal owed = parseBigDecimal(rowList.get(2).get(2));
	    if (owed.compareTo(BigDecimal.ZERO) != 0) {
		BigDecimal bonus = parseBigDecimal(getEmployee(entry.getKey()).getBonus());

		String checksum = StringUtils.replace(CHECKSUM, "BBB", "B" + nextBousRow);
		checksum = StringUtils.replace(checksum, "CCC", "C" + nextBousRow);

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
		    mondayMap.put(getEmployee(entry.getKey()).getName(), payout.toString());
		} else {
		    mondayMap.put(getEmployee(entry.getKey()).getName(), getZero());
		}

		System.out.println(mondayMap);

		ValueRange body = new ValueRange();
		body.setValues(writeRowList);
		getSheets().spreadsheets().values()
			.update(entry.getValue(), LocalDate.now().getYear() + "!A" + nextBousRow, body)
			.setValueInputOption(USER_ENTERED).execute();

		System.out.println(entry.getKey() + " Updated!");
	    }
	    delay(1000);

	}
    }

    private BigDecimal parseBigDecimal(Object o) {
	String value = String.valueOf(o);
	value = StringUtils.remove(value, '$');
	value = StringUtils.remove(value, ',');
	return new BigDecimal(value);
    }

    private void writeStuff() throws IOException {
	List<List<Object>> rowList = new ArrayList<List<Object>>();
	for (int i = 0; i < 4; i++) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add("Mr. Cool" + i);
	    dataList.add(Integer.toString(i));
	    rowList.add(dataList);
	}

	for (int i = 10 + rowList.size(); i < nextRow; i++) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add("");
	    dataList.add("");
	    rowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(rowList);

	getSheets().spreadsheets().values().update(MONDAY_SHEET_ID, MONDAY_WRITE_RANGE, body)
		.setValueInputOption(USER_ENTERED).execute();
    }

    private void keyIn() {
	// inital time to alt-tab
	delay(3000);

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
    }

    private boolean isBonus(Employee employee) {
	return !employee.getBonus().equals(getZero());
    }

    public static void main(String[] args) {
	BonusHelper helper = new BonusHelper();
	helper.run();
    }
}
