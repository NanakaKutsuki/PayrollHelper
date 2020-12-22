package org.kutsuki.sheets.annual;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.AbstractBonusSheets;

import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.ValueRange;

public class AnnualHelper extends AbstractBonusSheets {
    private static final Integer TEMPLATE_SHEET_ID = 0;
    private static final String BONUS_SUMMARY_ID = "1-0sMbBFY2C4pOzg5Kej4AxUYM2SEcXayyst-yy5_roU";
    private static final String CARRYOVER = "Carryover";
    private static final String CARRYOVER_RANGE = "!A";
    private static final String COPY_OF = "Copy of";
    private static final String FORMULA_CLOSE = "\"},";
    private static final String FORMULA_VALUE = "\"formulaValue\":\"";
    private static final String MAIN_RANGE = "Calculator!A2:H";
    private static final String SICK_LEAVE = "Sick Leave";
    private static final String TEMPLATE_ID = "13QJaSWURfFQu2dfgKU_8KTluHaUyKk1gnhI0JnDauVg";

    private int year;
    private int nextYear;
    private Map<Integer, BigDecimal> wageMap;

    public AnnualHelper() {
	this.wageMap = new HashMap<Integer, BigDecimal>();
	LocalDate now = LocalDate.now();

	if (now.getMonthValue() == 12) {
	    this.year = now.getYear();
	    this.nextYear = now.plusYears(1).getYear();
	} else {
	    this.year = now.minusYears(1).getYear();
	    this.nextYear = now.getYear();
	}
    }

    public void run() {
	parseWages();

	for (Entry<Integer, String> entry : getNameIdMap().entrySet()) {
	    copySheet(TEMPLATE_ID, TEMPLATE_SHEET_ID, entry.getValue());
	    int sheetId = getSheetId(entry.getValue(), COPY_OF);
	    updateSheetTitle(entry.getValue(), sheetId, Integer.toString(nextYear));
	    calculateCarryOver(entry.getKey());
	}

	copyBonusSummary();
	updateBonusSummary('B');
	updateBonusSummary('C');
    }

    private void parseWages() {
	List<List<Object>> rowList = readSheet(getMainSheetId(), MAIN_RANGE);

	int i = 0;
	while (i < rowList.size() && rowList.get(i).size() > 0) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(rowList.get(i).get(0));
	    sb.append(StringUtils.SPACE);
	    sb.append(rowList.get(i).get(1));

	    BigDecimal wage = parseBigDecimal(rowList.get(i).get(7));
	    wageMap.put(sb.toString().hashCode(), wage);
	    i++;
	}
    }

    private void calculateCarryOver(int key) {
	String spreadsheetId = getNameIdMap().get(key);
	List<List<Object>> rowList = readSheet(spreadsheetId, year + getBonusRange());
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	int row = rowList.size() + 1;

	BigDecimal carryover = parseBigDecimal(rowList.get(2).get(2));
	if (carryover.compareTo(BigDecimal.ZERO) != 0) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(LocalDate.of(year, 12, 31).toString());
	    dataList.add(CARRYOVER);
	    dataList.add(carryover.negate());
	    dataList.add(getChecksum(row));
	    writeRowList.add(dataList);

	    ValueRange body = new ValueRange();
	    body.setValues(writeRowList);

	    writeSheet(spreadsheetId, year + CARRYOVER_RANGE + row, body);

	    writeRowList.clear();
	    row = 4;

	    dataList = new ArrayList<Object>();
	    dataList.add(LocalDate.of(nextYear, 1, 1).toString());
	    dataList.add(CARRYOVER);
	    dataList.add(carryover);
	    dataList.add(getChecksum(row));
	    writeRowList.add(dataList);
	    row++;
	} else {
	    row = 4;
	}

	StringBuilder sick = new StringBuilder();
	sick.append('=');
	sick.append(wageMap.get(key).negate());
	sick.append('*');
	sick.append(40);

	List<Object> dataList2 = new ArrayList<Object>();
	dataList2.add(LocalDate.of(nextYear, 1, 1).toString());
	dataList2.add(SICK_LEAVE);
	dataList2.add(sick.toString());
	dataList2.add(getChecksum(row));
	writeRowList.add(dataList2);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);

	writeSheet(spreadsheetId, nextYear + CARRYOVER_RANGE + row, body);
    }

    private void copyBonusSummary() {
	int sheetId = getSheetId(BONUS_SUMMARY_ID, Integer.toString(year));
	copySheet(BONUS_SUMMARY_ID, sheetId, BONUS_SUMMARY_ID);
	sheetId = getSheetId(BONUS_SUMMARY_ID, COPY_OF);
	updateSheetTitle(BONUS_SUMMARY_ID, sheetId, Integer.toString(nextYear));
    }

    private void updateBonusSummary(char column) {
	StringBuilder range = new StringBuilder();
	range.append(nextYear);
	range.append('!');
	range.append(column);
	range.append(3);
	range.append(':');
	range.append(column);

	List<RowData> rowData = readRowData(BONUS_SUMMARY_ID, range.toString());
	List<String> formulaList = new ArrayList<String>();
	for (RowData rd : rowData) {
	    String formula = StringUtils.substringBetween(rd.toString(), FORMULA_VALUE, FORMULA_CLOSE);

	    if (formula != null) {
		formulaList.add(formula);
	    }
	}

	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	String search = year + Character.toString('!');
	String replacement = nextYear + Character.toString('!');

	for (String formula : formulaList) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(StringUtils.replace(formula, search, replacement));
	    writeRowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);

	range = new StringBuilder();
	range.append(nextYear);
	range.append('!');
	range.append(column);
	range.append(3);
	writeSheet(BONUS_SUMMARY_ID, range.toString(), body);
    }

    private BigDecimal parseBigDecimal(Object bd) {
	return new BigDecimal(StringUtils.remove(String.valueOf(bd), '$'));
    }

    public static void main(String[] args) {
	AnnualHelper helper = new AnnualHelper();
	helper.run();
    }
}
