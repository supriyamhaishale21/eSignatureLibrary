package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

@Element(name ="InputHash" )
public class InputHash {

    @Attribute(name = "id")
    private int id;

@Attribute(name = "hashAlgorithm")
    private String hashAlgorithm;

@Attribute(name = "docInfo")
    private String docInfo;
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
}

