package org.kutsuki.sheets.invoice;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractTimesheet;
import org.kutsuki.sheets.model.AbstractTimesheetModel;
import org.kutsuki.sheets.model.InvoiceModel;

import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

public class InvoiceHelper extends AbstractTimesheet {
    private static final String INVOICE_ID = "1IER8vbOZpru8fjEkOMVRyObaMgIDM2yKMAUk6jJH9uI";
    private static final String MONTH_ENDING_RANGE = "!B7";
    private static final String NAME_RANGE = "!B10:C";
    private static final String SPENDING_PLAN_ID = "1C5qfRsv1KrlhnuLIXUz2oCNFPDLUNdA2jXoBeetSuZ8";
    private static final String ITSM_ALLIANCE = "ITSM Alliance";
    private static final String STRING_VALUE = "\"userEnteredValue\":{\"stringValue\":\"";

    private static final String TTO_14_RANGE = "TTO 14 2021!E2:F6";
    private static final String TTO_14_SERVICE = "ITSM Alliance EIT Ops TTO-014-2021";
    private static final String TTO_16_RANGE = "TTO 16 2021!E2:F3";
    private static final String TTO_16_SERVICE = "ITSM Alliance EIT Ops TTO-016-2021";
    private static final String TTO_20_RANGE = "TTO 20 2021!E2:F2";
    private static final String TTO_20_SERVICE = "ITSM Alliance EIT Ops TTO-020-2021";
    private static final String TTO_21_2021_RANGE = "TTO 21 2021!E2:F2";
    private static final String TTO_21_2021_SERVICE = "ITSM Alliance EIT Ops TTO-021-2021";
    private static final String TTO_26_2021_RANGE = "TTO 26 2021!E2:F2";
    private static final String TTO_26_2021_SERVICE = "ITSM Alliance EIT Ops TTO-026-2021";
    private static final String EVERYBODY_ELSE_RANGE = "Everybody Else!A2:C";
    private static final String TIME_OFF_RANGE = "Time Off!I2:J2";

    private LocalDate lastDate;
    private LocalDate validate;
    private Map<Integer, String> monthColumnMap;

