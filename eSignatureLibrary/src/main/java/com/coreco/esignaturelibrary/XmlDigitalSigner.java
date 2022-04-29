package com.coreco.esignaturelibrary;


import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
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
     * @param keyStoreFile
     *            - Location of .p12 file
     * @param keyStorePassword
     *            - Password of .p12 file
     * @param alias
     *            - Alias of the certificate in .p12 file
     */

    public XmlDigitalSigner(String keyStoreFile, char[] keyStorePassword,
                            String alias) {
        this.keyEntry = getKeyFromKeyStore(keyStoreFile, keyStorePassword,alias);
        if (keyEntry == null) {
            throw new RuntimeException(
                    "Key could not be read for digital signature. Please check value of signature "
                            + "alias and signature password, and restart the Auth Client");
        }
    }

    public XmlDigitalSigner(){

    }


    @SuppressWarnings("unused")
    private KeyStore.PrivateKeyEntry getKeyFromKeyStore(Provider userProvider, char[] keyStorePassword) {
        // Load the KeyStore and get the signing key and certificate.

        try {
            Log.i("","inside try in getkeyfromkeystore");
            //KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_DONGLE,userProvider);
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE_DONGLE);
            //keyFileStream = new FileInputStream(keyStoreFile);
            ks.load(null, keyStorePassword);
            Enumeration<String> e= ks.aliases();
            String alias=null;
            while (e.hasMoreElements())
            {
                alias = e.nextElement();
                Log.i("","DS Alias :"+alias);
				/*if(EKYCProperties.getSignatureAlias().equalsIgnoreCase(alias))
				{
					break;
				}*/
            }
            Log.i("","Alias loaded :** "+alias);
			/*X509Certificate UserCert = (X509Certificate) ks.getCertificate(alias);
			LOGGER.info("X509Certificate : \n"+UserCert);*/
            Log.i("","inside try in getkeyfromkeystore before load");
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
                    new KeyStore.PasswordProtection(keyStorePassword));
            Log.i("","inside try in getkeyfromkeystore before return");
            return entry;

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("","in getKeyFromKeyStore method and the exception is",e );
            return null;
        }

    }

    /**
     * Method to digitally sign an XML document.
     *
     * @param xmlDocument
     *            - Input XML Document.
     * @return Signed XML document
     */
    public String signXML(String keyStoreFile, String keyStorePassword,
                          String alias,String xmlDocument, boolean includeKeyInfo) {

        //Security.addProvider(new BouncyCastleProvider());
        //Security.addProvider(new org.jcp.xml.dsig.internal.dom.XMLDSigRI());
        //prov = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        this.keyEntry = getKeyFromKeyStore(keyStoreFile, keyStorePassword.toCharArray(),alias);
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
            Document signedDocument = sign(inputDocument, includeKeyInfo);

            // Convert the signedDocument to XML String
            StringWriter stringWriter = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(signedDocument), new StreamResult(
                    stringWriter));

            return stringWriter.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Error while digitally signing the XML document", e);
        }
    }

    private Document sign(Document xmlDoc, boolean includeKeyInfo)
            throws Exception {

        if (System.getenv("SKIP_DIGITAL_SIGNATURE") != null) {
            return xmlDoc;
        }

        // Creating the XMLSignature factory.
        Security.addProvider(new org.jcp.xml.dsig.internal.dom.XMLDSigRI());
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance
                (MEC_TYPE,"XMLDSig");

        // Creating the reference object, reading the whole document for
        // signing.

        DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);
        Transform transform = factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Reference reference = factory.newReference("", digestMethod, Collections.singletonList(transform), null, null);



        CanonicalizationMethod canonicalizationMethod = factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null);
        SignatureMethod signatureMethod = factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        SignedInfo sInfo = factory.newSignedInfo(canonicalizationMethod,
                signatureMethod, Collections.singletonList(reference));


        //LOGGER.error("Before getInstance 22222");
        if (keyEntry == null) {
            throw new RuntimeException(
                    "Key could not be read for digital signature. Please check value of signature alias and signature password, and restart the Auth Client");
        }

        X509Certificate x509Cert = (X509Certificate) keyEntry.getCertificate();

        KeyInfo kInfo = getKeyInfo(x509Cert, factory);
        DOMSignContext dsc = new DOMSignContext(this.keyEntry.getPrivateKey(),
                xmlDoc.getDocumentElement());
        XMLSignature signature = factory.newXMLSignature(sInfo,
                includeKeyInfo ? kInfo : null);

        signature.sign(dsc);

        Node node = dsc.getParent();
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

    private KeyStore.PrivateKeyEntry getKeyFromKeyStore(String keyStoreFile,char[] keyStorePassword, String alias) {
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