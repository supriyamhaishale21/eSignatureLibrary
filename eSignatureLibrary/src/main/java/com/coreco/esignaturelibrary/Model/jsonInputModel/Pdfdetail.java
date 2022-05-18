package com.coreco.esignaturelibrary.Model.jsonInputModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Pdfdetail {

    @SerializedName("pdfbase64val")
    @Expose
    private String pdfbase64val;
    @SerializedName("docInfo")
    @Expose
    private String docInfo;
    @SerializedName("docUrl")
    @Expose
    private String docUrl;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("coordinates")
    @Expose
    private String coordinates;
    @SerializedName("pagenos")
    @Expose
    private String pagenos;
    @SerializedName("signaturedetails")
    @Expose
    private List<Signaturedetail> signaturedetails = null;

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

    public List<Signaturedetail> getSignaturedetails() {
        return signaturedetails;
    }

    public void setSignaturedetails(List<Signaturedetail> signaturedetails) {
        this.signaturedetails = signaturedetails;
    }

}
