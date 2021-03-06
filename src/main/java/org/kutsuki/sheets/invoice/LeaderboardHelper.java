package org.kutsuki.sheets.invoice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.kutsuki.sheets.AbstractSheets;
import org.kutsuki.sheets.model.LeaderboardModel;

public class LeaderboardHelper extends AbstractSheets {
    private static final String LEADERBOARDS = " Leaderboards";
    private static final String YOU = "You";
    private static final String AVERAGE = "Company Average";
    private static final String WORKED_THIS_MONTH = "Worked This Month";
    private static final String OFF_THIS_MONTH = "Off This Month";
    private static final String WORKED_THIS_YEAR = "Worked This Year";
    private static final String WORKABLE_THIS_YEAR = "Workable This Year";
    private static final String OFF_THIS_YEAR = "Off This Year";
    private static final String TOTAL_DAYS_OFF = "Total Days Off";
    private static final String PERCENTAGE_WORKED = "Percentage Worked";
    private static final String FEDERAL_HOLIDAYS = "Federal Holidays";
    private static final String RANK = "Rank";
    private static final String HOURS = " hours";
    private static final String DAYS = " days";
    private static final String NOT_APPLICABLE = "n/a";
    private static final String OUT_OF = "Out of ";

    private static final String TABLE = "<table style=\"border: 1px solid black;border-collapse: collapse;\">";
    private static final String TABLE_END = "</table><br/></br>";
    private static final String TD = "<td style=\"border: 1px solid #ddd;padding: 8px;\">";
    private static final String TD_END = "</td>";
    private static final String TH = "<th style=\"border: 1px solid #ddd;padding: 8px;\">";
    private static final String TH_END = "</th>";
    private static final String TR = "<tr>";
    private static final String TR_END = "</tr>";

    private static final String ND = "nd";
    private static final String RD = "rd";
    private static final String ST = "st";
    private static final String NTH = "th";

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DTF_PARSE = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String INVOICE_ID = "1IER8vbOZpru8fjEkOMVRyObaMgIDM2yKMAUk6jJH9uI";
    private static final String TIME_OFF_RANGE = "Time Off!A2:J";

    private EmailManager email;
    private int holidays;
    private List<LocalDate> holidayList;
    private LocalDate endDate;

    public LeaderboardHelper() {
	this.email = new EmailManager();
	this.holidayList = new ArrayList<LocalDate>();
	this.holidayList.add(LocalDate.of(2021, 1, 1));
	this.holidayList.add(LocalDate.of(2021, 1, 18));
	this.holidayList.add(LocalDate.of(2021, 2, 15));
	this.holidayList.add(LocalDate.of(2021, 5, 31));
	this.holidayList.add(LocalDate.of(2021, 6, 18));
	this.holidayList.add(LocalDate.of(2021, 7, 5));
	this.holidayList.add(LocalDate.of(2021, 9, 6));
	this.holidayList.add(LocalDate.of(2021, 10, 11));
	this.holidayList.add(LocalDate.of(2021, 11, 11));
	this.holidayList.add(LocalDate.of(2021, 11, 25));
	this.holidayList.add(LocalDate.of(2021, 12, 24));

	if (LocalDate.now().minusWeeks(1).getYear() != holidayList.get(0).getYear()) {
	    throw new IllegalArgumentException("Holiday List needs to be updated! " + holidayList.get(0).getYear());
	}
    }

