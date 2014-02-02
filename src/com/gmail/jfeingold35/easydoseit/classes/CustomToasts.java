package com.gmail.jfeingold35.easydoseit.classes;

import android.content.Context;
import android.widget.Toast;

/**
 * This class is simply a container for functions that create and display
 * commonly used toasts. These toasts are mostly related to field validation.
 * @author Josh Feingold
 *
 */
public class CustomToasts {

	/**
	 * Creates and displays a Toast, notifying the user that the given field is empty.
	 * @param context - the application context
	 * @param fieldName - The name of the empty field
	 */
	public void notDirtyToast(Context context, String fieldName) {
		String toastMsg = "NOTE: The field \'" + fieldName + "\' is empty.";
		Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Creates and displays a Toast notifying the user that the given field
	 * contains non-alphanumeric characters.
	 * @param context
	 * @param fieldName - The name of the infringing field
	 */
	public void nonAlphaNumToast(Context context, String fieldName) {
		String toastMsg = "NOTE: The field \'" + fieldName + "\' contains"
				+ " non-alphanumeric characters.";
		Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Creates and displays a Toast, notifying the user that the given field
	 * contains an improperly formatted or invalid date.
	 * @param context
	 * @param date - The date that has failed validation
	 */
	public void invalidDateToast(Context context, String date) {
		String toastMsg = "NOTE: The entry \'" + date + "\' is an invalid date.";
		Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
	}
}
