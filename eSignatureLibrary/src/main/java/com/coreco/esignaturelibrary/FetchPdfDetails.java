package com.coreco.esignaturelibrary;

import android.content.Context;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
        Element rootElement = document.createElement("map");
        document.appendChild(rootElement);

        Element em = document.createElement("string");
        em.setAttribute("name", "FirstName");
        em.appendChild(document.createTextNode("Rita"));
        rootElement.appendChild(em);

        em = document.createElement("string");
        em.setAttribute("name", "LastName");
        em.appendChild(document.createTextNode("Roy"));
        rootElement.appendChild(em);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();  // This code
        Transformer transformer = null;                // doesn't work
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        DOMSource source = new DOMSource(document);                                  // with Android
        StreamResult result = new StreamResult("c:\\abc.xml");                          //  :(
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
