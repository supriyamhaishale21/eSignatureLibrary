package com.coreco.esignaturelibrary.Model.responseModel;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

@Element(name = "Signatures")
public class Signatures
{
    @ElementList(name = "DocSignature")
    public List<DocSignature> docSignature;

    public List<DocSignature> getDocSignature() {
        return docSignature;
    }
    public void setDocSignature(List<DocSignature> docSignature) {
        this.docSignature = docSignature;
    }
}
