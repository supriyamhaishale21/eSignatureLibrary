package com.coreco.esignaturelibrary.Model.responseModel;


import com.coreco.esignaturelibrary.Model.xmlResponseModelWA.Signature;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "EsignResp")
public class EsignResp {
    @Element(name = "UserX509Certificate",required = false)
    public String userX509Certificate;

    @Element(name = "X509Certificate",required = false)
    public String x509Certificate;

    @Element(name = "Signatures",required = false)
    public Signatures signatures;

    @Element(name = "Signature",required = false)
    public Signature signature;

    @Attribute(name= "ts")
    public String ts;

    @Attribute(name= "txn")
    public String txn;

    @Attribute(name= "resCode")
    public String resCode;

    @Attribute(name= "errCode",required = false)
    public String errCode;

    @Attribute(name= "errMsg",required = false)
    public String errMsg;

    @Attribute(name= "ver",required = false)
    public String ver;

    @Attribute(name = "status",required = false)
    public String status;

    @SerializedName("data")
    @Expose
    private DataResponse data;

    public DataResponse getData() {
        return data;
    }

    public void setData(DataResponse data) {
        this.data = data;
    }


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

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(String x509Certificate) {
        this.x509Certificate = x509Certificate;
    }
}
