package com.coreco.esignaturelibrary.Model.xmlResponseModelWA;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

@Element(name="Reference")
public class Reference {
    @Element(name = "Transforms")
    private Transforms transforms;

    @Element(name = "DigestMethod")
    private CanonicalizationMethod digestMethod;

    @Element(name="DigestValue")
    private String digestValue;

    @Attribute(name = "URI")
    private String uri;

    public Transforms getTransforms() {
        return transforms;
    }

    public void setTransforms(Transforms value) {
        this.transforms = value;
    }

    public CanonicalizationMethod getDigestMethod() {
        return digestMethod;
    }

    public void setDigestMethod(CanonicalizationMethod value) {
        this.digestMethod = value;
    }

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String value) {
        this.digestValue = value;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String value) {
        this.uri = value;
    }
}
