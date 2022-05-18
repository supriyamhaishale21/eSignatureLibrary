package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

@ElementList(name = "CanonicalizationMethod , DigestMethod")
public class CanonicalizationMethod {
    @Attribute(name = "algorithm")
    private String algorithm;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String value) {
        this.algorithm = value;
    }
}
