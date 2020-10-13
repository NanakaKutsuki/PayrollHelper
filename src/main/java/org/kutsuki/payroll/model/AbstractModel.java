package org.kutsuki.payroll.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractModel implements Comparable<AbstractModel> {
    private String firstName;
    private String lastName;
    private String fullName;

    public AbstractModel(String firstName, String lastName) {
	this.firstName = firstName;
	this.lastName = lastName;
	setFullName();
    }

    public AbstractModel(List<Object> list, int expectedSize) {
	if (list.size() == expectedSize) {
	    this.firstName = setData(list, 0);
	    this.lastName = setData(list, 1);
	    setFullName();
	} else {
	    throw new IllegalArgumentException("Bad Data: " + list);
	}
    }

    public AbstractModel(String worker) {
	this.firstName = StringUtils.substringBefore(worker, StringUtils.SPACE);
	this.lastName = StringUtils.substringAfterLast(worker, StringUtils.SPACE);
	setFullName();
    }

    /**
     * Comparing alphabetically by last name first then first name.
     */
    @Override
    public int compareTo(AbstractModel rhs) {
	int result = getLastName().compareTo(rhs.getLastName());

	if (result == 0) {
	    result = getFirstName().compareTo(rhs.getFirstName());
	}

	return result;
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

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public String getFullName() {
	return fullName;
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
