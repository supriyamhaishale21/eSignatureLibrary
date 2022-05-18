package com.coreco.esignaturelibrary.Model.responseModel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Text;

@Element(name = "DocSignature")
public class DocSignature {

    @Attribute(name="id")
    public String id;

    @Attribute(name = "sigHashAlgorithm")
    public String sigHashAlgorithm;

    @Attribute(name = "error",required = false)
    public String error;

    @Text(data = true,required = true)
    public String value;

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
