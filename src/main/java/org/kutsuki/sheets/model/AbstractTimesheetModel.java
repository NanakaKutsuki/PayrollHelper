package org.kutsuki.sheets.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractTimesheetModel {
    private static final String BUSINESS_DEVELOPMENT = "Business Development";
    private static final String COVID_SICK_LEAVE = "COVID-19 Sick Leave";
    private static final String GENERAL_ADMIN = "General Admin";
    private static final String ADMIN = "Admin";
    private static final String PAGER = "Pager";
    private static final String SECURITY = "Security";
    private static final String SENTINEL = "Sentinel";
    private static final String SICK_LEAVE = "Sick Leave";

    public abstract void addHours(String customer, String service, BigDecimal hours);

    private boolean valid;
    private String fullName;

    public AbstractTimesheetModel(String fullName) {
	this.fullName = fullName;
	this.valid = false;
    }

    public void validate() {
	this.valid = true;
    }

    public String getAdmin() {
	return ADMIN;
    }

    public String getBusinessDevelopment() {
	return BUSINESS_DEVELOPMENT;
    }

    public String getCovidSickLeave() {
	return COVID_SICK_LEAVE;
    }

    public String getFullName() {
	return fullName;
    }

    public String getGeneralAdmin() {
	return GENERAL_ADMIN;
    }

    public String getPager() {
	return PAGER;
    }

    public String getSecurity() {
	return SECURITY;
    }

    public String getSentinel() {
	return SENTINEL;
    }

    public String getSickLeave() {
	return SICK_LEAVE;
    }

    public boolean isValid() {
	return valid;
    }

    public void errorIfSentinel(String customer, String service) {
	if (StringUtils.startsWith(customer, getSentinel())) {
	    System.out.println(getFullName() + " - Wrong Customer: " + customer + StringUtils.SPACE + service);
	}
    }

    public void errorIfNotSentinel(String customer, String service) {
	if (!StringUtils.startsWith(customer, getSentinel())) {
	    System.out.println(getFullName() + " - Wrong Customer: " + customer + StringUtils.SPACE + service);
	}
    }
}
