package com.coreco.esignaturelibrary.Model.responseModel;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Attribute;


public class EsignResponse {

    @SerializedName("data")
    @Expose
    private DataResponse data;

    @Attribute(name= "errCode",required = false)
    public String errCode;

    @Attribute(name= "errMsg",required = false)
    public String errMsg;

    @Attribute(name= "ver",required = false)
    public String ver;

    @Attribute(name = "status")
    public String status;


    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


