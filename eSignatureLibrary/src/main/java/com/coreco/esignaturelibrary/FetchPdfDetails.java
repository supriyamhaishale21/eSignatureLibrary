package com.coreco.esignaturelibrary;

import android.content.Context;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
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

public class FetchPdfDetails {
    public static void fetchDetails(Context c, ArrayList<String> pdfDetails) {


        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Esign");
        rootElement.getElementsByTagNameNS("ver=","2.1");

        rootElement.setTextContent("'ver'= \"'2.1'\"");
        rootElement.setTextContent("'AuthMode'= \"'1'\"");
        rootElement.setTextContent("'aspId'= \"'VTL001'\"");
        rootElement.setTextContent("'ekycIdType'= \"'A'\"");
        rootElement.setTextContent("'responseSigType'= \"'pkcs7pdf'\"");
        rootElement.setTextContent("'responseUrl'= \"\"'http://127.0.0.1:7080/aspesignresponse'\"");
        rootElement.setTextContent("'sc'= \"'Y'\"");
        rootElement.setTextContent("'ts'= \"'2021-01-14T17:35:39.891+05:3'\"");
        rootElement.setTextContent("'txn'= \"'VTL001:14012021053537000208:1987'\"");
        document.appendChild(rootElement);

        Element doc_em = document.createElement("Docs");
        rootElement.appendChild(doc_em);
        Element inputhash_em = document.createElement("InputHash");
        inputhash_em.setTextContent("'docInfo=' \"'eSigning of PDF Document'\"");
        inputhash_em.setTextContent("'hashAlgorithm=' \"'SHA256'\"");
        inputhash_em.setTextContent("'id=' \"'1'\"");
        doc_em.appendChild(inputhash_em);
        inputhash_em.setTextContent("a20a9d837daf39efb889e8246031b6e315e5c7a0793394a4a4bf6a3342b75109");
        Element signature_em = document.createElement("Signature");
        signature_em.setTextContent("xmlns= \"http://www.w3.org/2000/09/xmldsig#\"");
        rootElement.appendChild(signature_em);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();  // This code
        Transformer transformer = null;                // doesn't work
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        DOMSource source = new DOMSource(document);                                  // with Android
        StreamResult result = null;                          //  :(
        try {
            result = new StreamResult(File.createTempFile("abc",".xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
