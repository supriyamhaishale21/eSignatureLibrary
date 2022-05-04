package com.coreco.esignatureapp;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class InputHash {


    @JacksonXmlProperty(isAttribute = true)
    private int id;

    @JacksonXmlProperty(isAttribute = true)
    private String hashAlgorithm;

    @JacksonXmlProperty(isAttribute = true)
    private String docInfo;

    @JacksonXmlProperty(isAttribute = true)
    String responseSigType;

    @JacksonXmlProperty(isAttribute = true)
    String docUrl;

    @JacksonXmlText
    protected String value;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }
    public String getDocInfo() {
        return docInfo;
    }
    public void setDocInfo(String docInfo) {
        this.docInfo = docInfo;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getResponseSigType() {
        return responseSigType;
    }
    public void setResponseSigType(String responseSigType) {
        this.responseSigType = responseSigType;
    }
    public String getDocUrl() {
        return docUrl;
    }
    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }
}

