package org.kutsuki.payroll;

import java.io.IOException;
import java.util.List;

import com.google.api.services.sheets.v4.model.ValueRange;

public class PayrollHelper extends AbstractSheets {
    private static final String ID = "1AGzsuTlo03umh2e7bGsRV0CTwo6hY9KNlViABVSjj3g";
    private static final String RANGE = "Calculator!A2:F";

    public void run() {
	try {
	    ValueRange response = getSheets().spreadsheets().values().get(ID, RANGE).execute();
	    parseResponse(response);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void parseResponse(ValueRange response) {
	List<List<Object>> values = response.getValues();

	int i = 0;
	while (i < values.size() && values.get(i).size() > 0) {
	    List<Object> list = values.get(i);
	    System.out.println(list + " " + list.size());
	    Employee e = new Employee(list);
	    i++;
	}

	// add in interns, finish employee including sort, implement robot
    }

    public static void main(String[] args) {
	PayrollHelper helper = new PayrollHelper();
	helper.run();
    }

}
