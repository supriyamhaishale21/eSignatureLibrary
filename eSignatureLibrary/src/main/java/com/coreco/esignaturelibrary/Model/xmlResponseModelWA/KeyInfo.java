package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Element;

@Element(name="KeyInfo")
public class KeyInfo {
    @Element(name = "X509Data")
    private X509Data x509Data;

    public X509Data getX509Data() {
        return x509Data;
    }

    public void setX509Data(X509Data value) {
        this.x509Data = value;
    }
}
