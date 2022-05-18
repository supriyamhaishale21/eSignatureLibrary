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
}
