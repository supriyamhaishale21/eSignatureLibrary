package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Element;

@Element(name = "Signature")
public class Signature {
    @Element(name="signedInfo")
    private SignedInfo signedInfo;

    @Element(name="signatureValue")
    private String signatureValue;

    @Element(name="keyInfo")
    private KeyInfo keyInfo;

    @Element(name="xmlns")
    private String xmlns;

    public SignedInfo getSignedInfo() { return signedInfo; }
    public void setSignedInfo(SignedInfo value) { this.signedInfo = value; }

    public String getSignatureValue() { return signatureValue; }
    public void setSignatureValue(String value) { this.signatureValue = value; }

    public KeyInfo getKeyInfo() { return keyInfo; }
    public void setKeyInfo(KeyInfo value) { this.keyInfo = value; }

    public String getXmlns() { return xmlns; }
    public void setXmlns(String value) { this.xmlns = value; }
}













