package org.kutsuki.sheets.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LeaderboardModel implements Comparable<LeaderboardModel> {
    private BigDecimal workedThisMonth;
    private BigDecimal offThisMonth;
    private BigDecimal workedThisYear;
    private BigDecimal workableThisYear;
    private BigDecimal offThisYear;
    private int totalDaysOff;
    private String percentWorked;
    private String email;

    public LeaderboardModel(List<Object> row) {
	this.workedThisMonth = toBigDecimal(row.get(1));
	this.offThisMonth = toBigDecimal(row.get(2));
	this.workedThisYear = toBigDecimal(row.get(3));
	this.workableThisYear = toBigDecimal(row.get(4));
	this.offThisYear = toBigDecimal(row.get(5));
	this.totalDaysOff = Integer.parseInt(String.valueOf(row.get(6)));
	this.percentWorked = String.valueOf(row.get(7));
	this.email = String.valueOf(row.get(9));
    }

    @Override
    public int compareTo(LeaderboardModel rhs) {
	return rhs.getOffThisYear().compareTo(getOffThisYear());
    }

    @Override
    public boolean equals(Object obj) {
	boolean equals = false;

	if (obj == null || obj.getClass() != getClass()) {
	    equals = false;
	} else if (obj == this) {
	    equals = true;
	} else {
	    LeaderboardModel rhs = (LeaderboardModel) obj;
	    EqualsBuilder eb = new EqualsBuilder();
	    eb.append(getWorkedThisMonth(), rhs.getWorkedThisMonth());
	    eb.append(getOffThisMonth(), rhs.getOffThisMonth());
	    eb.append(getWorkedThisYear(), rhs.getWorkedThisYear());
	    eb.append(getWorkableThisYear(), rhs.getWorkableThisYear());
	    eb.append(getOffThisYear(), rhs.getOffThisYear());
	    eb.append(getTotalDaysOff(), rhs.getTotalDaysOff());
	    eb.append(getPercentWorked(), rhs.getPercentWorked());
	    eb.append(getEmail(), rhs.getEmail());
	    equals = eb.isEquals();
	}

	return equals;
    }

    @Override
    public int hashCode() {
	HashCodeBuilder hcb = new HashCodeBuilder();
	hcb.append(getWorkedThisMonth());
	hcb.append(getOffThisMonth());
	hcb.append(getWorkedThisYear());
	hcb.append(getWorkableThisYear());
	hcb.append(getOffThisYear());
	hcb.append(getTotalDaysOff());
	hcb.append(getPercentWorked());
	hcb.append(getEmail());
	return hcb.toHashCode();
    }

    public BigDecimal getWorkedThisMonth() {
	return workedThisMonth;
    }

    public BigDecimal getOffThisMonth() {
	return offThisMonth;
    }

    public BigDecimal getWorkedThisYear() {
	return workedThisYear;
    }

    public BigDecimal getWorkableThisYear() {
	return workableThisYear;
    }

    public BigDecimal getOffThisYear() {
	return offThisYear;
    }

    public int getTotalDaysOff() {
	return totalDaysOff;
    }

    public String getPercentWorked() {
	return percentWorked;
    }

    public String getEmail() {
	return email;
    }

    private BigDecimal toBigDecimal(Object o) {
	String escaped = StringUtils.remove(String.valueOf(o), ',');
	return new BigDecimal(escaped).setScale(2, RoundingMode.HALF_UP);
    }
}
