package com.coreco.esignaturelibrary;

import android.content.Context;
import android.widget.Toast;

import org.w3c.dom.Attr;
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
    public static void fetchDetails(Context c, ArrayList<String> pdfDetails)
            throws ParserConfigurationException, TransformerException, IOException {


        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        // root elements
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("Esign");

        // set an attribute to Esign element
        Attr attr = document.createAttribute("ver");
        attr.setValue("2.1");
        rootElement.setAttributeNode(attr);

        Attr attr1 = document.createAttribute("AuthMode");
        attr1.setValue("1");
        rootElement.setAttributeNode(attr1);

        Attr attr2 = document.createAttribute("aspId");
        attr2.setValue("VTL001");
        rootElement.setAttributeNode(attr2);

        Attr attr3 = document.createAttribute("ekycIdType");
        attr3.setValue("A");
        rootElement.setAttributeNode(attr3);

        Attr attr4 = document.createAttribute("responseSigType");
        attr4.setValue("pkcs7pdf");
        rootElement.setAttributeNode(attr4);

        Attr attr5 = document.createAttribute("responseUrl");
        attr5.setValue("http://127.0.0.1:7080/aspesignresponse");
        rootElement.setAttributeNode(attr5);

        Attr attr6 = document.createAttribute("sc");
        attr6.setValue("Y");
        rootElement.setAttributeNode(attr6);

        Attr attr7 = document.createAttribute("ts");
        attr7.setValue("2021-01-14T17:35:39.891+05:3");
        rootElement.setAttributeNode(attr7);

        Attr attr8 = document.createAttribute("txn");
        attr8.setValue("VTL001:14012021053537000208:1987");
        rootElement.setAttributeNode(attr8);

        document.appendChild(rootElement);

        //create Docs element
        Element doc_em = document.createElement("Docs");
        rootElement.appendChild(doc_em);

        //create InputHash element

        Element inputhash_em = document.createElement("InputHash");
        doc_em.appendChild(inputhash_em);

        Attr input_attr = document.createAttribute("docInfo");
        input_attr.setValue("eSigning of PDF Document");
        inputhash_em.setAttributeNode(input_attr);

        Attr input_attr1 = document.createAttribute("hashAlgorithm");
        input_attr1.setValue("SHA256");
        inputhash_em.setAttributeNode(input_attr1);

        Attr input_attr2 = document.createAttribute("id");
        input_attr2.setValue("1");
        inputhash_em.setAttributeNode(input_attr2);

        inputhash_em.appendChild(document.createTextNode("a20a9d837daf39efb889e8246031b6e315e5c7a0793394a4a4bf6a3342b75109"));

        //create Signature element
        Element signature_em = document.createElement("Signature");
        Attr signature_attr = document.createAttribute("xmlns");
        signature_attr.setValue("http://www.w3.org/2000/09/xmldsig#");
        signature_em.setAttributeNode(signature_attr);

        rootElement.appendChild(signature_em);

        // write dom document to a file and write doc to output stream
        TransformerFactory transformerFactory = TransformerFactory.newInstance();  // This code
        Transformer transformer = null;                // doesn't work

        transformer = transformerFactory.newTransformer();

        DOMSource source = new DOMSource(document);                                  // with Android
        StreamResult result = null;                          //  :(

        result = new StreamResult(File.createTempFile("eSignature_payload", ".xml"));

        transformer.transform(source, result);

    }
}
