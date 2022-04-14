package com.coreco.esignaturelibrary;

import android.content.Context;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class AppendDigitalSignOnPdf {

    /**
     *
     * @param context
     * @param eSignJsonFile
     *
     * Get the JSON file from assets folder and Read it.
     */
    public static void getPDFeSignParams(Context context,String eSignJsonFile)
    {
        String eSignJsonDetails;
        try {
            InputStream is = context.getAssets().open(eSignJsonFile);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            eSignJsonDetails = new String(buffer, "UTF-8");

            ParseJson(eSignJsonDetails);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param eSignJsonDetails
     * Parse Json String and store it in Arraylist object
     * Pass this ArrayList to Create XML file
     */
    public static void ParseJson(String eSignJsonDetails)
    {
        ArrayList<PdfDetails> pdfListDetails=new ArrayList<>();
        ArrayList<eSignPdfDetails> esignListDetails=new ArrayList<>();
        ArrayList<SignatureDetails> signatureListDetails=new ArrayList<>();
        ArrayList<Coordinate> coordinateListDetails=new ArrayList<>();
        try {
            final JSONObject obj = new JSONObject(eSignJsonDetails);
            String tempInfoPath=obj.getString("tempInfoPath");
            String signerid=obj.getString("signerid");
            String redirectUrl=obj.getString("redirectUrl");
            String responseUrl=obj.getString("responseUrl");
            String txn=obj.getString("txn");
            String aspId=obj.getString("aspId");
            String pfxPath=obj.getString("pfxPath");
            String pfxPassword=obj.getString("pfxPassword");
            String pfxAlias=obj.getString("pfxAlias");
            String signingAlgorithm=obj.getString("signingAlgorithm");
            String maxWaitPeriod=obj.getString("maxWaitPeriod");
            String ver=obj.getString("ver");
            String AuthMode=obj.getString("AuthMode");
            String fileType=obj.getString("fileType");

            final JSONArray pdfdetails = obj.getJSONArray("pdfdetails");
            for (int i = 0; i < pdfdetails.length(); i++) {
                JSONObject details = pdfdetails.getJSONObject(i);
                String pdfbase64val=details.getString("pdfbase64val");
                String docInfo=details.getString("docInfo");
                String docUrl=details.getString("docUrl");
                String reason=details.getString("reason");
                String coordinates=details.getString("coordinates");
                String pagenos=details.getString("pagenos");
                final JSONArray signDetails = details.getJSONArray("signaturedetails");
                for (int j = 0; j < signDetails.length(); j++) {
                    JSONObject sdetails = signDetails.getJSONObject(i);
                    String page=sdetails.getString("page");
                    final JSONArray coordinateDetails = sdetails.getJSONArray("coordinates");
                    for (int k = 0; k <coordinateDetails.length() ; k++) {
                        JSONObject cdetails = coordinateDetails.getJSONObject(i);
                        String x=cdetails.getString("x");
                        String y=cdetails.getString("y");
                        String w=cdetails.getString("w");
                        String h=cdetails.getString("h");

                        coordinateListDetails.add(new Coordinate(x,y,w,h));
                    }
                    signatureListDetails.add(new SignatureDetails(page,coordinateListDetails));
                }
                pdfListDetails.add(new PdfDetails(pdfbase64val,docInfo,docUrl,reason,coordinates,pagenos,signatureListDetails));
            }

            esignListDetails.add(new eSignPdfDetails(tempInfoPath,signerid,redirectUrl,responseUrl,
                    txn,aspId,pfxPath,pfxPassword,pfxAlias,signingAlgorithm,
                    maxWaitPeriod,ver,AuthMode,fileType,pdfListDetails));

            /**
             * Create XML file
             */
            createXmlRequest(esignListDetails);


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
     *
     * @param eSignListDetails
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     *
     * Create XML request from inputs received from ASP client Application
     */

    public static void createXmlRequest(ArrayList<eSignPdfDetails> eSignListDetails)
            throws ParserConfigurationException, TransformerException, IOException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        /**
         * Create root elements as Esign
         */
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Esign");

        /**
         * Set an attribute to Esign element
         * @ver
         * @AuthMode
         * @aspId
         * @ekycIdType
         * @responseSigType
         * @responseUrl
         * @sc
         * @ts
         * @txn
         *
         */

        Attr attr = document.createAttribute("ver");
        attr.setValue(eSignListDetails.get(0).getVer());
        rootElement.setAttributeNode(attr);

        Attr attr1 = document.createAttribute("AuthMode");
        attr1.setValue(eSignListDetails.get(0).getAuthMode());
        rootElement.setAttributeNode(attr1);

        Attr attr2 = document.createAttribute("aspId");
        attr2.setValue(eSignListDetails.get(0).getAspId());
        rootElement.setAttributeNode(attr2);

        Attr attr3 = document.createAttribute("ekycIdType");
        attr3.setValue("A");
        rootElement.setAttributeNode(attr3);

        Attr attr4 = document.createAttribute("responseSigType");
        attr4.setValue("pkcs7pdf");
        rootElement.setAttributeNode(attr4);

        Attr attr5 = document.createAttribute("responseUrl");
        attr5.setValue(eSignListDetails.get(0).getResponseUrl());
        rootElement.setAttributeNode(attr5);

        Attr attr6 = document.createAttribute("sc");
        attr6.setValue("Y");
        rootElement.setAttributeNode(attr6);

        Attr attr7 = document.createAttribute("ts");
        attr7.setValue("2021-01-14T17:35:39.891+05:3");
        rootElement.setAttributeNode(attr7);

        Attr attr8 = document.createAttribute("txn");
        attr8.setValue(eSignListDetails.get(0).getTxn());
        rootElement.setAttributeNode(attr8);

        document.appendChild(rootElement);

        /**
         * Create Docs element inside Root element Esign
         *
         */

        Element doc_em = document.createElement("Docs");
        rootElement.appendChild(doc_em);

        /**
         * Create InputHash element inside element Docs
         */

        Element inputhash_em = document.createElement("InputHash");
        doc_em.appendChild(inputhash_em);

        /**
         * Set an attribute to InputHash element
         * @docInfo
         * @hashAlgorithm
         * @id
         * Create Text Node includes Pdf hash key generated by Hash algorithm
         *
         */

        Attr input_attr = document.createAttribute("docInfo");
        input_attr.setValue(eSignListDetails.get(0).getPdfdetails().get(0).getDocInfo());
        inputhash_em.setAttributeNode(input_attr);

        Attr input_attr1 = document.createAttribute("hashAlgorithm");
        input_attr1.setValue("SHA256");
        inputhash_em.setAttributeNode(input_attr1);

        Attr input_attr2 = document.createAttribute("id");
        input_attr2.setValue("1");
        inputhash_em.setAttributeNode(input_attr2);

        inputhash_em.appendChild(document.createTextNode("a20a9d837daf39efb889e8246031b6e315e5c7a0793394a4a4bf6a3342b75109"));

        /**
         * Create Signature element inside Root element Esign
         */
        //create Signature element
        Element signature_em = document.createElement("Signature");
        rootElement.appendChild(signature_em);

        /**
         * Set an attribute to Signature element
         * @xmlns
         * Attribute value to Signature element is the
         * data as per standard XML Signature format
         *
         */
        Attr signature_attr = document.createAttribute("xmlns");
        signature_attr.setValue("http://www.w3.org/2000/09/xmldsig#");
        signature_em.setAttributeNode(signature_attr);

        /**
         * Write dom document to a file and doc to output stream
         */

        //
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;

        transformer = transformerFactory.newTransformer();

        DOMSource source = new DOMSource(document);
        StreamResult result = null;

        /**
         * Create temprory xml file to save the details
         */

        result = new StreamResult(File.createTempFile("eSignature_payload", ".xml"));

        transformer.transform(source, result);

        postXmlRequestToESP();

    }

    public static void postXmlRequestToESP() {

    }
}
