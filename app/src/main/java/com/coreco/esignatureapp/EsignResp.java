package com.coreco.esignatureapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="EsignResp")
@XmlAccessorType(XmlAccessType.FIELD)
public class EsignResp {


    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlElement(name="UserX509Certificate", required=true)
    protected String userX509Certificate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlElement(name="Signatures", required=true)
    protected Signatures signatures;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="ver", required=true)
    protected String ver;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="ts", required=true)
    protected String ts;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="txn", required=true)
    protected String txn;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="resCode", required=true)
    protected String resCode;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="error", required=true)
    protected String error;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlAttribute(name="status", required=true)
    protected String status;

    public String getUserX509Certificate() {
        return userX509Certificate;
    }

    public void setUserX509Certificate(String userX509Certificate) {
        this.userX509Certificate = userX509Certificate;
    }

    public Signatures getSignatures() {
        return signatures;
    }

    public void setSignatures(Signatures signatures) {
        this.signatures = signatures;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
