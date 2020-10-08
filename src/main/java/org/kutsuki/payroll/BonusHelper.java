package org.kutsuki.payroll;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.api.services.sheets.v4.model.ValueRange;

/**
 * Automates updating biweekly payroll spreadsheets, logging changes into the
 * Monday Special sheet and keying into payroll system.
 * 
 * @author MatchaGreen
 */
public class BonusHelper extends AbstractBonusSheets {
    private static final String BONUS = "Bonus";
    private static final String BONUS_RANGE = LocalDate.now().getYear() + "!A:D";
    private static final String CHECKSUM = "=IF(OR(AND(BBB<>\"Bonus\",CCC<0),AND(OR(BBB=\"Bonus\",BBB=\"Carryover\",BBB=\"Sick Leave\"),CCC>0)),\"OK\",\"BAD\")";
    private static final String MONDAY_WRITE_RANGE = "Payroll!A10";
    private static final String PAYOUT = "Payout";
    private static final String USER_ENTERED = "USER_ENTERED";

    private Map<Integer, String> nameIdMap;

    /**
     * BonusHelper constructor
     */
    public BonusHelper() {
	this.nameIdMap = new HashMap<Integer, String>();
	this.nameIdMap.put(-1270501051, "1CsJf_myUrh8TlkE7f2llvGLXJjGhlcmL1w3OFYh-ZAU");
	this.nameIdMap.put(798342352, "1vCKdgucrG8KEgG-ahFzvhQDLjhtZ_jpPCW-_EeG2trU");
	this.nameIdMap.put(-1371257021, "15XGACbxP1PV3s2MJLPvkZwIN_7OkHE-qPXBiQbpcstY");
	this.nameIdMap.put(2023693587, "18MD-Os1lMgeirDN1iNSy8tw1MM5q1lB57QUaPUQ8UbY");
	this.nameIdMap.put(284331424, "1jZaYzvlM8C_UdxKUhvvXBlZ64eFpGukZhViXC1wVsIM");
	this.nameIdMap.put(-1301936526, "1tz05G5rtArMtIUrjQGjlAGjF7tNcP6d0CvByd9rZUi4");
	this.nameIdMap.put(1906050053, "1MnuuoLN6JoI9k2Jgdi1KlOGoLJ_H99wV7ub_pyD5dLk");
	this.nameIdMap.put(-562799442, "17_tDTuuOOvIV2oRpAKTIKPPajqVtJlEsLcI0UNzY350");
	this.nameIdMap.put(91890592, "1DpQrix-y_Q5lrFUC0p6OiVTzaBWhovYtM-UpkC6AztQ");
	this.nameIdMap.put(-1747439098, "19g7hojRYUMTEhTXQ7ePURvhmoh0sne9P_xDpKBd7vAw");
	this.nameIdMap.put(1388904599, "15iztAChJlezXNmNJZGNLv3wrC8x_mVQASl8OW32aHDY");
	this.nameIdMap.put(795816972, "1eTMgKojRbod4C_7d9kpRiI0j73eIvEC1FHQ7Skd8eg0");
	this.nameIdMap.put(1276387122, "1yPspt5NF2JIMaP41pVqrpzfJStrh31vCsFa76PFtboI");
	this.nameIdMap.put(-1052014173, "1eKWP3OYaTJBunqd-tUPm7QTJHw6bPiMEIcF-cpyd-2Q");
	this.nameIdMap.put(-1425788371, "18ZYwBKINYRGOCcYzHWsX_9XZWWAa_sNHgNALehEXeL8");
	this.nameIdMap.put(703122690, "154WGoAAJTRWkvHynLcsW0N8VVSLGRaqmLMFkT8xTw-I");
	this.nameIdMap.put(-1595037887, "1u92o_KizZna2itV95Yzu9l3CWwG_BrSmkN-hqyq5IOk");
	this.nameIdMap.put(148086847, "1iYQJn7zvU5WLOL6A7q_-WFigs-swRJJmFoOFl3lh3OI");
	this.nameIdMap.put(-404564145, "1VyQ35bEOtm-u083mnfbQ3GQHEB2wrgwruRIqwwgBX9o");
	this.nameIdMap.put(-1144482561, "1XtIRdvsT77T_HTZP5KcmiT0Bb3zMzDaxC63Bh8S7Z3M");
	this.nameIdMap.put(495338495, "1UH_bLc8bFfXLIww-7IPsQKUiGNYbeWWutqMObiKcOEE");
	this.nameIdMap.put(-1281717341, "1uHWbRno3E97aYz7CCsq4FWjEXtRBsCYdtMYqoSTN5JQ");
	this.nameIdMap.put(812608982, "1q9v92aErm78wDon2lqZO3NVaGWV4PWXxxqM2aFpRt-A");
	this.nameIdMap.put(722153521, "1fN1ZdapD_DzwcWoNtplMU60g1HCPSANHxkwbiYaOMFc");
	this.nameIdMap.put(1973893852, "1tnbQAT-MAhLaQuVHzjnhXJIQLnanvfgy9T_Ybeh1y-0");
	this.nameIdMap.put(-412921826, "1nObUgQjFLZJnLy6A6_ZB7S1mPtpLlb-qzi1TrsKbtVU");
    }

