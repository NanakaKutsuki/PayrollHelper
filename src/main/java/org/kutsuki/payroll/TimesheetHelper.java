package org.kutsuki.payroll;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.payroll.model.Timesheet;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Parses timesheet csv and updates the Main sheet.
 * 
 * @author MatchaGreen
 */
public class TimesheetHelper extends AbstractBonusSheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final File DESKTOP = new File("C:\\Users\\MatchaGreen\\Desktop\\");
    private static final String NAME_RANGE = "Calculator!A2:B";
    private static final String PENDING = "Pending";
    private static final String USER_REPORT = "UserReport_";
    private static final String WRITE_RANGE = "Calculator!C2:F";
    private static final String ZIP = ".zip";

    private Map<String, Timesheet> timesheetMap;

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
	    throw new IllegalStateException("Unable to parse CSV file.", e);
	}
    }

    /**
     * Finds zip file, read csv file within, and parses timesheet data.
     */
    private void parseCsv() throws IOException {
	boolean found = false;
	int i = 0;
	File[] files = DESKTOP.listFiles();
	this.timesheetMap = new HashMap<String, Timesheet>();
	LocalDate validate = getEndDate().minusDays(4);

	// go through desktop files
	while (i < files.length && !found) {
	    File file = files[i];

	    // find timesheet file
	    if (StringUtils.startsWith(file.getName(), USER_REPORT) && StringUtils.endsWith(file.getName(), ZIP)) {
		ZipFile zip = new ZipFile(file);
		InputStreamReader isr = new InputStreamReader(zip.getInputStream(zip.entries().nextElement()));
		BufferedReader br = new BufferedReader(isr);

		String line = br.readLine();
		while ((line = br.readLine()) != null) {
		    String[] splitLine = StringUtils.split(line, ',');
		    LocalDate date = LocalDate.parse(splitLine[1], DTF);
		    String fullName = createKey(StringUtils.remove(splitLine[2], '"'));
		    String customer = StringUtils.remove(splitLine[3], '"');
		    String service = StringUtils.remove(splitLine[4], '"');
		    BigDecimal hours = new BigDecimal(splitLine[9]);
		    String status = splitLine[10];

		    Timesheet timesheet = timesheetMap.get(fullName);
		    if (timesheet == null) {
			timesheet = new Timesheet(fullName);
			timesheetMap.put(fullName, timesheet);
		    }

		    if (date.isAfter(validate) && !status.equals(PENDING)) {
			timesheet.validate();
		    } else if (date.equals(getEndDate()) && status.equals(PENDING)) {
			timesheet.validate();
			System.out.println(timesheet.getFullName() + " - Status still pending!");
		    }

		    timesheet.addHours(customer, service, hours);
		}

		found = true;

		br.close();
		isr.close();
		zip.close();
	    }

	    i++;
	}
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
	    Timesheet timesheet = timesheetMap.get(createKey(rowList.get(i)));

	    if (timesheet != null && timesheet.isValid()) {
		payrollList.add(timesheet.getRegularPay().toString());
		payrollList.add(emptyIfZero(timesheet.getBusinessDevelopment()));
		payrollList.add(emptyIfZero(timesheet.getGeneralAdmin()));
		payrollList.add(emptyIfZero(timesheet.getSickPay()));
	    } else {
		payrollList.add(StringUtils.EMPTY);
		payrollList.add(StringUtils.EMPTY);
		payrollList.add(StringUtils.EMPTY);
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
     * Creates full name from worker line.
     * 
     * @param Employee full name with potentially a middle name.
     * @return Employee full name, without middle name.
     */
    private String createKey(String worker) {
	StringBuilder sb = new StringBuilder();
	sb.append(StringUtils.substringBefore(worker, StringUtils.SPACE));
	sb.append(StringUtils.SPACE);
	sb.append(StringUtils.substringAfterLast(worker, StringUtils.SPACE));
	return sb.toString();
    }

    /**
     * Parses full name from Main sheet
     * 
     * @param list List of names.
     * @return Employee full name.
     */
    private String createKey(List<Object> list) {
	StringBuilder sb = new StringBuilder();
	if (list.size() == 2) {
	    sb.append(list.get(0));
	    sb.append(StringUtils.SPACE);
	    sb.append(list.get(1));
	}

	return sb.toString();
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
	TimesheetHelper helper = new TimesheetHelper();
	helper.run();
    }
}
