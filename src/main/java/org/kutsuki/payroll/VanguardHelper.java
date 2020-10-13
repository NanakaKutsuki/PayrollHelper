package org.kutsuki.payroll;

import java.awt.event.KeyEvent;
import java.util.List;

public class VanguardHelper extends AbstractSheets {
    private static final String VANGUARD_RANGE = "Calculator!A2:C";

    private String regular;
    private String safeHarbor;
    private String roth;
    private String totalCompensation;
    private String totalHours;

    /**
     * Top level runner
     */
    @Override
    public void run() {
	parseVanguard();
	keyIn(3000);
    }

    /**
     * Keys in Vanguard fields.
     */
    @Override
    public void keyIn(int ms) {
	delay(ms);

	keyPress(KeyEvent.VK_TAB);
	keyIn(regular);
	keyPress(KeyEvent.VK_TAB);
	keyIn(safeHarbor);
	keyPress(KeyEvent.VK_TAB);
	keyIn(roth);
	keyPress(KeyEvent.VK_TAB);
	keyPress(KeyEvent.VK_TAB);
	keyPress(KeyEvent.VK_TAB);
	keyPress(KeyEvent.VK_TAB);
	keyIn(totalCompensation);
	keyPress(KeyEvent.VK_TAB);
	keyIn(totalHours);

	System.out.println("Done keying in!");
    }

    /**
     * Parses 401k data from the Main sheet.
     */
    private void parseVanguard() {
	List<List<Object>> rowList = readSheet(getMainSheetId(), VANGUARD_RANGE);

	int i = 0;
	boolean found = false;
	while (!found && i < rowList.size()) {
	    found = rowList.get(i).isEmpty();
	    i++;
	}

	regular = escapeString(rowList.get(i).get(2));
	safeHarbor = escapeString(rowList.get(i + 1).get(2));
	roth = escapeString(rowList.get(i + 2).get(2));
	totalCompensation = escapeString(rowList.get(i + 4).get(2));
	totalHours = escapeString(rowList.get(i + 5).get(2));

	System.out.println("Parsed data for Vanguard!");
    }

    /**
     * Main
     * 
     * @param args Outside Arguments are ignored.
     */
    public static void main(String[] args) {
	VanguardHelper helper = new VanguardHelper();
	helper.run();
    }
}
