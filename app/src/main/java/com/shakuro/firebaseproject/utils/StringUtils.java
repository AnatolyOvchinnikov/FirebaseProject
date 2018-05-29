package com.shakuro.firebaseproject.utils;

import android.util.Patterns;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
    public static final String HASH_TAG_REGULAR_EXPRESSION = "#[A-Za-z0-9]+";
    private static final String HTML_REGULAR_EXPRESSION = ".*\\<[^>]+>.*";

    public static String getString(int resourceId, Object... args) {
        return String.format(getString(resourceId), args);
    }

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean notEmpty(CharSequence s) {
        return !isEmpty(s);
    }

    public static String emptyIfNull(String s) {
        return isEmpty(s) ? "" : s;
    }

    public static boolean isHtml(String text) {
        return text != null && text.matches(HTML_REGULAR_EXPRESSION);
    }

    public static boolean isEmail(String email) {
        return notEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPhoneNumber(String phone) {
        return notEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isWebUrl(String url) {
        return notEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean isNumber(String number) {
        if (isEmpty(number)) {
            return false;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(number, pos);

        // After parsing the string the parser position is at the end of the string.
        // If it is, we can assume the entire string is numeric:
        return number.length() == pos.getIndex();
    }

    public static List<String> splitIntoWords(String s) {
        List<String> result = new ArrayList<String>();
        if (notEmpty(s)) {
            result.addAll(Arrays.asList(s.split("\\s+")));
        }
        return result;
    }

    public static String getFirstWord(String text, char delimeter) {
        int indexOf = text.indexOf(delimeter);
        if (indexOf > -1) {
            return text.substring(0, indexOf);
        } else {
            return text;
        }
    }

    public static String removeNonDigitCharacters(String s) {
        return StringUtils.emptyIfNull(s).replaceAll("[^\\d]", "");
    }

    public static String getFileNameFromUrl(String url) {
        try {
            URL u = new URL(url);
            String fileName = u.getFile();
            if (notEmpty(fileName)) {
                String query = u.getQuery();
                if (notEmpty(query)) {
                    if (fileName.length() > query.length() + 1) {
                        fileName = fileName.substring(0, fileName.length() - query.length() - 1);
                    } else {
                        fileName = null;
                    }
                }
                return fileName;
            }
        } catch (MalformedURLException e) {
            // do nothing
        }
        return null;
    }

    public static String zeroIfEmpty(Integer value) {
        return String.valueOf((value != null) ? value : 0);
    }
}
