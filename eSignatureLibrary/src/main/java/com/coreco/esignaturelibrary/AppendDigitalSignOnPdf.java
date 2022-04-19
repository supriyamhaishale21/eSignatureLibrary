package com.coreco.esignaturelibrary;

import android.content.Context;
import android.os.StrictMode;
import android.widget.TextView;
import android.widget.Toast;

import com.coreco.esignaturelibrary.Model.Coordinate;
import com.coreco.esignaturelibrary.Model.PdfDetails;
import com.coreco.esignaturelibrary.Model.SignatureDetails;
import com.coreco.esignaturelibrary.Model.eSignPdfDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class AppendDigitalSignOnPdf {

    /**
     * @param context
     * @param eSignJsonFile Get the JSON file from assets folder and Read it.
     */
    public static void GetPDFeSignParams(Context context, String eSignJsonFile, TextView txtJsonInput,TextView txtTitle) {
        ParseJson(eSignJsonFile,txtJsonInput,txtTitle);
    }

    /**
     * @param eSignJsonDetails Parse Json String and store it in Arraylist object
     *                         Pass this ArrayList to Create XML file
     */
    public static void ParseJson(String eSignJsonDetails,TextView txtJsonInput,TextView txtTitle) {
        ArrayList<PdfDetails> pdfListDetails = new ArrayList<>();
        ArrayList<eSignPdfDetails> esignListDetails = new ArrayList<>();
        ArrayList<SignatureDetails> signatureListDetails = new ArrayList<>();
        ArrayList<Coordinate> coordinateListDetails = new ArrayList<>();
        try {
            final JSONObject obj = new JSONObject(eSignJsonDetails);
            String tempInfoPath = obj.getString("tempInfoPath");
            String signerid = obj.getString("signerid");
            String redirectUrl = obj.getString("redirectUrl");
            String responseUrl = obj.getString("responseUrl");
            String txn = obj.getString("txn");
            String aspId = obj.getString("aspId");
            String pfxPath = obj.getString("pfxPath");
            String pfxPassword = obj.getString("pfxPassword");
            String pfxAlias = obj.getString("pfxAlias");
            String signingAlgorithm = obj.getString("signingAlgorithm");
            String maxWaitPeriod = obj.getString("maxWaitPeriod");
            String ver = obj.getString("ver");
            String AuthMode = obj.getString("AuthMode");
            String fileType = obj.getString("fileType");

            final JSONArray pdfdetails = obj.getJSONArray("pdfdetails");
            for (int i = 0; i < pdfdetails.length(); i++) {
                JSONObject details = pdfdetails.getJSONObject(i);
                String pdfbase64val = details.getString("pdfbase64val");
                String docInfo = details.getString("docInfo");
                String docUrl = details.getString("docUrl");
                String reason = details.getString("reason");
                String coordinates = details.getString("coordinates");
                String pagenos = details.getString("pagenos");
                final JSONArray signDetails = details.getJSONArray("signaturedetails");
                for (int j = 0; j < signDetails.length(); j++) {
                    JSONObject sdetails = signDetails.getJSONObject(i);
                    String page = sdetails.getString("page");
                    final JSONArray coordinateDetails = sdetails.getJSONArray("coordinates");
                    for (int k = 0; k < coordinateDetails.length(); k++) {
                        JSONObject cdetails = coordinateDetails.getJSONObject(i);
                        String x = cdetails.getString("x");
                        String y = cdetails.getString("y");
                        String w = cdetails.getString("w");
                        String h = cdetails.getString("h");

                        coordinateListDetails.add(new Coordinate(x, y, w, h));
                    }
                    signatureListDetails.add(new SignatureDetails(page, coordinateListDetails));
                }
                pdfListDetails.add(new PdfDetails(pdfbase64val, docInfo, docUrl, reason, coordinates, pagenos, signatureListDetails));
            }

            esignListDetails.add(new eSignPdfDetails(tempInfoPath, signerid, redirectUrl, responseUrl,
                    txn, aspId, pfxPath, pfxPassword, pfxAlias, signingAlgorithm,
                    maxWaitPeriod, ver, AuthMode, fileType, pdfListDetails));

            /**
             * Create XML file
             */
            CreateXmlRequest(esignListDetails,txtJsonInput,txtTitle);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }


    /**
     * @param eSignListDetails
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException                  Create XML request from inputs received from ASP client Application
     */

    public static void CreateXmlRequest(ArrayList<eSignPdfDetails> eSignListDetails,TextView txtJsonInput,TextView txtTitle)
            throws ParserConfigurationException, TransformerException, IOException {

        /**
         * Create String xml and post this to ESP server.
         */
        String eSignXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Esign ver=\"" + eSignListDetails.get(0).getVer() +
                "\" AuthMode=\"" + eSignListDetails.get(0).getAuthMode() +
                "\" aspId=\"" + eSignListDetails.get(0).getAspId()
                + "\"ekycIdType=\"A\" " +
                "responseSigType=\"pkcs7pdf\" " +
                "responseUrl=\"" + eSignListDetails.get(0).getResponseUrl() + "\" " +
                "sc=\"Y\" " +
                "ts=\"2021-01-14T17:35:39.891+05:3\" " +
                "txn=\"" + eSignListDetails.get(0).getTxn() + "\">" +
                "<Docs>" +
                "<InputHash " +
                "docInfo=\"" + eSignListDetails.get(0).getPdfdetails().get(0).getDocInfo()
                + "\" hashAlgorithm=\"SHA256\" id=\"1\">" +
                "a20a9d837daf39efb889e8246031b6e315e5c7a0793394a4a4bf6a3342b75109" +
                "</InputHash>" +
                "</Docs>" +
                "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "</Signature>" +
                "</Esign>";

        txtTitle.setText("This is XML Request");

        txtJsonInput.setText(eSignXml);

        postXmlRequestToESP(eSignXml,txtJsonInput,txtTitle);

    }

    /**
     * @param eSignXml
     */
    public static void postXmlRequestToESP(String eSignXml,TextView txtJsonInput,TextView txtTitle) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL("https://esignuat.vsign.in/asp/esign/3.2/signature");

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

            txtTitle.setText("This is XML Response");

            txtJsonInput.setText(eSignXmlResponse.toString());
            ParseXmlResponse(eSignXmlResponse.toString());

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
     * Parse XML Response
     * @param eSignXmlResponse
     */

    public static void ParseXmlResponse(String eSignXmlResponse) {
        try {
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(eSignXmlResponse));

            Document doc = db.parse(is);
            String age = doc.getElementsByTagName("EsignResp").item(0).getTextContent();

            // iterate the employees
            NodeList list = doc.getElementsByTagName("EsignResp");

            // iterate the employees
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    // get staff's attribute
                    String resCode = element.getTextContent();
                    System.out.println("Current Element :" + node.getNodeName());
                    System.out.println("resCode : " + resCode);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
