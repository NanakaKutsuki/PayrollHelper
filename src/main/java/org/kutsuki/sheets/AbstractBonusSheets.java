package org.kutsuki.sheets;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.sheets.model.AbstractTimesheetModel;
import org.kutsuki.sheets.model.TimesheetModel;

public abstract class AbstractBonusSheets extends AbstractTimesheet {
    private static final String BBB = "BBB";
    private static final String BONUS_RANGE = "!A:D";
    private static final String CCC = "CCC";
    private static final String CHECKSUM = "=IF(OR(AND(BBB<>\"Bonus\",CCC<0),AND(OR(BBB=\"Bonus\",BBB=\"Carryover\",BBB=\"Sick Leave\"),CCC>0)),\"OK\",\"BAD\")";

    private Map<Integer, String> nameIdMap;
    private LocalDate validate;

    /**
     * AbstractBonusSheets constructor
     */
    public AbstractBonusSheets() {
	this.nameIdMap = new HashMap<Integer, String>();
	this.nameIdMap.put(-1270501051, "1CsJf_myUrh8TlkE7f2llvGLXJjGhlcmL1w3OFYh-ZAU");
	this.nameIdMap.put(798342352, "1vCKdgucrG8KEgG-ahFzvhQDLjhtZ_jpPCW-_EeG2trU");
	this.nameIdMap.put(-1372210333, "15XGACbxP1PV3s2MJLPvkZwIN_7OkHE-qPXBiQbpcstY");
	this.nameIdMap.put(2023693587, "18MD-Os1lMgeirDN1iNSy8tw1MM5q1lB57QUaPUQ8UbY");
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
	this.nameIdMap.put(608561597, "1NdWxtrWEG3PSnVj35QXw_Da04ign3xvUh2YJ52cet54");
	this.nameIdMap.put(203595292, "1pHT8P3ME08Lk9vt0HypEXWLV4w6463HVLvEqMfbHn3s");
	this.nameIdMap.put(1910574595, "1a3uPMFJzB53rtO48KVEJ-dgBAQQFP46sCXtf6LRq5_4");
	this.nameIdMap.put(-1821306982, "1taCKWmGaHLRK_My5cZPrdl6t_EpjRyNDP2BMCH4B33c");
	this.nameIdMap.put(1140926759, "1Lb3S4_h5yFD1-RhWJ9ViJYsGT3qK-bzHze3SQdb4CwI");
	this.nameIdMap.put(1534121308, "1GYpEAnzUn0ALnvOsfO2ubdheJj0l_Hldl37ovoH3Y6M");

//	// TODO Remove special case MD
//	this.nameIdMap.put(284331424, "1jZaYzvlM8C_UdxKUhvvXBlZ64eFpGukZhViXC1wVsIM");
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

    public String getChecksum(int row) {
	String checksum = StringUtils.replace(CHECKSUM, BBB, Character.toString('B') + row);
	checksum = StringUtils.replace(checksum, CCC, Character.toString('C') + row);
	return checksum;
    }

    public Map<Integer, String> getNameIdMap() {
	return nameIdMap;
    }

    public String getBonusRange() {
	return BONUS_RANGE;
    }
}
