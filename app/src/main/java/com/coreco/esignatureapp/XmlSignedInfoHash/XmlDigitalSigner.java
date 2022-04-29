package com.coreco.esignatureapp.XmlSignedInfoHash;


import android.util.Log;

import javax.xml.XMLConstants;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.XMLSignatureFactory;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XPathFuncHereAPI;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlDigitalSigner {

    //private static final String LOGGER =  LoggerFactory.getLogger(XmlDigitalSigner.class);

    private static final String MEC_TYPE = "DOM";
    //private static final String WHOLE_DOC_URI = "";
    public static final String KEY_STORE_TYPE = "PKCS12";

    private KeyStore.PrivateKeyEntry keyEntry;


    // used for dongle
    //private Provider userProvider;
    //public String prov;
    private static final String KEY_STORE_TYPE_DONGLE = "PKCS11";

    /**
     * Constructor
     *
     * @param keyStoreFile     - Location of .p12 file
     * @param keyStorePassword - Password of .p12 file
     * @param alias            - Alias of the certificate in .p12 file
     */

    public XmlDigitalSigner(String keyStoreFile, char[] keyStorePassword,
                            String alias) {
        this.keyEntry = getKeyFromKeyStore(keyStoreFile, keyStorePassword, alias);
        if (keyEntry == null) {
            throw new RuntimeException(
                    "Key could not be read for digital signature. Please check value of signature "
                            + "alias and signature password, and restart the Auth Client");
        }
    }

    public XmlDigitalSigner() {

    }


    @SuppressWarnings("unused")
    private KeyStore.PrivateKeyEntry getKeyFromKeyStore(Provider userProvider, char[] keyStorePassword) {
        // Load the KeyStore and get the signing key and certificate.

        try {
            Log.i("", "inside try in getkeyfromkeystore");
            //KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_DONGLE,userProvider);
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_DONGLE);
            //keyFileStream = new FileInputStream(keyStoreFile);
            ks.load(null, keyStorePassword);
            Enumeration<String> e = ks.aliases();
            String alias = null;
            while (e.hasMoreElements()) {
                alias = e.nextElement();
                Log.i("", "DS Alias :" + alias);
				/*if(EKYCProperties.getSignatureAlias().equalsIgnoreCase(alias))
				{
					break;
				}*/
            }
            Log.i("", "Alias loaded :** " + alias);
			/*X509Certificate UserCert = (X509Certificate) ks.getCertificate(alias);
			LOGGER.info("X509Certificate : \n"+UserCert);*/
            Log.i("", "inside try in getkeyfromkeystore before load");
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
                    new KeyStore.PasswordProtection(keyStorePassword));
            Log.i("", "inside try in getkeyfromkeystore before return");
            return entry;

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("", "in getKeyFromKeyStore method and the exception is", e);
            return null;
        }

    }

    /**
     * Method to digitally sign an XML document.
     *
     * @param xmlDocument - Input XML Document.
     * @return Signed XML document
     */
    public String signXML(String keyStoreFile, String keyStorePassword,
                          String alias, String xmlDocument, boolean includeKeyInfo) {

        this.keyEntry = getKeyFromKeyStore(keyStoreFile, keyStorePassword.toCharArray(), alias);
        if (keyEntry == null) {
            throw new RuntimeException(
                    "Key could not be read for digital signature. Please check value of signature "
                            + "alias and signature password, and restart the Auth Client");
        }
        try {
            // Parse the input XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            Document inputDocument = dbf.newDocumentBuilder().parse(
                    new InputSource(new StringReader(xmlDocument)));

            // Sign the input XML's DOM document
            Document signedDocument = signDocument(inputDocument, includeKeyInfo);

            // Convert the signedDocument to XML String
            StringWriter stringWriter = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "no");
            trans.transform(new DOMSource(signedDocument), new StreamResult(
                    stringWriter));

            String xmlSignedRequest = stringWriter.toString().replaceAll("\n|\r", "");
            Log.d("stringWriter: ", xmlSignedRequest);

            return xmlSignedRequest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Error while digitally signing the XML document", e);
        }
    }

    private Document signDocument(Document xmlDoc, boolean includeKeyInfo)
            throws Exception {

        if (System.getenv("SKIP_DIGITAL_SIGNATURE") != null) {
            return xmlDoc;
        }

        // Creating the XMLSignature factory.
        Security.addProvider(new XMLDSigRI());
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance
                (MEC_TYPE, "XMLDSig");

        // Creating the reference object, reading the whole document for
        // signing.


        if (keyEntry == null) {
            throw new RuntimeException(
                    "Key could not be read for digital signature. Please check value of signature alias and signature password, and restart the Auth Client");
        }

        X509Certificate x509Cert = (X509Certificate) keyEntry.getCertificate();

        KeyInfo kInfo = getKeyInfo(x509Cert, factory);


        DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);

        Log.d("digestMethod:1 ", digestMethod.toString());

        Transform transform = factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Log.d("transform:2 ", transform.toString());

        //Create a <Reference> element that references the node with the specified ID, the <Signature> element will not be counted
        Reference reference = factory.newReference("", digestMethod, Collections.singletonList(transform), null, null);
        Log.d("reference:3 ", reference.toString());

        CanonicalizationMethod canonicalizationMethod = factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null);
        Log.d("canonicalization:4 ", canonicalizationMethod.toString());

        SignatureMethod signatureMethod = factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        Log.d("signatureMethod:5 ", signatureMethod.toString());

        //Create a <SignedInfo> element
        SignedInfo sInfo = factory.newSignedInfo(canonicalizationMethod,
                signatureMethod, Collections.singletonList(reference));

        Log.d("SignedInfo:6 ", sInfo.toString());

        // Create a signature instance
        XMLSignature signature = factory.newXMLSignature(sInfo,
                includeKeyInfo ? kInfo : null);

        Log.d("XMLSignature:7 ", signature.toString());

        // Create a signature context, generated on the specified node
        DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(),
                xmlDoc.getDocumentElement());


        Log.d("DOMSignContext:8 ", dsc.toString());


        Node node = dsc.getParent();

        Log.d("Try block:9 ", "Inside Try block");
        signature.sign(dsc);


        Log.d("Owner Document:11 ", node.getOwnerDocument().toString());
        return node.getOwnerDocument();

    }

    @SuppressWarnings("unchecked")
    private KeyInfo getKeyInfo(X509Certificate cert, XMLSignatureFactory fac) {
        // Create the KeyInfo containing the X509Data.
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        @SuppressWarnings("rawtypes")
        List x509Content = new ArrayList();
        x509Content.add(cert.getSubjectX500Principal().getName());
        x509Content.add(cert);
        X509Data xd = kif.newX509Data(x509Content);
        return kif.newKeyInfo(Collections.singletonList(xd));
    }

    private KeyStore.PrivateKeyEntry getKeyFromKeyStore(String keyStoreFile, char[] keyStorePassword, String alias) {
        // Load the KeyStore and get the signing key and certificate.
        FileInputStream keyFileStream = null;
        try {
            //System.out.println("in side getKeyFromKeyStore :"+keyStoreFile+":"+keyStorePassword.toString()+":"+alias+":");
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
            keyFileStream = new FileInputStream(keyStoreFile);
            ks.load(keyFileStream, keyStorePassword);

            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(keyStorePassword));
            return entry;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (keyFileStream != null) {
                try {
                    keyFileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}