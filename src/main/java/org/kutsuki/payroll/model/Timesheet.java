package org.kutsuki.payroll.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class Timesheet {
    private static final BigDecimal FORTY = new BigDecimal(40);
    private static final String BUSINESS_DEVELOPMENT = "Business Development";
    private static final String COVID_SICK_LEAVE = "COVID-19 Sick Leave";
    private static final String GENERAL_ADMIN = "General Admin";
    private static final String SECURITY = "Security";
    private static final String SENTINEL = "Sentinel";
    private static final String SICK_LEAVE = "Sick Leave";

    private boolean valid;
    private BigDecimal regularPay;
    private BigDecimal businessDevelopment;
    private BigDecimal sickPay;
    private String fullName;

    /**
     * Constructor
     * 
     * @param fullName Employee's full name.
     */
    public Timesheet(String fullName) {
	this.fullName = fullName;
	this.regularPay = BigDecimal.ZERO;
	this.businessDevelopment = BigDecimal.ZERO;
	this.sickPay = BigDecimal.ZERO;
	this.valid = false;
    }

    /**
     * @return string for debuging.
     */
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFullName()).append(',').append(' ');
	sb.append(getRegularPay()).append(',').append(' ');
	sb.append(getBusinessDevelopment()).append(',').append(' ');
	sb.append(getGeneralAdmin()).append(',').append(' ');
	sb.append(getSickPay()).append(',').append(' ');
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
    public void addHours(String customer, String service, BigDecimal hours) {
	if (StringUtils.equals(service, BUSINESS_DEVELOPMENT)) {
	    customerCheck(customer, service);
	    businessDevelopment = businessDevelopment.add(hours);
	} else if (StringUtils.equals(service, GENERAL_ADMIN)) {
	    customerCheck(customer, service);
	    // generalAdmin = generalAdmin.add(hours);
	} else if (StringUtils.equals(service, SICK_LEAVE)) {
	    customerCheck(customer, service);
	    sickPay = sickPay.add(hours);
	} else if (StringUtils.equals(service, SECURITY)) {
	    customerCheck(customer, service);
	    regularPay = regularPay.add(hours);
	    validate();
	} else if (StringUtils.equals(service, COVID_SICK_LEAVE)) {
	    customerCheck(customer, service);
	    System.out.println(getFullName() + " - Covid-19 Sick Leave Found!");
	} else {
	    if (StringUtils.startsWith(customer, SENTINEL)) {
		System.out.println(getFullName() + " - Wrong Customer: " + customer + StringUtils.SPACE + service);
	    }

	    regularPay = regularPay.add(hours);
	}
    }

    public String getFullName() {
	return fullName;
    }

    public BigDecimal getRegularPay() {
	return regularPay;
    }

    public BigDecimal getBusinessDevelopment() {
	return businessDevelopment;
    }

    public BigDecimal getGeneralAdmin() {
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

    public boolean isValid() {
	return valid;
    }

    public void validate() {
	this.valid = true;
    }

    private void customerCheck(String customer, String service) {
	if (!StringUtils.startsWith(customer, SENTINEL)) {
	    System.out.println(getFullName() + " - Wrong Customer: " + customer + StringUtils.SPACE + service);
	}
    }
}
