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
import android.widget.Toast;

import com.coreco.esignatureapp.R;


public class AuthPageActivity extends AppCompatActivity {
    //private Handler handlerForJavascriptInterface = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String html_content=getIntent().getExtras().getString("html_content");
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
      //  web_view.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        //String strCrome = "Mozilla/5.0 (Linux; U; Android-4.0.3; en-us; Xoom Build/IML77) AppleWebKit/535.7 (KHTML, like Gecko) CrMo/16.0.912.75 Safari/535.7";

        //web_view.getSettings().setUserAgentString(strCrome); web_view.getSettings().setSupportZoom(true);
        web_view.loadData( Html.fromHtml(html_content).toString(),"text/html","UTF-8");

        web_view.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                //web_view.loadUrl("javascript:window.HtmlViewer.showHTML" +html_content);
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