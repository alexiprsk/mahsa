package ir.mahsa_amini.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SafeCache {
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    public SafeCache(Context context) {
        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    "mahsa_cache",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            preferences = sharedPreferences;
            editor = preferences.edit();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SafeCache setString(String key, String value) {
        editor.putString(key, value);
        return this;
    }

    public SafeCache setInt(String key, int value) {
        editor.putInt(key, value);
        return this;
    }
    public int getInt(String key) {
        return preferences.getInt(key, -1);
    }
    public String getString(String key) {
        return preferences.getString(key, "");
    }
}