    /**
     * Top level runner
     */
    @Override
    public void run() {
	try {
	    parseMondaySheet(true);
	    parseEmployees();
	    updateBonusSheets();
	    updateMondaySheet();
	    keyIn(0);
	} catch (IOException e) {
	    throw new IllegalStateException(e);
	}
    }

    /**
     * Checks all bonus sheets and updates if necessary.
     * 
     * @throws IOException Exception from GoogleSheets API.
     */
    private void updateBonusSheets() throws IOException {
	for (Entry<Integer, String> entry : nameIdMap.entrySet()) {
	    System.out.println(getEmployee(entry.getKey()).getFullName() + " Checking...");
	    ValueRange response = getSheets().spreadsheets().values().get(entry.getValue(), BONUS_RANGE).execute();
	    List<List<Object>> rowList = response.getValues();
	    int nextBonusRow = rowList.size() + 1;

	    BigDecimal owed = new BigDecimal(escapeString(rowList.get(2).get(2)));
	    if (owed.compareTo(BigDecimal.ZERO) != 0) {
		BigDecimal bonus = new BigDecimal(escapeString(getEmployee(entry.getKey()).getBonus()));

		String checksum = StringUtils.replace(CHECKSUM, "BBB", "B" + nextBonusRow);
		checksum = StringUtils.replace(checksum, "CCC", "C" + nextBonusRow);

		List<List<Object>> writeRowList = new ArrayList<List<Object>>();
		List<Object> bonusList = new ArrayList<Object>();
		bonusList.add(getPayDate());
		bonusList.add(BONUS);
		bonusList.add(bonus);
		bonusList.add(checksum);
		writeRowList.add(bonusList);

		BigDecimal payout = bonus.add(owed);
		if (payout.compareTo(BigDecimal.ZERO) == 1 && entry.getKey() != 1373398698
			&& entry.getKey() != 1649047938) {
		    List<Object> payoutList = new ArrayList<Object>();
		    payoutList.add(getPayDate());
		    payoutList.add(PAYOUT);
		    payoutList.add(payout.negate());
		    payoutList.add(checksum);
		    writeRowList.add(payoutList);
		} else {
		    payout = BigDecimal.ZERO;
		}

		getMondayMap().put(getEmployee(entry.getKey()).getFullName(), payout.toString());
		getEmployee(entry.getKey()).setBonus(payout.toString());

		ValueRange body = new ValueRange();
		body.setValues(writeRowList);
		getSheets().spreadsheets().values()
			.update(entry.getValue(), LocalDate.now().getYear() + "!A" + nextBonusRow, body)
			.setValueInputOption(USER_ENTERED).execute();

		System.out.println(entry.getKey() + " updated to " + payout.toString());
	    }

	    delay(200);
	}
    }

    /**
     * Updates Monday sheet with change log.
     * 
     * @throws IOException
     */
    private void updateMondaySheet() throws IOException {
	List<List<Object>> rowList = new ArrayList<List<Object>>();
	for (Entry<String, String> entry : getMondayMap().entrySet()) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(entry.getKey());
	    dataList.add(entry.getValue());
	    rowList.add(dataList);
	}

	for (int i = 10 + rowList.size(); i < getMondayNextRow(); i++) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(StringUtils.EMPTY);
	    dataList.add(StringUtils.EMPTY);
	    rowList.add(dataList);
	}

	ValueRange body = new ValueRange();
	body.setValues(rowList);

	getSheets().spreadsheets().values().update(getMondaySheetId(), MONDAY_WRITE_RANGE, body)
		.setValueInputOption(USER_ENTERED).execute();

	System.out.println("Monday Special Sheet updated with " + getMondayMap().size() + " changes!");
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	BonusHelper helper = new BonusHelper();
	helper.run();
    }
}
