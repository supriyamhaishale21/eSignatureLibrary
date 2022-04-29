package com.coreco.esignatureapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.PdfHash.GeneratePDFHash;
import com.coreco.esignatureapp.PermissionsUtils.PermissionUtility;
import com.coreco.esignatureapp.R;
import com.coreco.esignatureapp.XmlSignedInfoHash.XmlDigitalSigner;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



public class ChoosePdfFileActivity extends AppCompatActivity {
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;

    Button selectFileButton;
    TextView logTextView;
    String selectedFilePath = "";
    String pdfJsonInput;
    private static final String LOG_TAG = "AndroidExample";
    GeneratePDFHash generatePDFHash=new GeneratePDFHash();

    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private PermissionUtility permissionUtility;
    private ProgressBar loadingPB;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pdf_file);
        PDFBoxResourceLoader.init(getApplicationContext());

        selectFileButton = findViewById(R.id.bt_selectFile);
        logTextView = findViewById(R.id.tv_logTextView);
        loadingPB = findViewById(R.id.idLoadingPB);



        selectFileButton.setOnClickListener(view -> {
            try {
                askPermissionAndBrowseFile();
            } catch (IOException | NoSuchAlgorithmException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Request for Permission to make accessible for Storage
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void askPermissionAndBrowseFile() throws IOException, NoSuchAlgorithmException, JSONException {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            permissionUtility = new PermissionUtility(this, PERMISSIONS);
            if(permissionUtility.arePermissionsEnabled()){
                Log.d(LOG_TAG, "Permission granted 1");
                selectPdf();
            } else {
                permissionUtility.requestMultiplePermissions();
            }
        }

    }

    /**
     * Permission request result
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissionUtility.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Log.d(LOG_TAG, "Permission granted 2");
            try {
                selectPdf();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Get Pdf file path from assets folder
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectPdf() throws JSONException, IOException, NoSuchAlgorithmException {
        File file=getFileFromAssets(this,"testing.pdf");
        selectedFilePath = file.getAbsolutePath();

        /**
         * Get file path of pfx file
         */
        File pfxFile=getFileFromAssets(this, "vsign_test_certificate.pfx");
        String pfxFilePath=pfxFile.getAbsolutePath();

        /**
         * Get tickImagePath
         */
        File tickImageFile=getFileFromAssets(this, "tickimage.png");
        String tickImagePath=tickImageFile.getAbsolutePath();


        pdfJsonInput = "{\"tempInfoPath\":\"" + selectedFilePath + "\"," +
                "\"signerid\":\"\",\"redirectUrl\":\"http://localhost:55691/esignresposen.aspx\"," +
                "\"responseUrl\":\"http://localhost:55691/esignresposen.aspx\"," +
                "\"txn\":\"abc1234561237776274\"," +
                "\"aspId\":\"VTL001\"," +
                "\"pfxPath\":\""+pfxFilePath+"\"" +
                ",\"pfxPassword\":\"abc1234\"," +
                "\"pfxAlias\":\"vsign - test certificate - class ii organization 2 year signature\"," +
                "\"signingAlgorithm\":\"RSA\"," +
                "\"maxWaitPeriod\":\"1440\"," +
                "\"ver\":\"3.2\"," +
                "\"AuthMode\":\"1\"," +
                "\"fileType\":\"path\"," +
                "\"pdfdetails\":[{\"pdfbase64val\":" +
                "\"" + selectedFilePath +
                "\",\"docInfo\":\"eSignSignedFile.pdf\"," +
                "\"docUrl\":\"https://esign.verasys.in/\"," +
                "\"reason\":\"Signedby 63 moon\"," +
                "\"coordinates\":\"30,700,225,60\"," +
                "\"pagenos\":\"1\"," +
                "\"signaturedetails\":[{\"page\":\"2,3\"," +
                "\"coordinates\":[{\"x\":\"30\",\"y\":\"700\"," +
                "\"w\":\"225\",\"h\":\"60\"},{\"x\":\"330\",\"y\":" +
                "\"700\",\"w\":\"225\",\"h\":\"60\"}," +
                "{\"x\":\"30\",\"y\":\"110\",\"w\":\"225\",\"h\":\"60\"}]}]}]}";




        JSONObject jsonObject = new JSONObject(pdfJsonInput);
        JSONArray jsonArray = jsonObject.getJSONArray("pdfdetails");
        JSONObject childObject = jsonArray.getJSONObject(0);
        JSONArray jsonArray1 = childObject.getJSONArray("signaturedetails");
        JSONObject childObject1 = jsonArray1.getJSONObject(0);
        JSONArray jsonArray2 = childObject1.getJSONArray("coordinates");
        JSONObject childObject2 = jsonArray2.getJSONObject(0);


        /**
         * These are input taken for testing purpose
         */
        String pageNo=childObject1.getString("page");
        int[] pageN = new int[0];
        boolean isAllPage = false;
        if (pageNo != null && (pageNo.contains("all") || pageNo.equalsIgnoreCase("all"))) {
            isAllPage = true;
            pageN = new int[] { 0 };
        } else {
            String[] pageNost = pageNo.split(",");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pageN = Arrays.asList(pageNost).stream().mapToInt(Integer::parseInt).toArray();
                pageN = Arrays.stream(pageN).toArray();

                Log.d("Page No:",pageN.toString());
            }
        }


        /**
         * Get PDF Hash value based on signatures
         */
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap=generatePDFHash.generatePdfHash(ChoosePdfFileActivity.this,
                selectedFilePath,tickImagePath,
                "Supriya A Mhaishale","Testing","Ichalkaranji",
                "true",Float.parseFloat(childObject2.getString("x")),
                Float.parseFloat(childObject2.getString("y")),
                Float.parseFloat(childObject2.getString("w")),
                Float.parseFloat(childObject2.getString("h")),
                pageN,true,selectedFilePath,
                60,10000,isAllPage,pageNo);

        this.logTextView.setText("PDF Json Inputs received from ASP Application" +
                "\n\nSelected PDF file path: "+selectedFilePath
                +"\n\n PFXFile Path:"+pfxFilePath
        +"\n\n Pdf Hash: "+responseMap.get("hash"));

        /**
         * Create XML Payload
         */

      //  Esign esignObj=CreateXmlPayload(responseMap);

        /**
         * Write XML declaration in String format
         */
//        XmlMapper xmlMapper = new XmlMapper();
//        xmlMapper.configure(Feature.WRITE_XML_DECLARATION, true);
//        String esignReqXml = xmlMapper.writeValueAsString(esignObj);

        String eSignXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Esign ver=\"" +"2.1" +
                "\" AuthMode=\"" + "1" +
                "\" aspId=\"" + "VSYSTEST"
                + "\"ekycIdType=\"A\" " +
                "responseSigType=\"pkcs7pdf\" " +
                "responseUrl=\"" + "https://esignuat.vsign.in/asp/esign/2.1/signature" + "\" " +
                "sc=\"Y\" " +
                "ts=\""+ AspConstants.generateTsValue()+"\" " +
                "txn=\"" + AspConstants.generateTxn("VSYSTEST") + "\">" +
                "<Docs>" +
                "<InputHash " +
                "docInfo=\"" + "temp_sign.pdf"
                + "\" hashAlgorithm=\"SHA256\" id=\"1\">" +
                responseMap.get("hash") +
                "</InputHash>" +
                "</Docs>" +
                "</Esign>";

        Log.d("esignReqXml: ",eSignXml);


        /**
         * Sign XML with valid Signature
         */
        XmlDigitalSigner xmlDigitalSigner = new XmlDigitalSigner();
        String signedEsignReqXml = xmlDigitalSigner.signXML(pfxFilePath,
               "abc1234","vsign - test certificate - class ii organization 2 year signature", eSignXml, true);

        postXmlRequestToESP(signedEsignReqXml);


    }

    public void parseXml(String xmlEsignResponse)  {
        try {
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlEsignResponse));

            Document doc = db.parse(is);
            NodeList nodes = doc.getElementsByTagName("EsignResp");

            // iterate the employees
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);

                String resCode=element.getAttribute("resCode");
                System.out.println("resCode: " + resCode);

                String status=element.getAttribute("status");
                System.out.println("status: " + status);

                String ts=element.getAttribute("ts");
                System.out.println("ts: " + ts);

                String txn=element.getAttribute("txn");
                System.out.println("txn: " + txn);

                String errCode=element.getAttribute("errCode");
                System.out.println("errCode: " + errCode);

                String errMsg=element.getAttribute("errMsg");
                System.out.println("errMsg: " + errMsg);

                if(status.equals("1"))
                {
                    String txnref =txn + "|" + resCode;
                    System.out.println("txnref: " + txnref);
                    // Base 64 conversion of txn and response code
                    byte[] data = txnref.getBytes("UTF-8");
                    String base64TxnRef = Base64.encodeToString(data, Base64.DEFAULT);
                    System.out.println("base64TxnRef: " + base64TxnRef);

                    httpPostRequest(base64TxnRef);
                }


