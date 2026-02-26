package com.hanin.smartcareer.utils;

import android.util.Patterns;


public final class ValidationUtils {

    private ValidationUtils() { /* prevent instantiation */ }

    /** @return true if the value is null or blank after trimming. */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** @return true if the email matches Android's built-in EMAIL_ADDRESS pattern. */
    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** @return true if the password meets the minimum length requirement. */
    public static boolean isPasswordLongEnough(String password, int minLength) {
        return !isEmpty(password) && password.length() >= minLength;
    }

    /** @return true if the GPA falls within the accepted range. */
    public static boolean isGpaInRange(String gpaStr, double min, double max) {
        if (isEmpty(gpaStr)) return false;
        try {
            double gpa = Double.parseDouble(gpaStr);
            return gpa >= min && gpa <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
