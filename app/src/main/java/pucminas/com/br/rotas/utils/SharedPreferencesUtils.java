package pucminas.com.br.rotas.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;

import java.util.ArrayList;

public class SharedPreferencesUtils {
     private static final String SHARED_PREFERENCE_FILE =
            "pucminas.com.br.rotas.PREFERENCE_FILE_KEY";


    public static void clear(Context context) {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        // Start editing SharedPreferences file.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        // Apply changes.
        editor.apply();
    }

    public static boolean readBoolean(Context context, String key) {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(key, false);
    }

    public static void writeBoolean(Context context, String key, boolean value) {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        // Start editing SharedPreferences file.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);

        // Apply changes.
        editor.apply();
    }

    public static void writeLong(Context context, String key, long value) {
        // Get SharedPreferences
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

        // Start editing SharedPreferences file.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);

        // Apply changes.
        editor.apply();
    }
}
