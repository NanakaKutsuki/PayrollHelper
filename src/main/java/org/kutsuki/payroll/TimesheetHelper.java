package org.kutsuki.payroll;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.payroll.model.TimesheetModel;

public class TimesheetHelper extends AbstractBonusSheets {
    private static final File DESKTOP = new File("C:\\Users\\MatchaGreen\\Desktop\\");
    private static final String USER_REPORT = "UserReport_";
    private static final String ZIP = ".zip";

    private Map<String, TimesheetModel> timesheetMap;

    /**
     * Top level runner
     */
    @Override
    public void run() {
	try {
	    parseMondaySheet(false);
	    parseCsv();
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
	this.timesheetMap = new HashMap<String, TimesheetModel>();

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
		    LocalDate date = LocalDate.parse(splitLine[1], getDateTimeFormatter());
		    String worker = splitLine[2];
		    String service = splitLine[3] + splitLine[4];
		    BigDecimal hours = new BigDecimal(splitLine[9]);
		    String status = splitLine[10];

		    TimesheetModel model = timesheetMap.get(worker);
		    if (model == null) {
			model = new TimesheetModel(worker);
			timesheetMap.put(worker, model);
		    }
		    model.addDateStatus(date, status);
		    model.addServiceHours(service, hours);
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
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	TimesheetHelper helper = new TimesheetHelper();
	helper.run();
    }
}
