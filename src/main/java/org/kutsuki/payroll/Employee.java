package org.kutsuki.payroll;

import java.util.List;

public class Employee {
    private String lastName;
    private String regularPay;
    private String businessDevelopment;
    private String generalAdmin;
    private String sickPay;
    private boolean partner;
    private boolean skip;

    public Employee(List<Object> list) {
	if (list.size() == 2) {
	    this.skip = true;
	} else if (list.size() > 4) {
	    this.lastName = String.valueOf(list.get(1));
	    this.regularPay = String.valueOf(list.get(2));
	    this.businessDevelopment = String.valueOf(list.get(3));
	    this.generalAdmin = String.valueOf(list.get(4));

	    if (list.size() > 5) {
		this.sickPay = String.valueOf(list.get(5));
	    }

	    this.partner = true;
	} else {
	    this.lastName = String.valueOf(list.get(1));
	    this.regularPay = String.valueOf(list.get(2));

	    if (list.size() > 3) {
		this.sickPay = String.valueOf(list.get(3));
	    }
	}
    }
}
