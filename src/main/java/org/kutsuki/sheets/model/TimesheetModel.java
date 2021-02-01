package org.kutsuki.sheets.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class TimesheetModel extends AbstractTimesheetModel {
    private static final BigDecimal FORTY = new BigDecimal(40);

    private BigDecimal regularPay;
    private BigDecimal businessDevelopment;
    private BigDecimal sickPay;

    /**
     * Constructor
     * 
     * @param fullName Employee's full name.
     */
    public TimesheetModel(String fullName) {
	super(fullName);
	this.regularPay = BigDecimal.ZERO;
	this.businessDevelopment = BigDecimal.ZERO;
	this.sickPay = BigDecimal.ZERO;
    }

    /**
     * @return string for debuging.
     */
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFullName()).append(',').append(StringUtils.SPACE);
	sb.append(getRegularPay()).append(',').append(StringUtils.SPACE);
	sb.append(getBusinessDevelopment()).append(',').append(StringUtils.SPACE);
	sb.append(getGeneralAdmin()).append(',').append(StringUtils.SPACE);
	sb.append(getSickPay()).append(',').append(StringUtils.SPACE);
	sb.append(isValid());
	return sb.toString();
    }

    /**
     * Adds hours to respective type.
     * 
     * @param customer the name of the customer.
     * @param service  the name of the service.
     * @param hours    the number of hours.
     */
    @Override
    public void addHours(String customer, String service, BigDecimal hours) {
	if (StringUtils.equals(service, getBusinessDevelopment())) {
	    errorIfNotSentinel(customer, service);
	    businessDevelopment = businessDevelopment.add(hours);
	} else if (StringUtils.equals(service, getGeneralAdmin())) {
	    errorIfNotSentinel(customer, service);
	    // generalAdmin = generalAdmin.add(hours);
	} else if (StringUtils.equals(service, getSickLeave())) {
	    errorIfNotSentinel(customer, service);
	    sickPay = sickPay.add(hours);
	} else if (StringUtils.equals(service, getSecurity())) {
	    errorIfNotSentinel(customer, service);
	    regularPay = regularPay.add(hours);
	    validate();
	} else if (StringUtils.equals(service, getAdmin())) {
	    errorIfNotSentinel(customer, service);
	    regularPay = regularPay.add(hours);
	    validate();
	} else if (StringUtils.equals(service, getCovidSickLeave())) {
	    errorIfNotSentinel(customer, service);
	    System.out.println(getFullName() + " - Covid-19 Sick Leave Found!");
	} else {
	    errorIfSentinel(customer, service);
	    regularPay = regularPay.add(hours);
	}
    }

    public BigDecimal getRegularPay() {
	return regularPay;
    }

    public BigDecimal getBD() {
	return businessDevelopment;
    }

    public BigDecimal getGA() {
	BigDecimal generalAdmin = BigDecimal.ZERO;

	if (getFullName().hashCode() == -2123502949 || getFullName().hashCode() == -2045858276
		|| getFullName().hashCode() == 887471566 || getFullName().hashCode() == -2127870935) {
	    generalAdmin = FORTY;
	}

	return generalAdmin;
    }

    public BigDecimal getSickPay() {
	return sickPay;
    }
}
