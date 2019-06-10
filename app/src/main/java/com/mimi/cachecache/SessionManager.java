package com.mimi.cachecache;

import android.content.Context;
import android.content.SharedPreferences;

import org.checkerframework.checker.nullness.compatqual.NonNullType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

class SessionManager {
    // Shared Preferences
    final SharedPreferences sharedPrefer;

    // Editor for Shared preferences
    final SharedPreferences.Editor editor;

    // Context
    final Context context;

    // Shared Pref mode
    final int PRIVATE_MODE = 0;

    // Shared Pref file name
    private static final String PREF_NAME = "MySession";

    // SHARED PREF KEYS FOR ALL DATA

    // User's UserId
    public static final String KEY_USERID = "userId";

    // User's categoryId
    public static final String KEY_CATTYPE = "catId";

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        sharedPrefer = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPrefer.edit();

        if (noUser()) {
            editor.putString(KEY_USERID, UUID.randomUUID().toString());
        }
    }

    public void createLoginSession(String catType) {

        // Storing catType in pref
        editor.putString(KEY_CATTYPE, catType);

        // commit changes
        editor.commit();
    }
    @NotNull
    public HashMap<String, String> getUserDetails() {

        HashMap<String, String> user = new HashMap<String, String>();
        user.put("userId",sharedPrefer.getString(KEY_USERID, null));
        user.put("catType", sharedPrefer.getString(KEY_CATTYPE, null));

        return user;
    }

    public boolean noUser() {
        return (sharedPrefer.getString(KEY_USERID, null) == null);
    }
}
