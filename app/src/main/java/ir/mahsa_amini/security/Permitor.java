package ir.mahsa_amini.security;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class Permitor {
    // Permitor is a helper class that controls Permission operation
    public static boolean hasPermission(Context context, String permission){
        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED;
    }

    public static void requestPermission(@NonNull Activity activity, @NonNull String[] permission, int requestCode){
        SharedPreferences preferences = activity.getSharedPreferences(getBufferPreferences(), Context.MODE_PRIVATE);
        preferences.edit().putInt(getPermissionRequestCodeKey(), requestCode).apply();
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }

    public static int getRequestedPermissionCode(@NonNull Activity activity){
        SharedPreferences preferences = activity.getSharedPreferences(getBufferPreferences(), Context.MODE_PRIVATE);
        return preferences.getInt(getPermissionRequestCodeKey(), 0x0);
    }

    public static String getBufferPreferences(){
        return "BUFFER_PREF";
    }

    public static String getPermissionRequestCodeKey(){
        return "PERMISSION_REQUEST_CODE";
    }
}