    /**
     * Constructor
     */
    public InvoiceHelper() {
	LocalDate date = LocalDate.now();

	if (date.getDayOfMonth() < 5) {
	    date = date.minusMonths(1);
	}

	this.lastDate = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));
	this.validate = lastDate.minusDays(4);

	this.monthColumnMap = new HashMap<Integer, String>();
	this.monthColumnMap.put(1, "E");
	this.monthColumnMap.put(2, "G");
	this.monthColumnMap.put(3, "I");
	this.monthColumnMap.put(4, "K");
	this.monthColumnMap.put(5, "M");
	this.monthColumnMap.put(6, "O");
	this.monthColumnMap.put(7, "Q");
	this.monthColumnMap.put(8, "S");
	this.monthColumnMap.put(9, "U");
	this.monthColumnMap.put(10, "W");
	this.monthColumnMap.put(11, "Y");
	this.monthColumnMap.put(12, "AA");
    }

    /**
     * Top level runner
     */
    @Override
    public void run() {
	try {
	    parseCsv();
	    updateITSM(TTO_14_RANGE, TTO_14_SERVICE, 91890592, -404564145, 798342352, -1301936526, 284331424);
	    updateITSM(TTO_16_RANGE, TTO_16_SERVICE, 812608982, 1910574595);
	    updateITSM(TTO_20_RANGE, TTO_20_SERVICE, 1973893852);
	    updateITSM(TTO_21_2021_RANGE, TTO_21_2021_SERVICE, 2023693587);
	    updateITSM(TTO_26_2021_RANGE, TTO_26_2021_SERVICE, 1973893852);
	    updateEveryoneElse();
	    updateTimeOffDates();
	} catch (IOException e) {
	    throw new IllegalArgumentException("Unable to parse CSV file.", e);
	}
    }

    @Override
    public AbstractTimesheetModel newTimesheet(String fullName) {
	return new InvoiceModel(fullName);
    }

    @Override
    public LocalDate getLastDate() {
	return lastDate;
    }

    @Override
    public LocalDate getValidationPeriod() {
	return validate;
    }

    private void updateITSM(String range, String service, int... keys) {
	System.out.println("Working on: " + service);

	List<RowData> rowData = readRowData(INVOICE_ID, range);

	List<String> formulaList = new ArrayList<String>();
	for (RowData rd : rowData) {
	    formulaList.add(StringUtils.substringBetween(rd.toString(), getFormulaOpen(), getFormulaClose()));
	}

	List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	for (int i = 0; i < keys.length; i++) {
	    InvoiceModel model = (InvoiceModel) getTimesheetMap().get(keys[i]);

	    String hours = StringUtils.EMPTY;
	    StringBuilder sb = new StringBuilder();

	    if (model.isValid()) {
		hours = model.getHours(service);
		sb.append(formulaList.get(i));
		sb.append('+');
		sb.append(hours);
	    } else {
		sb.append(formulaList.get(i));
	    }

	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(hours);
	    dataList.add(sb.toString());
	    writeRowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(INVOICE_ID, range, body);

	updateSpendingPlan(StringUtils.substringAfterLast(service, StringUtils.SPACE), keys.length);
    }

    private void updateSpendingPlan(String sheet, int rows) {
	// update Month Ending
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	List<Object> dataList = new ArrayList<Object>();
	dataList.add(lastDate.toString());
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(SPENDING_PLAN_ID, sheet + MONTH_ENDING_RANGE, body);

	// update Hours
	writeRowList.clear();
	List<List<Object>> rowData = readSheet(SPENDING_PLAN_ID, sheet + NAME_RANGE + (10 + rows - 1));
	for (List<Object> data : rowData) {
	    String name = data.get(1) + StringUtils.SPACE + data.get(0);
	    InvoiceModel model = (InvoiceModel) getTimesheetMap().get(name.hashCode());

	    dataList = new ArrayList<Object>();
	    if (model.isValid()) {
		BigDecimal hours = BigDecimal.ZERO;
		for (Entry<String, BigDecimal> entry : model.getHoursMap().entrySet()) {
		    if (StringUtils.endsWith(entry.getKey(), sheet)) {
			hours = hours.add(entry.getValue());
		    }
		}

		dataList.add(hours);
	    } else {
		dataList.add(StringUtils.EMPTY);
	    }

	    writeRowList.add(dataList);
	}

	body = new ValueRange();
	body.setValues(writeRowList);

	StringBuilder range = new StringBuilder();
	range.append(sheet);
	range.append('!');
	range.append(monthColumnMap.get(lastDate.getMonthValue()));
	range.append(10);
	range.append(':');
	range.append(monthColumnMap.get(lastDate.getMonthValue()));
	range.append(10 + rows - 1);
	writeSheet(SPENDING_PLAN_ID, range.toString(), body);
    }

    private void updateEveryoneElse() {
	List<RowData> rowData = readRowData(INVOICE_ID, EVERYBODY_ELSE_RANGE);
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	for (RowData rd : rowData) {
	    String name = StringUtils.substringBetween(rd.toString(), STRING_VALUE, Character.toString('"'));
	    String formula = StringUtils.substringBetween(rd.toString(), getFormulaOpen(), getFormulaClose());

	    InvoiceModel model = (InvoiceModel) getTimesheetMap().get(name.hashCode());

	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(name);

	    if (model.isValid()) {
		BigDecimal hours = BigDecimal.ZERO;
		for (Entry<String, BigDecimal> entry : model.getHoursMap().entrySet()) {
		    if (!StringUtils.startsWith(entry.getKey(), ITSM_ALLIANCE)) {
			hours = hours.add(entry.getValue());
		    }
		}

		dataList.add(hours);

		StringBuilder sb = new StringBuilder();
		sb.append(formula);
		sb.append('+');
		sb.append(hours);
		dataList.add(sb.toString());
	    } else {
		dataList.add(StringUtils.EMPTY);
		dataList.add(formula);
	    }

	    writeRowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);

	writeSheet(INVOICE_ID, EVERYBODY_ELSE_RANGE, body);

	System.out.println("Finished with Everybody Else!");
    }

    private void updateTimeOffDates() {
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	List<Object> dataList = new ArrayList<Object>();
	dataList.add(lastDate.withDayOfMonth(1).toString());
	dataList.add(lastDate.toString());
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);

	writeSheet(INVOICE_ID, TIME_OFF_RANGE, body);

	System.out.println("Updated Time Off Dates!");
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	InvoiceHelper helper = new InvoiceHelper();
	helper.run();
    }
}
