package org.kutsuki.sheets.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class InvoiceModel extends AbstractTimesheetModel {
    private boolean sentinel;
    private Map<String, BigDecimal> hoursMap;

    /**
     * Constructor
     * 
     * @param fullName Employee's full name.
     */
    public InvoiceModel(String fullName) {
	super(fullName);
	this.sentinel = false;
	this.hoursMap = new HashMap<String, BigDecimal>();
    }

    @Override
    public boolean isValid() {
	return super.isValid() && !sentinel;
    }

    /**
     * @return string for debuging.
     */
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFullName()).append(',').append(StringUtils.SPACE);
	sb.append(hoursMap).append(',').append(StringUtils.SPACE);
	sb.append(isValid());
	return sb.toString();
    }

    /**
     * Adds hours to respective customer and service.
     * 
     * @param customer the name of the customer.
     * @param service  the name of the service.
     * @param hours    the number of hours.
     */
    @Override
    public void addHours(String customer, String service, BigDecimal hours) {
	if (!StringUtils.startsWith(customer, getSentinel())) {
	    String key = createKey(customer, service);
	    BigDecimal total = hoursMap.get(key);

	    if (total == null) {
		total = BigDecimal.ZERO;
	    }

	    hoursMap.put(key, total.add(hours));
	} else if (!StringUtils.equals(service, getBusinessDevelopment())
		&& !StringUtils.equals(service, getGeneralAdmin()) && !StringUtils.equals(service, getSickLeave())
		&& !StringUtils.equals(service, getSecurity()) && !StringUtils.equals(service, getCovidSickLeave())) {
	    errorIfSentinel(customer, service);
	    sentinel = true;
	}
    }

    public String getHours(String service) {
	String result = StringUtils.EMPTY;

	if (isValid()) {
	    BigDecimal hours = hoursMap.get(service);
	    if (hours == null) {
		hours = BigDecimal.ZERO;
	    }

	    result = hours.toString();
	}

	return result;
    }

    public Map<String, BigDecimal> getHoursMap() {
	return hoursMap;
    }

    /**
     * Creates key from customer and service
     * 
     * @return customer service.
     */
    private String createKey(String customer, String service) {
	StringBuilder sb = new StringBuilder();
	sb.append(customer);
	sb.append(StringUtils.SPACE);
	sb.append(service);
	return sb.toString();
    }
}
