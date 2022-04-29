package com.coreco.esignatureapp.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.coreco.esignatureapp.R;


public class AuthPageActivity extends AppCompatActivity {
    //private Handler handlerForJavascriptInterface = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String html_content="<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<body>\n" +
                "<form id=\"authPageForm\" action=\"https://esignuat.vsign.in/esp/authpage\" method=\"post\">\n" +
                "<br> <input type=\"text\" id=\"txnref\" name=\"txnref\" value=\"VlNZU1RFU1Q6MjkwNDIwMjIxMDIzMjM0MjMwMDA6NTYwMXwwZjdjNWU3OTNkOWI4NTYyOWFhZjAyMzRmNzY5MmZhYg==\"> \n" +
                "<br> <input type=\"submit\" value=\"Submit\"> </form>\n" +
                "</div> </body>\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "function formAutoSubmit () {\n" +
                "\n" +
                "var frm = document.getElementById(\"authPageForm\");\n" +
                "\n" +
                "frm.submit();\n" +
                "\n" +
                "}\n" +
                "\n" +
                "window.onload = formAutoSubmit;\n" +
                "</script>\n" +
                "</html>";
        Log.d("Html Content: ",html_content);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);
        WebView web_view = findViewById(R.id.web_view);
        web_view.requestFocus();

        web_view.getSettings().setLightTouchEnabled(true);
        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.getSettings().setGeolocationEnabled(true);
        web_view.setSoundEffectsEnabled(true);

        web_view.loadData(html_content,"text/html","UTF-8");

        web_view.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {

            }
        });

        //web_view.loadUrl(html_content);
    }

//    class MyJavaScriptInterface
//    {
//        private Context ctx;
//
//        MyJavaScriptInterface(Context ctx)
//        {
//            this.ctx = ctx;
//        }
//
//        @JavascriptInterface
//        public void showHTML(String html)
//        {
//            //code to use html content here
//            handlerForJavascriptInterface.post(new Runnable() {
//                @Override
//                public void run()
//                {
//                    Toast toast = Toast.makeText(AuthPageActivity
//                    .this, "Page has been loaded in webview. html content :"+html, Toast.LENGTH_LONG);
//                    toast.show();
//                }});
//        }
//    }
}