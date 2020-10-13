package org.kutsuki.payroll.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Employee Model
 * 
 * @author MatchaGreen
 */
public class Employee extends AbstractModel {
    private static final String ZERO = "0.00";

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
    public Employee(String firstName, String lastName, String regularPay) {
	super(firstName, lastName);
	this.regularPay = regularPay;
	this.bonus = ZERO;
    }

    /**
     * Employee Constructor parsed from Main sheet.
     * 
     * @param data from a row.
     */
    public Employee(List<Object> list) {
	super(list, 7);
	this.regularPay = setData(list, 2);
	this.businessDevelopment = setData(list, 3);
	this.generalAdmin = setData(list, 4);
	this.sickPay = setData(list, 5);
	this.bonus = StringUtils.remove(setData(list, 6), '$');
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
}
