package com.coreco.esignatureapp.Constants;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AspConstants {
    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String generateTxn(String preFix) {

        SimpleDateFormat sdfTS=new SimpleDateFormat("ddMMyyyyhhmmssSSSSSS");
        String ts=sdfTS.format(new Date());
        Random rand = new Random();
        String id = String.format("%04d", rand.nextInt(10000));
        Log.d("Transaction ID: ",preFix+":"+ts+":"+id);
        return preFix+":"+ts+":"+id;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String generateTsValue() {
        SimpleDateFormat sdfTS2= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sdfTS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        }
        return sdfTS2.format(new Date());
    }
}
