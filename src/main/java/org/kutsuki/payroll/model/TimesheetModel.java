package org.kutsuki.payroll.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class TimesheetModel extends AbstractModel {
    private static final String PENDING = "Pending";

    private boolean pending;
    private Map<String, BigDecimal> serviceHoursMap;
    private Map<LocalDate, String> dateStatusMap;

    public TimesheetModel(String worker) {
	super(worker);
	this.dateStatusMap = new TreeMap<LocalDate, String>();
	this.serviceHoursMap = new HashMap<String, BigDecimal>();
    }

    public void addDateStatus(LocalDate date, String status) {
	dateStatusMap.put(date, status);

	if (StringUtils.equals(status, PENDING)) {
	    pending = true;
	}
    }

    public void addServiceHours(String service, BigDecimal hours) {
	BigDecimal sum = serviceHoursMap.get(service);
	if (sum == null) {
	    sum = BigDecimal.ZERO;
	}

	serviceHoursMap.put(service, sum.add(hours));
    }
}
