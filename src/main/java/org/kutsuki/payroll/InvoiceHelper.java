package org.kutsuki.payroll;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.payroll.model.AbstractTimesheetModel;
import org.kutsuki.payroll.model.InvoiceModel;

import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

public class InvoiceHelper extends AbstractTimesheet {
    private static final String COVID = "COVID";
    private static final String FORMULA_VALUE = "\"formulaValue\":\"";
    private static final String INVOICE_ID = "1IER8vbOZpru8fjEkOMVRyObaMgIDM2yKMAUk6jJH9uI";
    private static final String ITSM_ALLIANCE = "ITSM Alliance";
    private static final String NUMBER_VALUE = "\"userEnteredValue\":{\"numberValue\":";
    private static final String STRING_VALUE = "\"userEnteredValue\":{\"stringValue\":\"";
    private static final String SUM = "=SUM(";

    private static final String TTO_14_RANGE = "TTO 14!E2:F2";
    private static final String TTO_14_SERVICE = "ITSM Alliance EIT Ops TTO-014-2020";
    private static final String TTO_14C_RANGE = "TTO 14C!E2:F2";
    private static final String TTO_14C_SERVICE = "ITSM Alliance EIT Ops TTO-014C-2020 - COVID";
    private static final String TTO_16_RANGE = "TTO 16!E2:F2";
    private static final String TTO_16_SERVICE = "ITSM Alliance EIT Ops TTO-016-2020";
    private static final String TTO_16C_RANGE = "TTO 16C!E2:F2";
    private static final String TTO_16C_SERVICE = "ITSM Alliance EIT Ops TTO-016C-2020 - COVID";
    private static final String TTO_20_RANGE = "TTO 20!E2:F2";
    private static final String TTO_20_SERVICE = "ITSM Alliance EIT Ops TTO-020-2020";
    private static final String TTO_21_RANGE = "TTO 21!E2:F3";
    private static final String TTO_21_SERVICE = "ITSM Alliance EIT Ops TTO-021-2020";
    private static final String TTO_21C_RANGE = "TTO 21C!E2:F3";
    private static final String TTO_21C_SERVICE = "ITSM Alliance EIT Ops TTO-021C-2020 - COVID";
    private static final String TTO_24_RANGE = "TTO 24!E2:F4";
    private static final String TTO_24_SERVICE = "ITSM Alliance EIT Ops TTO-024-2020";
    private static final String TTO_24C_RANGE = "TTO 24C!E2:F4";
    private static final String TTO_24C_SERVICE = "ITSM Alliance EIT Ops TTO-024C-2020 - COVID";
    private static final String TTO_26_RANGE = "TTO 26!E2:F2";
    private static final String TTO_26_SERVICE = "ITSM Alliance EIT Ops TTO-026-2020";
    private static final String TTO_26C_RANGE = "TTO 26C!E2:F2";
    private static final String TTO_26C_SERVICE = "ITSM Alliance EIT Ops TTO-026C-2020 - COVID";
    private static final String SPENDING_PLAN_RANGE = "Spending Plan!C2:R10";
    private static final String TIME_OFF_RANGE = "Time Off!I2:J2";

    private LocalDate lastDate;
    private LocalDate validate;

    /**
     * Constructor
     */
    public InvoiceHelper() {
	LocalDate date = LocalDate.now();
	// TODO remove debug
	// if (date.getDayOfMonth() < 5) {
	date = date.minusMonths(1);
	// }

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
	    // updateITSM(TTO_14_RANGE, TTO_14_SERVICE, 91890592);
	    // updateITSM(TTO_14C_RANGE, TTO_14C_SERVICE, 91890592);
	    // updateITSM(TTO_16_RANGE, TTO_16_SERVICE, 812608982);
	    // updateITSM(TTO_16C_RANGE, TTO_16C_SERVICE, 812608982);
	    // updateITSM(TTO_20_RANGE, TTO_20_SERVICE, 1973893852);
	    // updateITSM(TTO_21_RANGE, TTO_21_SERVICE, 2023693587, -1747439098);
	    // updateITSM(TTO_21C_RANGE, TTO_21C_SERVICE, 2023693587, -1747439098);
	    // updateITSM(TTO_24_RANGE, TTO_24_SERVICE, -404564145, 798342352, -1301936526,
	    // 284331424);
	    // updateITSM(TTO_24C_RANGE, TTO_24C_SERVICE, -404564145, 798342352,
	    // -1301936526, 284331424);
	    // updateITSM(TTO_26_RANGE, TTO_26_SERVICE, 1973893852);
	    // updateITSM(TTO_26C_RANGE, TTO_26C_SERVICE, 1973893852);
	    updateEveryoneElse();
	    // updateSpendingPlan();
	    // updateTimeOffDates();
	} catch (IOException e) {
	    throw new IllegalStateException("Unable to parse CSV file.", e);
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
	List<RowData> rowData = readRowData(INVOICE_ID, range);

	List<String> formulaList = new ArrayList<String>();
	for (RowData rd : rowData) {
	    formulaList.add(StringUtils.substringBetween(rd.toString(), FORMULA_VALUE, Character.toString('"')));
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
	    String[] formulas = StringUtils.substringsBetween(rd.toString(), FORMULA_VALUE, Character.toString('"'));
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

			dataList.add(hours);
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
