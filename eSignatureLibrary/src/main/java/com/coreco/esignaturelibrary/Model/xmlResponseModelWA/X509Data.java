package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Element;

@Element(name = "X509Data")
public class X509Data {
    @Element(name = "X509SubjectName")
    private String x509SubjectName;

    @Element(name = "X509Certificate")
    private String x509Certificate;

    public String getX509SubjectName() {
        return x509SubjectName;
    }

    public void setX509SubjectName(String value) {
        this.x509SubjectName = value;
    }

    public String getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(String value) {
        this.x509Certificate = value;
    }
}
