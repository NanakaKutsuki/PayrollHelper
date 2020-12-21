package org.kutsuki.sheets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.model.AbstractTimesheetModel;

public abstract class AbstractTimesheet extends AbstractMondaySheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final File DESKTOP = new File("C:\\Users\\MatchaGreen\\Desktop\\");
    private static final String PENDING = "Pending";
    private static final String USER_REPORT = "UserReport_";
    private static final String ZIP = ".zip";

    private Map<Integer, AbstractTimesheetModel> timesheetMap;

    public abstract AbstractTimesheetModel newTimesheet(String fullName);

    public abstract LocalDate getValidationPeriod();

    public abstract LocalDate getLastDate();

    /**
     * Finds zip file, read csv file within, and parses timesheet data.
     * 
     * @throws IOException problem with reading the file.
     */
    public void parseCsv() throws IOException {
	boolean found = false;
	int i = 0;
	File[] files = DESKTOP.listFiles();
	this.timesheetMap = new HashMap<Integer, AbstractTimesheetModel>();
	LocalDate lastDate = getLastDate();

	if (lastDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
	    lastDate = lastDate.minusDays(1);
	} else if (lastDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
	    lastDate = lastDate.minusDays(2);
	}

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

		    AbstractTimesheetModel invoice = timesheetMap.get(fullName.hashCode());
		    if (invoice == null) {
			invoice = newTimesheet(fullName);
			timesheetMap.put(fullName.hashCode(), invoice);
		    }

		    if (date.isAfter(getValidationPeriod()) && !status.equals(PENDING)) {
			invoice.validate();
		    } else if (date.equals(lastDate) && status.equals(PENDING)) {
			invoice.validate();
			System.out.println(invoice.getFullName() + " - Status still pending!");
		    }

		    invoice.addHours(customer, service, hours);
		}

		// TODO Remove special case MD
		AbstractTimesheetModel model = newTimesheet(StringUtils.EMPTY);
		model.validate();
		timesheetMap.put(284331424, model);

		found = true;

		br.close();
		isr.close();
		zip.close();
	    }

	    i++;
	}

	if (!found) {
	    throw new IllegalStateException("Timesheet file not found!");
	}
    }

    public Map<Integer, AbstractTimesheetModel> getTimesheetMap() {
	return timesheetMap;
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
}
