package com.coreco.esignatureapp.Constants;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.coreco.esignatureapp.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class AspConstants {
    static String pdfJsonInput;
    public static String aspId = "VSYSTEST";
    static String authMode = "2";
    public static String ver = "2.1";
    static String pfxPassword = "abc1234";
    static String pfxAlias = "vsign - test certificate - class ii organization 2 year signature";

    public static String ASPBASEURL = "https://drona-be-stg.corecotechnologies.com/";
    public static String ESPBASEURL = "https://esignuat.vsign.in/asp/esign/2.1/signature";


    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String generateTxn(String preFix) {

        SimpleDateFormat sdfTS = new SimpleDateFormat("ddMMyyyyhhmmssSSSSSS");
        String ts = sdfTS.format(new Date());
        Random rand = new Random();
        String id = String.format("%04d", rand.nextInt(10000));
        Log.d("Transaction ID: ", preFix + ":" + ts + ":" + id);
        return preFix + ":" + ts + ":" + id;

    }

    /**
     * Generate ts(Timestamp) value
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String generateTsValue() {
        SimpleDateFormat sdfTS2 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sdfTS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        }
        return sdfTS2.format(new Date());
    }

    public static String getHtml(String txnref)
    {
        String htmlContent="<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<body>\n" +
                "<form id=\"authPageForm\" action=\"https://esignuat.vsign.in/esp/authpage\" method=\"post\">\n" +
                "<br> <input type=\"text\" id=\"txnref\" name=\"txnref\" value=\"" + txnref + "\"> \n" +
                "<br> <input type=\"submit\" value=\"Submit\"> </form>\n" +
                "</div> </body>\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "function formAutoSubmit () {\n" +
                "\n" +
                "var frm = document.getElementById(\"authPageForm\");\n" +
                "\n" +
                "frm.submit();\n" +
                "\n" +
                "}\n" +
                "\n" +
                "window.onload = formAutoSubmit;\n" +
                "</script>\n" +
                "</html>";

        return htmlContent;
    }

    /**
     * Get Json Input of pdf files
     *
     * @param context
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getJsonInput(Context context, String pdfFilePath,String pdfFileName,int pageCount) throws IOException {
        // Get file path of pfx file

        File pfxFile = getFileFromAssets(context, context.getResources().getString(R.string.pfx_file_name));
        String pfxFilePath = pfxFile.getAbsolutePath();


        // Get tickImagePath
        File tickImageFile = getFileFromAssets(context, context.getResources().getString(R.string.tick_image_file_name));
        String tickImagePath = tickImageFile.getAbsolutePath();

        //Creating an info file
       // File infoDir=AspConstants.createDirectory("temp_info.txt");

        File infoDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.info_file_path));
        if(!infoDir.exists())
        {
            infoDir.mkdirs();
        }
        String infoData = infoDir.getAbsolutePath();

       /* pdfJsonInput = "{\"tempInfoPath\":\"" + infoData + "\"," +
                "\"signerid\":\"\",\"redirectUrl\":\"http://localhost:55691/esignresposen.aspx\"," +
                "\"responseUrl\":\""+ASPBASEURL+"verasys/doCallback"+"\"," +
                "\"txn\":\"" + generateTxn(aspId) + "\"," +
                "\"aspId\":\"" + aspId + "\"," +
                "\"pfxPath\":\"" + pfxFilePath + "\"" +
                ",\"pfxPassword\":\"" + pfxPassword + "\"," +
                "\"pfxAlias\":\"" + pfxAlias + "\"," +
                "\"signingAlgorithm\":\"RSA\"," +
                "\"maxWaitPeriod\":\"1440\"," +
                "\"ver\":\"21\"," +
                "\"AuthMode\":\"" + authMode + "\"," +
                "\"fileType\":\"path\"," +
                "\"pdfdetails\":[{\"pdfbase64val\":" +
                "\"" + pdfFilePath +
                "\",\"docInfo\":\"eSignSignedFile.pdf\"," +
                "\"docUrl\":\"https://esign.verasys.in/\"," +
                "\"reason\":\"Signed by User\"," +
                "\"coordinates\":\"30,700,225,60\"," +
                "\"pagenos\":\"4\"," +
                "\"signaturedetails\":[{\"page\":\"1,2,3,4\"," +
                "\"coordinates\":[{\"x\":\"30\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"330\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"30\",\"y\":\"110\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"50\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}" +
                "]" +
                "}" +
                "]" +
                "}" +
                "]" +
                "}";*/

        pdfJsonInput="{\"tempInfoPath\":\""+infoData+"\"," +
                "\"signerid\":\"\"," +
                "\"redirectUrl\":\""+context.getResources().getString(R.string.redirect_url)+"\"," +
                "\"responseUrl\":\""+ASPBASEURL+context.getResources().getString(R.string.callback_url_name)+"\",\"txn\":\""+generateTxn(context.getResources().getString(R.string.aspId))+"\"," +
                "\"aspId\":\""+context.getResources().getString(R.string.aspId)+"\",\"pfxPath\":\""+pfxFilePath+"\",\"pfxPassword\":\""+context.getResources().getString(R.string.pfxPassword)+"\"," +
                "\"pfxAlias\":\""+context.getResources().getString(R.string.pfxAlias)+"\",\"signingAlgorithm\":\""+context.getResources().getString(R.string.signingAlgorithm)+"\"," +
                "\"maxWaitPeriod\":\""+context.getResources().getString(R.string.maxWaitPeriod)+"\"," +
                "\"ver\":\""+context.getResources().getString(R.string.ver)+"\",\"AuthMode\":\""+context.getResources().getString(R.string.authMode)+"\"," +
                "\"fileType\":\""+context.getResources().getString(R.string.file_type)+"\"," +
                "\"pdfdetails\":[{\"pdfbase64val\":\""+pdfFilePath+"\"," +
                "\"docInfo\":\""+pdfFileName+"\"," +
                "\"docUrl\":\""+context.getResources().getString(R.string.docUrl)+"\"," +
                "\"reason\":\""+context.getResources().getString(R.string.reason)+"\"," +
                "\"coordinates\":\"0,0,0,0\"," +
                "\"pagenos\":\""+pageCount+"\",\"signaturedetails\":[" +
                "{" +
                "\"page\":\"2\"," +
                "\"coordinates\":[{\"x\":\"30\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}]},{"+
                "\"page\":\"4\"," +
                "\"coordinates\":[{\"x\":\"30\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"330\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"30\",\"y\":\"110\",\"w\":\"225\",\"h\":\"60\"}]}," +
                "{\"page\":\"3\"," +
                "\"coordinates\":[{\"x\":\"30\",\"y\":\"750\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"330\",\"y\":\"750\",\"w\":\"225\",\"h\":\"60\"}]" +
                "}]}]}";
        return pdfJsonInput;

    }

    /**
     * Get Correct file path from Assets folder
     *
     * @param context
     * @param filename
     * @return
     * @throws IOException
     */
    public static File getFileFromAssets(Context context, String filename) throws IOException {
        File cacheFile = new File(context.getCacheDir(), filename);
        try {
            InputStream inputStream = context.getAssets().open(filename);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new IOException("Could not open robot png", e);
        }
        return cacheFile;
    }

}
