package com.coreco.esignaturelibrary.Model;

import java.util.ArrayList;

public class PdfDetails {
    String pdfbase64val, docInfo, docUrl, reason, coordinates, pagenos;
    ArrayList<SignatureDetails> signaturedetails;

    public PdfDetails(String pdfbase64val, String docInfo, String docUrl,
                      String reason, String coordinates, String pagenos,
                      ArrayList<SignatureDetails> signaturedetails) {
        this.pdfbase64val = pdfbase64val;
        this.docInfo = docInfo;
        this.docUrl = docUrl;
        this.reason = reason;
        this.coordinates = coordinates;
        this.pagenos = pagenos;
        this.signaturedetails = signaturedetails;
    }

    public String getPdfbase64val() {
        return pdfbase64val;
    }

    public void setPdfbase64val(String pdfbase64val) {
        this.pdfbase64val = pdfbase64val;
    }

    public String getDocInfo() {
        return docInfo;
    }

    public void setDocInfo(String docInfo) {
        this.docInfo = docInfo;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getPagenos() {
        return pagenos;
    }

    public void setPagenos(String pagenos) {
        this.pagenos = pagenos;
    }

    public ArrayList<SignatureDetails> getSignaturedetails() {
        return signaturedetails;
    }

    public void setSignaturedetails(ArrayList<SignatureDetails> signaturedetails) {
        this.signaturedetails = signaturedetails;
    }
}