//
//
//                NodeList Signature = element.getElementsByTagName("Signature");
//                Element line = (Element) Signature.item(0);
//                System.out.println("Signature: " + getCharacterDataFromElement(line));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ChoosePdfFileActivity.this, "Redirecting to AuthPage"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    /**
     * Method to get Html response from ESP Auth Page API
     * @param base64Value
     */
    private void httpPostRequest(String base64Value)
    {
        HttpURLConnection con = null;
        String url = "https://esignuat.vsign.in/esp/authpage";
        String urlParameters = "txnref="+base64Value;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {

                wr.write(postData);
            }

            StringBuilder content;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            System.out.println(content.toString());

            Intent i = new Intent(ChoosePdfFileActivity.this, AuthPageActivity.class);
            i.putExtra("html_content", String.valueOf(content.toString()));
                   startActivity(i);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            con.disconnect();
        }

    }

    private void writeToFile(String data,Context context) {
        try {
            File file=new File(getCacheDir(),"authhtmlpage.txt");

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("authhtmlpage.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * @param eSignXml
     */
    public void postXmlRequestToESP(String eSignXml) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL("https://esignuat.vsign.in/asp/esign/2.1/signature");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            /**
             *
             *   Set timeout as per needs
             */

            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);

            /**
             * Set DoOutput to true if you want to use URLConnection for output.
             * Default is false
             */

            connection.setDoOutput(true);

            connection.setUseCaches(true);

            connection.setRequestMethod("POST");


            /**
             * Set Header
             */
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/xml");

            /**
             * Convert String XML into byte array and Send Request to ESP Server
             */

            OutputStream outputStream = connection.getOutputStream();


            byte[] b = eSignXml.getBytes("UTF-8");

            outputStream.write(b);
            outputStream.flush();
            outputStream.close();

            /**
             * Collect the response
             */
            InputStream inputStream = connection.getInputStream();

            byte[] res = new byte[2048];
            int i = 0;
            StringBuilder eSignXmlResponse = new StringBuilder();
            while (true) {

                if (!((i = inputStream.read(res)) != -1)) break;

                eSignXmlResponse.append(new String(res, 0, i));
            }

            inputStream.close();

            System.out.println("Response= " + eSignXmlResponse.toString());

            parseXml(eSignXmlResponse.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Get Correct file path from Assets folder
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