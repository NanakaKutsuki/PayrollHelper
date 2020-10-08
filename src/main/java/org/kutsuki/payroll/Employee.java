package org.kutsuki.payroll;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Employee Model
 * 
 * @author MatchaGreen
 */
public class Employee implements Comparable<Employee> {
    private static final String ZERO = "0.00";

    private String firstName;
    private String lastName;
    private String regularPay;
    private String businessDevelopment;
    private String generalAdmin;
    private String sickPay;
    private String bonus;
    private String fullName;
    private boolean partner;
    private boolean skip;

    /**
     * Employee Constructor for hard coding.
     * 
     * @param firstName  first name
     * @param lastName   last name
     * @param regularPay regular hours
     */
    public Employee(String firstName, String lastName, String regularPay) {
	this.firstName = firstName;
	this.lastName = lastName;
	this.regularPay = regularPay;
	this.bonus = ZERO;

	StringBuilder sb = new StringBuilder();
	sb.append(getFirstName());
	sb.append(' ');
	sb.append(getLastName());
	this.fullName = sb.toString();
    }

    /**
     * Employee Constructor parsed from Main sheet.
     * 
     * @param data from a row.
     */
    public Employee(List<Object> list) {
	if (list.size() == 7) {
	    this.firstName = setData(list, 0);
	    this.lastName = setData(list, 1);
	    this.regularPay = setData(list, 2);
	    this.businessDevelopment = setData(list, 3);
	    this.generalAdmin = setData(list, 4);
	    this.sickPay = setData(list, 5);
	    this.bonus = StringUtils.remove(setData(list, 6), '$');
	    this.partner = generalAdmin != null;
	    this.skip = regularPay == null;

	    StringBuilder sb = new StringBuilder();
	    sb.append(getFirstName());
	    sb.append(' ');
	    sb.append(getLastName());
	    this.fullName = sb.toString();
	} else {
	    throw new IllegalArgumentException("Bad Data: " + list);
	}
    }

    /**
     * To string for debuging.
     */
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFirstName()).append(',').append(' ');
	sb.append(getLastName()).append(',').append(' ');
	sb.append(getRegularPay()).append(',').append(' ');
	sb.append(getBusinessDevelopment()).append(',').append(' ');
	sb.append(getGeneralAdmin()).append(',').append(' ');
	sb.append(getSickPay()).append(',').append(' ');
	sb.append(getBonus()).append(',').append(' ');
	sb.append(isPartner()).append(',').append(' ');
	sb.append(isSkip());
	return sb.toString();
    }

    /**
     * Comparing alphabetically by last name first then first name.
     */
    @Override
    public int compareTo(Employee rhs) {
	int result = getLastName().compareTo(rhs.getLastName());

	if (result == 0) {
	    result = getFirstName().compareTo(rhs.getFirstName());
	}

	return result;
    }

    /**
     * Full name used for the key.
     * 
     * @return first name and last name.
     */
    public String getFullName() {
	return fullName;
    }

    /**
     * Checks if there is a bonus.
     * 
     * @param employee The employee to be checked.
     * @return <code>true</code> if there is a bonus.
     */
    public boolean isBonus() {
	return !getBonus().equals(ZERO);
    }

    /**
     * Parses row data and sets to null if it's blank.
     * 
     * @param list  row data.
     * @param index index of row data.
     * @return parsed data in String format.
     */
    private String setData(List<Object> list, int index) {
	String data = String.valueOf(list.get(index));
	if (StringUtils.isBlank(data)) {
	    data = null;
	}

	return data;
    }

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public String getRegularPay() {
	return regularPay;
    }

    public String getBusinessDevelopment() {
	return businessDevelopment;
    }

    public String getGeneralAdmin() {
	return generalAdmin;
    }

    public String getSickPay() {
	return sickPay;
    }

    public String getBonus() {
	return bonus;
    }

    public boolean isPartner() {
	return partner;
    }

    public boolean isSkip() {
	return skip;
    }

    public void setBonus(String bonus) {
	this.bonus = bonus;
    }
}
