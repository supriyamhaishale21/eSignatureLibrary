package com.coreco.esignatureapp;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name="Signatures")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Signatures", propOrder={"docSignature"})
public class Signatures
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    @XmlElement(name="DocSignature", required=true)
    protected ArrayList<DocSignature> docSignature;

    public ArrayList<DocSignature> getDocSignature() {
        return docSignature;
    }
    public void setDocSignature(ArrayList<DocSignature> docSignature) {
        this.docSignature = docSignature;
    }
}
