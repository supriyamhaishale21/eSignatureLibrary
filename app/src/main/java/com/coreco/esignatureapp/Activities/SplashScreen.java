package com.coreco.esignatureapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.coreco.esignatureapp.R;
import com.coreco.esignaturelibrary.PermissionsUtils.PermissionUtility;


public class SplashScreen extends AppCompatActivity {
    //Time to launch the another activity
    private static int TIME_OUT = 6000;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private PermissionUtility permissionUtility;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed((Runnable) () -> {
                    askPermissionAndBrowseFile();
                },
                TIME_OUT);
    }

    /**
     * Request for Permission to make accessible for Storage
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void askPermissionAndBrowseFile() {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        permissionUtility = new PermissionUtility(this, PERMISSIONS);
        if (permissionUtility.arePermissionsEnabled()) {
            this.finish();
            navigateToActivity();

        } else {
            permissionUtility.requestMultiplePermissions();
        }


    }

    /**
     * Permission request result
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionUtility.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            this.finish();
            navigateToActivity();
        }
    }

    public void navigateToActivity()
    {
        Intent intent=new Intent(SplashScreen.this,PdfSelectionActivity.class);
        startActivity(intent);
    }

}