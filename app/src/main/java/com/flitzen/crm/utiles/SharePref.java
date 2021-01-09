package com.flitzen.crm.utiles;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePref {

    public static String SharePrefName = "CRM_USER_DETAILS";

    public static String isLoggedIn = "isLoggedIn";
    public static String isAddCompany = "isAddCompany";
    public static String userId = "userId";
    public static String userName = "userName";
    public static String userEmail = "userEmail";
    public static String password = "password";
    public static String userType = "userType";
    public static String gstType = "gstType";
    public static String customerId = "customerId";
    public static String FIREBASE_TOKEN = "firebase_token";

    public static String pdf_code = "pdf_code";

    public static SharedPreferences getSharePref(Context context) {
        return context.getSharedPreferences(SharePrefName, Context.MODE_PRIVATE);
    }

    public static void Clear(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


    public static void setSharedPreferenceString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(SharePrefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSharedPreferenceString(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(SharePrefName, 0);
        return settings.getString(key, "");
    }

}
