package com.coreco.esignaturelibrary.Model;

import java.util.ArrayList;

public class eSignPdfDetails {
    String tempInfoPath,signerid,redirectUrl,responseUrl,txn,aspId,pfxPath,pfxPassword,pfxAlias,signingAlgorithm;
    String maxWaitPeriod,ver,AuthMode,fileType;
    ArrayList<PdfDetails> pdfdetails;

    public eSignPdfDetails(String tempInfoPath, String signerid, String redirectUrl, String responseUrl, String txn, String aspId, String pfxPath, String pfxPassword, String pfxAlias, String signingAlgorithm,
                           String maxWaitPeriod, String ver, String authMode, String fileType, ArrayList<PdfDetails> pdfdetails) {
        this.tempInfoPath = tempInfoPath;
        this.signerid = signerid;
        this.redirectUrl = redirectUrl;
        this.responseUrl = responseUrl;
        this.txn = txn;
        this.aspId = aspId;
        this.pfxPath = pfxPath;
        this.pfxPassword = pfxPassword;
        this.pfxAlias = pfxAlias;
        this.signingAlgorithm = signingAlgorithm;
        this.maxWaitPeriod = maxWaitPeriod;
        this.ver = ver;
        AuthMode = authMode;
        this.fileType = fileType;
        this.pdfdetails = pdfdetails;
    }

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
        return AuthMode;
    }

    public void setAuthMode(String authMode) {
        AuthMode = authMode;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public ArrayList<PdfDetails> getPdfdetails() {
        return pdfdetails;
    }

    public void setPdfdetails(ArrayList<PdfDetails> pdfdetails) {
        this.pdfdetails = pdfdetails;
    }
}