    @Override
    public void run() {
	List<LeaderboardModel> modelList = new ArrayList<LeaderboardModel>();

	List<List<Object>> rowList = readSheet(INVOICE_ID, TIME_OFF_RANGE);

	LeaderboardModel average = parseRow(rowList.get(0), true);
	String subject = DTF.format(endDate) + LEADERBOARDS;

	for (int i = 1; i < rowList.size(); i++) {
	    modelList.add(parseRow(rowList.get(i), false));
	}

	Collections.sort(modelList);

	Random random = new Random();
	int sendHome = random.nextInt(modelList.size());

	for (int i = 0; i < modelList.size(); i++) {
	    LeaderboardModel model = modelList.get(i);
	    StringBuilder sb = new StringBuilder();
	    sb.append(TABLE);
	    sb.append(TR);
	    sb.append(TH).append(DTF.format(endDate)).append(TH_END);
	    sb.append(TH).append(YOU).append(TH_END);
	    sb.append(TH).append(AVERAGE).append(TH_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(WORKED_THIS_MONTH).append(TD_END);
	    sb.append(TD).append(model.getWorkedThisMonth()).append(HOURS).append(TD_END);
	    sb.append(TD).append(average.getWorkedThisMonth()).append(HOURS).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(OFF_THIS_MONTH).append(TD_END);
	    sb.append(TD).append(model.getOffThisMonth()).append(HOURS).append(TD_END);
	    sb.append(TD).append(average.getOffThisMonth()).append(HOURS).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(WORKED_THIS_YEAR).append(TD_END);
	    sb.append(TD).append(model.getWorkedThisYear()).append(HOURS).append(TD_END);
	    sb.append(TD).append(average.getWorkedThisYear()).append(HOURS).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(WORKABLE_THIS_YEAR).append(TD_END);
	    sb.append(TD).append(model.getWorkableThisYear()).append(HOURS).append(TD_END);
	    sb.append(TD).append(NOT_APPLICABLE).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(OFF_THIS_YEAR).append(TD_END);
	    sb.append(TD).append(model.getOffThisYear()).append(HOURS).append(TD_END);
	    sb.append(TD).append(average.getOffThisYear()).append(HOURS).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(TOTAL_DAYS_OFF).append(TD_END);
	    sb.append(TD).append(model.getTotalDaysOff()).append(DAYS).append(TD_END);
	    sb.append(TD).append(average.getTotalDaysOff()).append(DAYS).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(PERCENTAGE_WORKED).append(TD_END);
	    sb.append(TD).append(model.getPercentWorked()).append(TD_END);
	    sb.append(TD).append(average.getPercentWorked()).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(FEDERAL_HOLIDAYS).append(TD_END);
	    sb.append(TD).append(holidays).append(DAYS).append(TD_END);
	    sb.append(TD).append(OUT_OF).append(holidayList.size()).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TR);
	    sb.append(TD).append(RANK).append(TD_END);
	    sb.append(TD).append(ordinal(i + 1)).append(TD_END);
	    sb.append(TD).append(OUT_OF).append(modelList.size()).append(TD_END);
	    sb.append(TR_END);
	    sb.append(TABLE_END);

	    System.out.println("mailto: " + model.getEmail());
	    email.emailSentinel(model.getEmail(), subject, sb.toString());

	    if (i == sendHome) {
		email.emailHome(model.getEmail(), sb.toString());
	    }
	}
    }

    private LeaderboardModel parseRow(List<Object> row, boolean average) throws DateTimeParseException {
	if (average) {
	    endDate = LocalDate.parse(String.valueOf(row.get(9)), DTF_PARSE);

	    if (LocalDate.now().minusWeeks(1).isAfter(endDate)) {
		throw new IllegalArgumentException("Parsed end date is too old! " + endDate);
	    }

	    Iterator<LocalDate> itr = holidayList.iterator();
	    LocalDate date = itr.next();
	    while (date.isBefore(endDate) || date.isEqual(endDate)) {
		holidays++;

		if (itr.hasNext()) {
		    date = itr.next();
		} else {
		    date = LocalDate.MAX;
		}
	    }
	}

	return new LeaderboardModel(row);
    }

    private String ordinal(int i) {
	String[] suffixes = new String[] { NTH, ST, ND, RD, NTH, NTH, NTH, NTH, NTH, NTH };
	switch (i % 100) {
	case 11:
	case 12:
	case 13:
	    return i + NTH;
	default:
	    return i + suffixes[i % 10];

	}
    }

    public static void main(String[] args) {
	LeaderboardHelper helper = new LeaderboardHelper();
	helper.run();
    }
}
