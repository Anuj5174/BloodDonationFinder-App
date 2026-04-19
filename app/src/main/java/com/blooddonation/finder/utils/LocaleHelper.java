package com.blooddonation.finder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

/**
 * Utility class for managing app locale (language) at runtime.
 * Supports English ("en") and Hindi ("hi").
 * Persists the user's language choice in SharedPreferences.
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "blood_donation_prefs";
    private static final String KEY_LANGUAGE = "app_language";
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_HINDI = "hi";

    /**
     * Call this from every Activity's attachBaseContext() or onCreate()
     * to apply the saved locale before the layout is inflated.
     */
    public static Context applyLocale(Context context) {
        String lang = getSavedLanguage(context);
        return updateResources(context, lang);
    }

    /**
     * Save the selected language and return an updated context.
     */
    public static Context setLocale(Context context, String language) {
        saveLanguage(context, language);
        return updateResources(context, language);
    }

    /**
     * Get the currently saved language code. Defaults to English.
     */
    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_ENGLISH);
    }

    private static void saveLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        return context.createConfigurationContext(config);
    }
}
