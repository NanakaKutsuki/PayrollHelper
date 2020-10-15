package org.kutsuki.payroll;

import java.io.IOException;
import java.time.LocalDate;

import org.kutsuki.payroll.model.AbstractTimesheetModel;
import org.kutsuki.payroll.model.InvoiceModel;

public class InvoiceHelper extends AbstractTimesheet {
    private LocalDate lastDate;
    private LocalDate validate;

    /**
     * Constructor
     */
    public InvoiceHelper() {
	LocalDate date = LocalDate.now();
	if (date.getDayOfMonth() < 5) {
	    date = date.minusMonths(1);
	}

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
	    updateInvoiceSheet();
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

    private void updateInvoiceSheet() {
	// TODO implement
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
