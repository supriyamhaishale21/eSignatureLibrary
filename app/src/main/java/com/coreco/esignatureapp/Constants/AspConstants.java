package com.coreco.esignatureapp.Constants;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.coreco.esignatureapp.EsignResp;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


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

    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String decodeUrlStringData(String encodedUrl) {
        String decodedUrl="";
        try {
            decodedUrl=URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedUrl;
    }

    public static EsignResp XMLToEsignResp(String req) {
        EsignResp  esignResp=new EsignResp();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EsignResp.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(req);
            esignResp = (EsignResp) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            e.printStackTrace();
            Log.e("JAXBException",e.getMessage());
        }
        return esignResp;
    }
}
