package com.coreco.esignatureapp.APIHandler;


import com.coreco.esignaturelibrary.Model.responseModel.EsignResp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitAPI {
    // as we are making a post request to post a data
    // so we are annotating it with post
    // and along with that we are passing a parameter as users
    @GET("verasys/getCallbackXMLDetails")

    //on below line we are creating a method to post our data.
    Call<EsignResp> receiveEsignRsponse(@Query("txn") String txn);
}
