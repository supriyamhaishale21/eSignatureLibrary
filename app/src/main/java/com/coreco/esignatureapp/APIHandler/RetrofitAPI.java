package com.coreco.esignatureapp.APIHandler;

import android.text.Html;
import android.util.Log;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitAPI {
    // as we are making a post request to post a data
    // so we are annotating it with post
    // and along with that we are passing a parameter as users
    @FormUrlEncoded
    @POST("esp/authpage")

    //on below line we are creating a method to post our data.
    Call<ResponseBody> navigateToAuthPage(@Field("txnref") String base64TxnRef);
}
