package com.gmail.jfeingold35.easydoseit.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.widget.EditText;

/**
 * This class is simply a container for functions used to validate the input
 * in the fields of the NewMedActivity.
 * @author Josh Feingold
 *
 */
public class FieldValidator {
	private Pattern pattern;
	private Matcher matcher;
	
	private static final String MONTH_PATTERN = "(0?[1-9]|1[0-2])";
	private static final String DAY_PATTERN = "(0?[1-9]|[12][0-9]|3[01])";
	private static final String YEAR_PATTERN = "((19|20)\\d\\d)";
	private static final String DATE_PATTERN =
			MONTH_PATTERN + "/" + DAY_PATTERN + "/" + YEAR_PATTERN;
	public FieldValidator() {
		pattern = Pattern.compile(DATE_PATTERN);
	}
	
	/**
	 * Confirms that a given field contains only alphanumeric characters.
	 * @param view - the EditText corresponding to the given field
	 * @param fieldName - the name of the field, sent to the appropriate
	 *                    toast in the event of failure
	 * @return true if valid, false if invalid
	 */
	public boolean validateAlphaNum(EditText view, String fieldName) {
		String contents = view.getText().toString();
		CustomToasts cToasts = new CustomToasts();
		if(contents.matches("")) {
			cToasts.notDirtyToast(view.getContext(), fieldName);
			return false;
		}
		if(!isAlphaNum(contents)) {
			cToasts.nonAlphaNumToast(view.getContext(), fieldName);
			return false;
		}
		return true;
	}
	
	/**
	 * Does the heavy lifting for validateAlphaNum above.
	 * @param string - the string to be checked
	 * @return true if valid, else false
	 */
	public boolean isAlphaNum(String string) {
		String pattern = "^[a-zA-Z0-9 ]+$";
		if(string.matches(pattern)) {
			// i.e. the string is strictly alphanumeric
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Confirms that a given date is of the form "MM/DD/YYYY".
	 * @param view - EditText corresponding to the dateFilled field
	 * @param fieldName - Generally should be 'Date Filled', but left open
	 *                    for the sake of modularity
	 * @return true if valid, else false
	 */
	public boolean validateDate(EditText view, String fieldName) {
		String contents = view.getText().toString();
		CustomToasts cToasts = new CustomToasts();
		if(contents.matches("")) {
			cToasts.notDirtyToast(view.getContext(), fieldName);
			return false;
		}
		if(!isWellFormedDate(contents)) {
			cToasts.invalidDateToast(view.getContext(), contents);
			return false;
		}
		return true;
	}
	
	/**
	 * Does the heavy lifting for validateDate above.
	 * @param date - date to be validated
	 * @return true if valid, else false
	 */
	public boolean isWellFormedDate(final String date) {
		matcher = pattern.matcher(date);
		
		if(matcher.matches()) {
			matcher.reset();
			
			if(matcher.find()) {
				String month = matcher.group(1);
				String day = matcher.group(2);
				int year = Integer.parseInt(matcher.group(3));
				if(!thirtyDayCheck(month,day)) {
					return false;
				}
				if(!febValid(month, day, year)) {
					return false;
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/** 
	 * Checks that April, June, September, or November
	 * don't have more than 30 days.
	 * @param month
	 * @param day
	 * @return true if correct, false otherwise
	 */
	public boolean thirtyDayCheck(String month, String day) {
		if(day.equals("31") &&
				(month.equals("4") || month.equals("6") || month.equals("9") ||
						month.equals("11") || month.equals("04") || month.equals("06") ||
						month.equals("09"))) {
			return false;
		}
		return true;
	}
	
	/**
	 * Deals with February, because February is a bitch.
	 * @param month
	 * @param day
	 * @param year
	 * @return true if correct, false otherwise
	 */
	public boolean febValid(String month, String day, int year) {
		if(month.equals("2") || month.equals("02")) {
			if(isLeap(year)) {
				if(day.equals("30") || day.equals("31")) {
					return false;
				} else {
					return true;
				}
			} else {
				if(day.equals("29") || day.equals("30") || day.equals("31")) {
					return false;
				} else {
					return true;
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns true if the given year is a leap year.
	 * @param year
	 * @return
	 */
	public boolean isLeap(int year) {
		if(year % 400 == 0) {
			return true;
		} else if(year % 100 == 0) {
			return false;
		} else if(year % 4 == 0) {
			return true;
		} else {
			return false;
		}
	}
}