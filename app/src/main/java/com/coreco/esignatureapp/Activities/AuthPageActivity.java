package com.coreco.esignatureapp.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.R;



public class AuthPageActivity extends AppCompatActivity {

    String txnVal="",resCode="";
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_auth_page);

        String txnref = getIntent().getExtras().getString("txnref");
        txnVal=getIntent().getExtras().getString("txn");
        resCode=getIntent().getExtras().getString("resCode");
        Log.d("txnVal",txnVal);

        //Get html content to load Auth Page
        String htmlContent =AspConstants.getHtml(txnref);
        Log.d("Html Content: ", htmlContent);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);
        WebView web_view = findViewById(R.id.web_view);
        web_view.requestFocus();


        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.setWebViewClient(new PageWebViewClient());
        web_view.addJavascriptInterface(new MyJavaScriptInterface(AuthPageActivity.this), "Android");

        web_view.loadData(htmlContent, "text/html", "UTF-8");
    }

    /**
     * WebViewClient allows you to listen to web page events, for example, when it starts loading, or
     * finished loading when an error has occurred related to page loading, form submission, links, and other events.
     */
    private class PageWebViewClient extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.e("Load Signup page", description);
            Toast.makeText(
                    AuthPageActivity.this,
                    "Problem loading. Make sure internet connection is available." + "\n\nDescription: " + description
                            + "\n\nfailingUrl: " + failingUrl + "\n\nerrorCode: " + errorCode,
                    Toast.LENGTH_SHORT).show();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onPageFinished(WebView view, String url) {
            Log.d("onPageFinished", "onPageFinished() called: url = " + url);

        }

        public void onLoadResource(WebView view, String url) {

        }


    }

    /**
     * This class calls javascript method defined in webpage and perform action on it.
     */
    class MyJavaScriptInterface {
        Context mContext;

        MyJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void getUserVerifiedEsignResponse(String toast) {
            finish();
            Intent intent=new Intent(AuthPageActivity.this,AppendSignStatusActivity.class);
            intent.putExtra("resCode",resCode);
            intent.putExtra("txn",txnVal);
            startActivity(intent);

        }

    }

}