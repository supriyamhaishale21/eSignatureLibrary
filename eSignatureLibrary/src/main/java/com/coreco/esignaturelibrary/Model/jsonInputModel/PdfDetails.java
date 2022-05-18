package com.coreco.esignaturelibrary.Model.jsonInputModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class PdfDetails {

    @SerializedName("tempInfoPath")
    @Expose
    private String tempInfoPath;
    @SerializedName("signerid")
    @Expose
    private String signerid;
    @SerializedName("redirectUrl")
    @Expose
    private String redirectUrl;
    @SerializedName("responseUrl")
    @Expose
    private String responseUrl;
    @SerializedName("txn")
    @Expose
    private String txn;
    @SerializedName("aspId")
    @Expose
    private String aspId;
    @SerializedName("pfxPath")
    @Expose
    private String pfxPath;
    @SerializedName("pfxPassword")
    @Expose
    private String pfxPassword;
    @SerializedName("pfxAlias")
    @Expose
    private String pfxAlias;
    @SerializedName("signingAlgorithm")
    @Expose
    private String signingAlgorithm;
    @SerializedName("maxWaitPeriod")
    @Expose
    private String maxWaitPeriod;
    @SerializedName("ver")
    @Expose
    private String ver;
    @SerializedName("AuthMode")
    @Expose
    private String authMode;
    @SerializedName("fileType")
    @Expose
    private String fileType;
    @SerializedName("pdfdetails")
    @Expose
    private List<Pdfdetail> pdfdetails = null;

    public String getTempInfoPath() {
        return tempInfoPath;
    }

    public void setTempInfoPath(String tempInfoPath) {
        this.tempInfoPath = tempInfoPath;
    }

    public String getSignerid() {
        return signerid;
    }

    public void setSignerid(String signerid) {
        this.signerid = signerid;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getAspId() {
        return aspId;
    }

    public void setAspId(String aspId) {
        this.aspId = aspId;
    }

    public String getPfxPath() {
        return pfxPath;
    }

    public void setPfxPath(String pfxPath) {
        this.pfxPath = pfxPath;
    }

    public String getPfxPassword() {
        return pfxPassword;
    }

    public void setPfxPassword(String pfxPassword) {
        this.pfxPassword = pfxPassword;
    }

    public String getPfxAlias() {
        return pfxAlias;
    }

    public void setPfxAlias(String pfxAlias) {
        this.pfxAlias = pfxAlias;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public String getMaxWaitPeriod() {
        return maxWaitPeriod;
    }

    public void setMaxWaitPeriod(String maxWaitPeriod) {
        this.maxWaitPeriod = maxWaitPeriod;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<Pdfdetail> getPdfdetails() {
        return pdfdetails;
    }

    public void setPdfdetails(List<Pdfdetail> pdfdetails) {
        this.pdfdetails = pdfdetails;
    }

}

