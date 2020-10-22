package org.kutsuki.sheets.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Employee Model
 * 
 * @author MatchaGreen
 */
public class EmployeeModel implements Comparable<EmployeeModel> {
    private static final String ZERO = "0.00";

    private String firstName;
    private String lastName;
    private String fullName;
    private String regularPay;
    private String businessDevelopment;
    private String generalAdmin;
    private String sickPay;
    private String bonus;

    /**
     * Employee Constructor for hard coding.
     * 
     * @param firstName  first name
     * @param lastName   last name
     * @param regularPay regular hours
     */
    public EmployeeModel(String firstName, String lastName, String regularPay) {
	this.firstName = firstName;
	this.lastName = lastName;
	this.regularPay = regularPay;
	this.bonus = ZERO;
	setFullName();
    }

    /**
     * Employee Constructor parsed from Main sheet.
     * 
     * @param data from a row.
     */
    public EmployeeModel(List<Object> list) {
	if (list.size() == 7) {
	    this.firstName = setData(list, 0);
	    this.lastName = setData(list, 1);
	    this.regularPay = setData(list, 2);
	    this.businessDevelopment = setData(list, 3);
	    this.generalAdmin = setData(list, 4);
	    this.sickPay = setData(list, 5);
	    this.bonus = StringUtils.remove(setData(list, 6), '$');
	    setFullName();
	} else {
	    throw new IllegalArgumentException("Bad Data: " + list);
	}
    }

    /**
     * Comparing alphabetically by last name first then first name.
     */
    @Override
    public int compareTo(EmployeeModel rhs) {
	int result = getLastName().compareTo(rhs.getLastName());

	if (result == 0) {
	    result = getFirstName().compareTo(rhs.getFirstName());
	}

	return result;
    }

    /**
     * @return string for debuging.
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
	sb.append(getBonus());
	return sb.toString();
    }

    /**
     * Parses row data and sets to null if it's blank.
     * 
     * @param list  row data.
     * @param index index of row data.
     * @return parsed data in String format.
     */
    public String setData(List<Object> list, int index) {
	String data = String.valueOf(list.get(index));
	if (StringUtils.isBlank(data)) {
	    data = null;
	}

	return data;
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
     * Checks if employee is a partner.
     * 
     * @return <code>true</code> if employee is a partner.
     */
    public boolean isPartner() {
	return getBusinessDevelopment() != null || getGeneralAdmin() != null;
    }

    /**
     * Checks if employee should be skipped.
     * 
     * @return <code>true</code> skip this employee.
     */
    public boolean isSkip() {
	return getRegularPay() == null || getRegularPay().equals(ZERO);
    }

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public String getFullName() {
	return fullName;
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

    public void setBonus(String bonus) {
	this.bonus = bonus;
    }

    /**
     * Creates Full Name.
     */
    private void setFullName() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFirstName());
	sb.append(StringUtils.SPACE);
	sb.append(getLastName());
	this.fullName = sb.toString();
    }
}
