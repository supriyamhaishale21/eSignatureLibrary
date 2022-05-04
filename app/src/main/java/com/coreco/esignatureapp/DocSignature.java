package com.coreco.esignatureapp;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "DocSignatures")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocSignature {


    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "sigHashAlgorithm", required = true)
    protected String sigHashAlgorithm;

    @XmlAttribute(name = "error", required = true)
    protected String error;

    @XmlValue
    protected String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSigHashAlgorithm() {
        return sigHashAlgorithm;
    }

    public void setSigHashAlgorithm(String sigHashAlgorithm) {
        this.sigHashAlgorithm = sigHashAlgorithm;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }





}
