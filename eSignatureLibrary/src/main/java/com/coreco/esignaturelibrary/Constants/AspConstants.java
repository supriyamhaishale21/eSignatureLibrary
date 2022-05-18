package com.coreco.esignaturelibrary.Constants;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.coreco.esignaturelibrary.Model.responseModel.DocSignature;
import com.coreco.esignaturelibrary.Model.responseModel.EsignResp;
import com.coreco.esignaturelibrary.Model.responseModel.Signatures;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class AspConstants {
    static String pdfJsonInput;
    public static String aspId = "VSYSTEST";
    static String authMode = "1";
    public static String ver = "2.1";
    static String pfxPassword = "abc1234";
    static String pfxAlias = "vsign - test certificate - class ii organization 2 year signature";

    public static String ASPBASEURL = "https://drona-be-stg.corecotechnologies.com/";
    public static String ESPBASEURL = "https://esignuat.vsign.in/asp/esign/2.1/signature";

    // "https://drona-be-stg.corecotechnologies.com/verasys/getCallbackXMLDetails?txn="VSYSTEST:06052022115816560000:8493";

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sdfTS2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        }
        return sdfTS2.format(new Date());
    }

    /**
     * decode encoded url in proper format
     *
     * @param encodedUrl
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    final public static String decodeUrlStringData(String encodedUrl) {
        String decodedUrl = "";
        try {
            decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedUrl;
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
//
//    public static EsignResp XMLToEsignResp(String req) {
//        EsignResp esignResp=new EsignResp();
//        Serializer serializer = new Persister();
//
//        Reader reader = new StringReader(req);
//        try {
//            esignResp=serializer.read(EsignResp.class, reader, false);
//
//            Log.d("Esign response:", esignResp.getSignatures().getDocSignature().toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return esignResp;
//    }

    /**
     * Get Json Input of pdf files
     *
     * @param context
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getJsonInput(Context context, String pdfFilePath) throws IOException {
        // Get file path of pfx file

        File pfxFile = getFileFromAssets(context, "vsign_test_certificate.pfx");
        String pfxFilePath = pfxFile.getAbsolutePath();


        // Get tickImagePath
        File tickImageFile = getFileFromAssets(context, "tickimage.png");
        String tickImagePath = tickImageFile.getAbsolutePath();

        //Creating an info file
       // File infoDir=AspConstants.createDirectory("temp_info.txt");

        File infoDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Signature Pdf Details/SignedInfo");
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
                "\"redirectUrl\":\"http://localhost:55691/esignresposen.aspx\"," +
                "\"responseUrl\":\""+ASPBASEURL+"verasys/doCallback"+"\",\"txn\":\""+generateTxn(aspId)+"\"," +
                "\"aspId\":\""+aspId+"\",\"pfxPath\":\""+pfxFilePath+"\",\"pfxPassword\":\""+pfxPassword+"\"," +
                "\"pfxAlias\":\""+pfxAlias+"\",\"signingAlgorithm\":\"RSA\",\"maxWaitPeriod\":\"1440\"," +
                "\"ver\":\""+ver+"\",\"AuthMode\":\""+authMode+"\"," +
                "\"fileType\":\"path\",\"pdfdetails\":[{\"pdfbase64val\":\""+pdfFilePath+"\"," +
                "\"docInfo\":\"eSignSignedFile.pdf\"," +
                "\"docUrl\":\"https://esign.verasys.in/\"," +
                "\"reason\":\"Signed by User\"," +
                "\"coordinates\":\"0,0,0,0\"," +
                "\"pagenos\":\"4\",\"signaturedetails\":[" +
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

    /**
     * Convert XML String to Java Object class
     *
     * @param req
     * @return
     */
    public static EsignResp XMLToEsignResp(String req, boolean isFinalXmlResponse) {
        EsignResp esignResp = new EsignResp();
        Signatures signatures = new Signatures();
        DocSignature docSignature = new DocSignature();
        ArrayList<DocSignature> docSignaturesArray = new ArrayList<>();
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(req));

            Document doc = db.parse(is);

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
            System.out.println("------");

            String ver = doc.getDocumentElement().getAttribute("ver");
            String resCode = doc.getDocumentElement().getAttribute("resCode");
            String txn = doc.getDocumentElement().getAttribute("txn");
            String ts = doc.getDocumentElement().getAttribute("ts");
            String status = doc.getDocumentElement().getAttribute("status");
            String errCode = doc.getDocumentElement().getAttribute("errCode");
            String errMsg = doc.getDocumentElement().getAttribute("errMsg");

            esignResp.setErrCode(errCode);
            esignResp.setVer(ver);
            esignResp.setTs(ts);
            esignResp.setTxn(txn);
            esignResp.setErrMsg(errMsg);
            esignResp.setStatus(status);
            esignResp.setResCode(resCode);

            if (isFinalXmlResponse) {
                NodeList userX509NodeList = doc.getElementsByTagName("UserX509Certificate");
                String userX509NodeValue = userX509NodeList.item(0).getTextContent();
                Log.e("userX509NodeValue: ", userX509NodeValue);
                esignResp.setUserX509Certificate(userX509NodeValue);

                NodeList signaturesList = doc.getElementsByTagName("Signatures");
                for (int i = 0; i < signaturesList.getLength(); i++) {
                    Node signaturesNode = signaturesList.item(i);

                    if (signaturesNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) signaturesNode;
                        NodeList docSignatureList = ((Element) signaturesNode).getElementsByTagName("DocSignature");

                        for (int j = 0; j < docSignatureList.getLength(); j++) {
                            Node docSignatureNode = docSignatureList.item(i);
                            Element element1 = (Element) docSignatureNode;
                            String id = ((Element) docSignatureNode).getAttribute("id");
                            String sigHashAlgorithm = ((Element) docSignatureNode).getAttribute("sigHashAlgorithm");

                            String docSignatureValue = docSignatureNode.getTextContent();

                            docSignature.setId(id);
                            docSignature.setSigHashAlgorithm(sigHashAlgorithm);
                            docSignature.setValue(docSignatureValue);
                            docSignaturesArray.add(docSignature);
                        }
                        signatures.setDocSignature(docSignaturesArray);

                    }

                    esignResp.setSignatures(signatures);
                    Log.d("EsignResponse:", esignResp.getSignatures().getDocSignature().get(0).getValue().toString());
                }
            } else {
                NodeList x509NodeCertificate = doc.getElementsByTagName("X509Certificate");
                String x509NodeValue = x509NodeCertificate.item(0).getTextContent();
                Log.e("x509NodeValue: ", x509NodeValue);

                esignResp.setX509Certificate(x509NodeValue);
            }





            return esignResp;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File createDirectory(String fileName) throws IOException {
        String folderPath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/Signature Pdf Details";
        File file = new File(folderPath);
        if(!file.exists()) {
            file.mkdir();
        }

        folderPath=folderPath+"/"+fileName;
        file = new File(folderPath);
        if(file.exists()) {
            file.delete();
            file.createNewFile();
        }
        return file;
    }
}
