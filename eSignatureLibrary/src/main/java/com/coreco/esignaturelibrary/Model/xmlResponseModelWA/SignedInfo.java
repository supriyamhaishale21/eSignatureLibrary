package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

public class SignedInfo {
    private CanonicalizationMethod canonicalizationMethod;
    private CanonicalizationMethod signatureMethod;
    private Reference reference;

    public CanonicalizationMethod getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    public void setCanonicalizationMethod(CanonicalizationMethod value) {
        this.canonicalizationMethod = value;
    }

    public CanonicalizationMethod getSignatureMethod() {
        return signatureMethod;
    }

    public void setSignatureMethod(CanonicalizationMethod value) {
        this.signatureMethod = value;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference value) {
        this.reference = value;
    }
}
