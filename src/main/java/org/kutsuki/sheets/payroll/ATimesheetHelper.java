package org.kutsuki.sheets.payroll;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractTimesheet;
import org.kutsuki.sheets.model.AbstractTimesheetModel;
import org.kutsuki.sheets.model.TimesheetModel;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Parses timesheet csv and updates the Main sheet.
 * 
 * @author MatchaGreen
 */
public class ATimesheetHelper extends AbstractTimesheet {
    private static final String NAME_RANGE = "Calculator!A2:B";
    private static final String WRITE_RANGE = "Calculator!C2:F";

    private LocalDate validate;

    /**
     * Top level runner
     */
    @Override
    public void run() {
	try {
	    parseMondaySheet(false);
	    parseCsv();
	    updateMainSheet();
	} catch (IOException e) {
	    throw new IllegalArgumentException("Unable to parse CSV file.", e);
	}
    }

    @Override
    public AbstractTimesheetModel newTimesheet(String fullName) {
	return new TimesheetModel(fullName);
    }

    @Override
    public LocalDate getLastDate() {
	return getEndDate();
    }

    @Override
    public LocalDate getValidationPeriod() {
	if (validate == null) {
	    validate = getEndDate().minusDays(4);
	}

	return validate;
    }

    /**
     * Updates the Main sheet with timesheet data.
     */
    private void updateMainSheet() {
	List<List<Object>> rowList = readSheet(getMainSheetId(), NAME_RANGE);

	int i = 0;
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	while (i < rowList.size() && !rowList.get(i).isEmpty()) {
	    List<Object> payrollList = new ArrayList<Object>();
	    TimesheetModel timesheet = (TimesheetModel) getTimesheetMap().get(createKey(rowList.get(i)));

	    if (timesheet != null && timesheet.isValid()) {
		payrollList.add(timesheet.getRegularPay().toString());
		payrollList.add(emptyIfZero(timesheet.getBD()));
		payrollList.add(emptyIfZero(timesheet.getGA()));
		payrollList.add(emptyIfZero(timesheet.getSickPay()));
	    } else {
		payrollList.add(StringUtils.EMPTY);
		payrollList.add(StringUtils.EMPTY);

		if (timesheet != null) {
		    payrollList.add(emptyIfZero(timesheet.getGA()));
		} else {
		    payrollList.add(StringUtils.EMPTY);
		}

		payrollList.add(StringUtils.EMPTY);
	    }

	    writeRowList.add(payrollList);
	    i++;
	}

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(getMainSheetId(), WRITE_RANGE, body);

	System.out.println("Main Sheet updated with hours!");
    }

    /**
     * Parses full name from Main sheet
     * 
     * @param list List of names.
     * @return Employee full name's hash code.
     */
    private int createKey(List<Object> list) {
	StringBuilder sb = new StringBuilder();
	if (list.size() == 2) {
	    sb.append(list.get(0));
	    sb.append(StringUtils.SPACE);
	    sb.append(list.get(1));
	}

	return sb.toString().hashCode();
    }

    /**
     * Sets string value to empty if it's zero.
     * 
     * @param bd The number of hours.
     * @return String value of number of values.
     */
    private String emptyIfZero(BigDecimal bd) {
	String result = StringUtils.EMPTY;

	if (bd.compareTo(BigDecimal.ZERO) != 0) {
	    result = bd.toString();
	}

	return result;
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	ATimesheetHelper helper = new ATimesheetHelper();
	helper.run();
    }
}
