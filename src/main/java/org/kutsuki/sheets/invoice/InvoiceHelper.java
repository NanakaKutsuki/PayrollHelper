package org.kutsuki.sheets.invoice;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractTimesheet;
import org.kutsuki.sheets.model.AbstractTimesheetModel;
import org.kutsuki.sheets.model.InvoiceModel;

import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

public class InvoiceHelper extends AbstractTimesheet {
    private static final String COVID = "COVID";
    private static final String INVOICE_ID = "1IER8vbOZpru8fjEkOMVRyObaMgIDM2yKMAUk6jJH9uI";
    private static final String ITSM_ALLIANCE = "ITSM Alliance";
    private static final String NUMBER_VALUE = "\"userEnteredValue\":{\"numberValue\":";
    private static final String STRING_VALUE = "\"userEnteredValue\":{\"stringValue\":\"";
    private static final String SUM = "=SUM(";

    private static final String TTO_14_RANGE = "TTO 14 2021!E2:F6";
    private static final String TTO_14_SERVICE = "ITSM Alliance EIT Ops TTO-014-2021";
    private static final String TTO_16_RANGE = "TTO 16 2021!E2:F3";
    private static final String TTO_16_SERVICE = "ITSM Alliance EIT Ops TTO-016-2021";
    private static final String TTO_20_RANGE = "TTO 20 2021!E2:F2";
    private static final String TTO_20_SERVICE = "ITSM Alliance EIT Ops TTO-020-2021";
    private static final String TTO_21_2021_RANGE = "TTO 21 2021!E2:F2";
    private static final String TTO_21_2021_SERVICE = "ITSM Alliance EIT Ops TTO-021-2021";
    private static final String TTO_24_RANGE = "TTO 24 2020!E2:F5";
    private static final String TTO_24_SERVICE = "ITSM Alliance EIT Ops TTO-024-2020";
    private static final String TTO_26_2021_RANGE = "TTO 26 2021!E2:F2";
    private static final String TTO_26_2021_SERVICE = "ITSM Alliance EIT Ops TTO-026-2021";
    private static final String SPENDING_PLAN_RANGE = "Spending Plan!C2:R10";
    private static final String EVERYBODY_ELSE_RANGE = "Everybody Else!A2:C";
    private static final String TIME_OFF_RANGE = "Time Off!I2:J2";

    private LocalDate lastDate;
    private LocalDate validate;

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
	    updateITSM(TTO_24_RANGE, TTO_24_SERVICE, -404564145, 798342352, -1301936526, 284331424);
	    updateITSM(TTO_26_2021_RANGE, TTO_26_2021_SERVICE, 1973893852);
	    updateEveryoneElse();
	    updateSpendingPlan();
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
    }

    private void updateSpendingPlan() {
	List<RowData> rowData = readRowData(INVOICE_ID, SPENDING_PLAN_RANGE);
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	for (int i = 0; i < rowData.size(); i++) {
	    RowData rd = rowData.get(i);
	    String name = StringUtils.substringBetween(rd.toString(), STRING_VALUE, Character.toString('"'));
	    String[] values = StringUtils.substringsBetween(rd.toString(), NUMBER_VALUE, Character.toString('}'));
	    String[] formulas = StringUtils.substringsBetween(rd.toString(), getFormulaOpen(), getFormulaClose());
	    int month = (int) formulas[2].charAt(5) - 68;

	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(name);

	    for (int j = 0; j < values.length; j++) {
		if (j == month) {
		    InvoiceModel model = (InvoiceModel) getTimesheetMap().get(name.hashCode());

		    if (model.isValid()) {
			BigDecimal hours = BigDecimal.ZERO;
			for (Entry<String, BigDecimal> entry : model.getHoursMap().entrySet()) {
			    if (StringUtils.startsWith(entry.getKey(), ITSM_ALLIANCE)
				    && !StringUtils.endsWith(entry.getKey(), COVID)) {
				hours = hours.add(entry.getValue());
			    }
			}

			dataList.add(hours.toString());
		    } else {
			dataList.add(StringUtils.EMPTY);
		    }
		} else {
		    dataList.add(values[j]);
		}
	    }

	    dataList.add(updateActual(month, i));
	    dataList.add(formulas[1]);
	    dataList.add(updateProjected(month, i));

	    writeRowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);

	writeSheet(INVOICE_ID, SPENDING_PLAN_RANGE, body);

	System.out.println("Updated Spending Plan!");
    }

    private String updateActual(int month, int row) {
	StringBuilder sb = new StringBuilder();
	sb.append(SUM);
	sb.append('D');
	sb.append(row + 2);
	sb.append(':');
	sb.append((char) (month + 68));
	sb.append(row + 2);
	sb.append(')');
	return sb.toString();
    }

    private String updateProjected(int month, int row) {
	StringBuilder sb = new StringBuilder();
	sb.append(SUM);
	sb.append((char) (month + 69));
	sb.append(row + 2);
	sb.append(':');
	sb.append('O');
	sb.append(row + 2);
	sb.append(')');
	return sb.toString();
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
