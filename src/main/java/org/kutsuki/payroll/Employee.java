package org.kutsuki.payroll;

import java.util.List;

public class Employee implements Comparable<Employee> {
    private String firstName;
    private String lastName;
    private String regularPay;
    private String businessDevelopment;
    private String generalAdmin;
    private String sickPay;
    private boolean partner;
    private boolean skip;

    public Employee(String lastName, String regularPay) {
	this.lastName = lastName;
	this.regularPay = regularPay;
    }

    public Employee(List<Object> list) {
	if (list.size() == 7) {
	    this.firstName = setData(list, 0);
	    this.lastName = setData(list, 1);
	    this.regularPay = setData(list, 2);
	    this.businessDevelopment = setData(list, 3);
	    this.generalAdmin = setData(list, 4);
	    this.sickPay = setData(list, 5);
	    this.partner = generalAdmin != null;
	    this.skip = regularPay == null;
	} else {
	    throw new IllegalArgumentException("Bad Data: " + list);
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(getFirstName()).append(',').append(' ');
	sb.append(getLastName()).append(',').append(' ');
	sb.append(getRegularPay()).append(',').append(' ');
	sb.append(getBusinessDevelopment()).append(',').append(' ');
	sb.append(getGeneralAdmin()).append(',').append(' ');
	sb.append(getSickPay()).append(',').append(' ');
	sb.append(isPartner()).append(',').append(' ');
	sb.append(isSkip());
	return sb.toString();
    }

    @Override
    public int compareTo(Employee rhs) {
	int result = getLastName().compareTo(rhs.getLastName());

	if (result == 0) {
	    result = getFirstName().compareTo(rhs.getFirstName());
	}

	return result;
    }

    // sets empty strings to null
    private String setData(List<Object> list, int index) {
	String data = String.valueOf(list.get(index));
	if (data.isEmpty()) {
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

    public boolean isPartner() {
	return partner;
    }

    public boolean isSkip() {
	return skip;
    }
}
