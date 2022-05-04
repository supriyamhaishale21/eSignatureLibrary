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
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.PdfHash.GeneratePDFHash;
import com.coreco.esignatureapp.R;
import com.tom_roush.pdfbox.io.RandomAccessFile;
import com.tom_roush.pdfbox.util.Hex;

import org.bouncycastle.oer.its.SequenceOfPsidGroupPermissions;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;


public class AuthPageActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String txnref=getIntent().getExtras().getString("txnref");


        String html_content="<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
                "<body>\n" +
                "<form id=\"authPageForm\" action=\"https://esignuat.vsign.in/esp/authpage\" method=\"post\">\n" +
                "<br> <input type=\"text\" id=\"txnref\" name=\"txnref\" value=\""+txnref+"\"> \n" +
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


        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.setWebViewClient(new PageWebViewClient());
        web_view.setWebChromeClient(new PageWebChromeClient());



        web_view.loadData(html_content,"text/html","UTF-8");


        //web_view.loadUrl("javascript:verifyOTP()");

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

        @Override
        public void onPageStarted(WebView view, String url,
                                  android.graphics.Bitmap favicon) {
            logLine(view, "onPageStarted() called: url = " + url);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onPageFinished(WebView view, String url) {
            logLine(view, "onPageFinished() called: url = " + url);
            //String response = getData(url);
            String data=("%3C%3Fxml+version%3D%221.0%22+encoding%3D%22UTF-8%22%3F%3E%3CEsignResp+resCode%3D%22f989b2e736c40e29f526019d4021c6fa%22+status%3D%221%22+ts%3D%222022-05-02T14%3A45%3A18.929%2B05%3A30%22+txn%3D%22VSYSTEST%3A02052022024518933000%3A3834%22%3E%3CUserX509Certificate%3EMIIF1zCCBL%2BgAwIBAgIGSC2c%2Bf6%2BMA0GCSqGSIb3DQEBCwUAMHQxCzAJBgNVBAYTAklOMScwJQYDVQQKEx5WZXJhc3lzIFRlY2hub2xvZ2llcyBQdnQuIEx0ZC4xHTAbBgNVBAsTFENlcnRpZnlpbmcgQXV0aG9yaXR5MR0wGwYDVQQDExRWZXJhc3lzIERldiBlU2lnbiBDQTAeFw0yMjA1MDIwOTE2MjhaFw0yMjA1MDIwOTQ1MjhaMIIBZDELMAkGA1UEBhMCSU4xDzANBgNVBBETBjQxNjE0MzEUMBIGA1UECBMLTWFoYXJhc2h0cmExGDAWBgNVBAkTD05lYXIgV2F0ZXIgVGFuazESMBAGA1UEBxMJQWJkdWwgTGF0MVIwUAYDVQQtA0kAMDExMTE3NTlkczZQdXJGQlB4ZjJkUjViaTFPaEdtMW5BbkJDZCtmOTU3bGRIM21uN0g0cUlGVDM1c2l2d3FHbkk4YTJhOE9pMU4wTAYDVQQuE0UxOTg5RjMwMGRlZTQwODFmYjI4NTljMzY0Njg0ZjU1NTY2NzM0MDEwY2RiZDlmNzA1MDg4OTBhYzYxZDRlOGVlY2Y1MTkxKTAnBgNVBEETIGRkMzZkNDU3ZjBmMzRkZmY5YmJkYzUwNDk1NzMyNTY2MQ0wCwYDVQQMEwQxNTE2MSIwIAYDVQQDExlTdXByaXlhIEF2aW5hc2ggTWhhaXNoYWxlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgvRkQ3RHpcFg%2FEu%2FQQpyRYYn0qSTVxIqN8lgRMSjpQD72CMVIWXPm0ugtZwM6btqR1kmiE5LD9KOOsBhoX6VSOrb%2FLB%2FnX%2Fom1%2FE9gVE7XSzy2ILeiIGj4cC3uWe6hFqR6qmTaZgmN0CsvOJiL0res2DZRD2Jh1NN%2BBYhx9qJHAGYmO5WxZyh05WU9eyaeNnREs1C1oOWFehNFT2kvLXMciWmrE%2FNjiAAaaJF%2Bw%2FA9ssilVeBNhXpMSw8Gv32GT2NZx%2Fy2WWytXu8e%2BH2nJ%2BT%2BVAMFtiUruj1niBznlp3Fs1Idp%2BJy6sITI44tdQ63CiUPId1Vd5JJqcRbnLK3Q7VQIDAQABo4IBezCCAXcwEwYDVR0jBAwwCoAIQKWvGt03gkwwTgYIKwYBBQUHAQEEQjBAMD4GCCsGAQUFBzAChjJodHRwczovL3d3dy52c2lnbi5pbi9yZXBvc2l0b3J5L1ZlcmFzeXNlU2lnbkNBLmNlcjCBrQYDVR0gBIGlMIGiMIGfBgdggmRkAgQBMIGTMC8GCCsGAQUFBwIBFiNodHRwczovL3d3dy52c2lnbi5pbi9yZXBvc2l0b3J5L2NwczBgBggrBgEFBQcCAjBUGlJBYWRoYWFyLU9ubGluZS1lS1lDLU9UUCBDbGFzcyBDZXJ0aWZpY2F0ZSBpc3N1ZWQgYnkgVmVyYXN5cyBUZWNobm9sb2dpZXMgUHZ0LiBMdGQuMDIGA1UdHwQrMCkwJ6AloCOGIWh0dHBzOi8vZGV2LnZzaWduLmluL2NybC9lc2lnbmNybDARBgNVHQ4ECgQISg3loSTUyPMwDgYDVR0PAQH%2FBAQDAgbAMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggEBADxgGooe%2BJQBjyFYzONTlsT9gG8qd9mrfSu357FvQv6udfL%2BcYc0006LcEIkZVGy2%2B%2B78TFh%2Bnvy0tePZ1QO77egFB%2F9wB%2Ba8wIDZE9TBjArHWVlsvL3hl75VpQx1%2BPb6OxcCDrquXdGYOL%2FnCuX%2Fvs21xgXHiQ%2FvGzsX6ScpdK0bQ9udLKUkxv5AdEJrmFqIJVlAEeseFWzEZAnGHYDz7et3p%2B7tYOyM%2Bje8VumfbTk298aCLQHOIZlr1i8hzy3%2FCQfGfHF92Y5mHDzXwFaAlA0bDhfYG7TM5Aj%2BJrEQ%2FISMCbruBCyZTdFNkm8AUkYo7O%2Fq9IiSigtL24chorLdYo%3D%3C%2FUserX509Certificate%3E%3CSignatures%3E%3CDocSignature+id%3D%221%22+sigHashAlgorithm%3D%22SHA256%22%3EMIIe8wYJKoZIhvcNAQcCoIIe5DCCHuACAQExDTALBglghkgBZQMEAgEwCwYJKoZIhvcNAQcBoIIMnTCCBdcwggS%2FoAMCAQICBkgtnPn%2BvjANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwJJTjEnMCUGA1UEChMeVmVyYXN5cyBUZWNobm9sb2dpZXMgUHZ0LiBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEdMBsGA1UEAxMUVmVyYXN5cyBEZXYgZVNpZ24gQ0EwHhcNMjIwNTAyMDkxNjI4WhcNMjIwNTAyMDk0NTI4WjCCAWQxCzAJBgNVBAYTAklOMQ8wDQYDVQQREwY0MTYxNDMxFDASBgNVBAgTC01haGFyYXNodHJhMRgwFgYDVQQJEw9OZWFyIFdhdGVyIFRhbmsxEjAQBgNVBAcTCUFiZHVsIExhdDFSMFAGA1UELQNJADAxMTExNzU5ZHM2UHVyRkJQeGYyZFI1YmkxT2hHbTFuQW5CQ2QrZjk1N2xkSDNtbjdINHFJRlQzNXNpdndxR25JOGEyYThPaTFOMEwGA1UELhNFMTk4OUYzMDBkZWU0MDgxZmIyODU5YzM2NDY4NGY1NTU2NjczNDAxMGNkYmQ5ZjcwNTA4ODkwYWM2MWQ0ZThlZWNmNTE5MSkwJwYDVQRBEyBkZDM2ZDQ1N2YwZjM0ZGZmOWJiZGM1MDQ5NTczMjU2NjENMAsGA1UEDBMEMTUxNjEiMCAGA1UEAxMZU3Vwcml5YSBBdmluYXNoIE1oYWlzaGFsZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIL0ZEN0R6XBYPxLv0EKckWGJ9Kkk1cSKjfJYETEo6UA%2B9gjFSFlz5tLoLWcDOm7akdZJohOSw%2FSjjrAYaF%2BlUjq2%2Fywf51%2F6JtfxPYFRO10s8tiC3oiBo%2BHAt7lnuoRakeqpk2mYJjdArLziYi9K3rNg2UQ9iYdTTfgWIcfaiRwBmJjuVsWcodOVlPXsmnjZ0RLNQtaDlhXoTRU9pLy1zHIlpqxPzY4gAGmiRfsPwPbLIpVXgTYV6TEsPBr99hk9jWcf8tllsrV7vHvh9pyfk%2FlQDBbYlK7o9Z4gc55adxbNSHaficurCEyOOLXUOtwolDyHdVXeSSanEW5yyt0O1UCAwEAAaOCAXswggF3MBMGA1UdIwQMMAqACEClrxrdN4JMME4GCCsGAQUFBwEBBEIwQDA%2BBggrBgEFBQcwAoYyaHR0cHM6Ly93d3cudnNpZ24uaW4vcmVwb3NpdG9yeS9WZXJhc3lzZVNpZ25DQS5jZXIwga0GA1UdIASBpTCBojCBnwYHYIJkZAIEATCBkzAvBggrBgEFBQcCARYjaHR0cHM6Ly93d3cudnNpZ24uaW4vcmVwb3NpdG9yeS9jcHMwYAYIKwYBBQUHAgIwVBpSQWFkaGFhci1PbmxpbmUtZUtZQy1PVFAgQ2xhc3MgQ2VydGlmaWNhdGUgaXNzdWVkIGJ5IFZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dC4gTHRkLjAyBgNVHR8EKzApMCegJaAjhiFodHRwczovL2Rldi52c2lnbi5pbi9jcmwvZXNpZ25jcmwwEQYDVR0OBAoECEoN5aEk1MjzMA4GA1UdDwEB%2FwQEAwIGwDAJBgNVHRMEAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQA8YBqKHviUAY8hWMzjU5bE%2FYBvKnfZq30rt%2Bexb0L%2BrnXy%2FnGHNNNOi3BCJGVRstvvu%2FExYfp78tLXj2dUDu%2B3oBQf%2FcAfmvMCA2RPUwYwKx1lZbLy94Ze%2BVaUMdfj2%2BjsXAg66rl3RmDi%2F5wrl%2F77NtcYFx4kP7xs7F%2BknKXStG0PbnSylJMb%2BQHRCa5haiCVZQBHrHhVsxGQJxh2A8%2B3rd6fu7WDsjPo3vFbpn205NvfGgi0BziGZa9YvIc8t%2FwkHxnxxfdmOZhw818BWgJQNGw4X2Bu0zOQI%2FiaxEPyEjAm67gQsmU3RTZJvAFJGKOzv6vSIkooLS9uHIaKy3WKMIIDRTCCAi2gAwIBAgIQd4BhRp1UILTCuzXvn%2Bsx%2FjANBgkqhkiG9w0BAQsFADBEMQswCQYDVQQGEwJJTjERMA8GA1UEChMIQ0NBIFRlc3QxDDAKBgNVBAsTA1BLSTEUMBIGA1UEAxMLQ0NBIFRlc3QgQ0EwHhcNMjAwNTA2MTUxMDU4WhcNMzAwNTA2MTUxMDU4WjBEMQswCQYDVQQGEwJJTjERMA8GA1UEChMIQ0NBIFRlc3QxDDAKBgNVBAsTA1BLSTEUMBIGA1UEAxMLQ0NBIFRlc3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCpGtfFC3Gmcy7UJEivpWChtmLOVXEE%2F%2BY71Bb5eV3Nc%2BkGRCBaIyL6Pg90LkAKQSS7ClhAZ15YIzfgBkzeUvtjBS2ufrORJYYk1odPGCujJFE2U3oCKmxL8UzHShpuqkQYx4%2FSbo27%2F8%2FeIvJ3YSd52ZHrepw6p2uMTvGUfvXEr2l%2BJSQmLmZTwlAv%2BpyH9CmO0nzfDqBtQOnBapAmzQZovgA92gT%2FcxxUWxsQLtUoH4Y4NOq4kvlLobB6HdLjWTtdEy0fDUtd6ZSIz8zTjkxU5Oyz3XVfzoRvKD%2FKrKDQxTVJ%2FjXx6KuGMUmcqOXSHdTHnG8u8kl%2BtZBtfILVCzjRAgMBAAGjMzAxMA8GA1UdEwEB%2FwQFMAMBAf8wEQYDVR0OBAoECE5erllH4SP9MAsGA1UdDwQEAwIBBjANBgkqhkiG9w0BAQsFAAOCAQEAOEPEOn9Xc%2FS6sw0Je52B%2FSL704gp9xi3wl7hOEAN6aDXoMCLKrvfJ6YAlO1JnwPOxHU5jmnhbZ%2B32SMfSY4j2yPAXMRhobKtCOHNCNsVBPj8%2FrSQaWD%2B3wxMvzKjxQjnNgX%2B5NOk6hGvyfr0dIKQWhE9NPlj33mBZwZZfqxNo0SYJw1DVf01vah1dBSU0f9juT1SGpXay5DX9wPSHGIrUyWGqHgHTVapQrXcvA37820kVutDPez386jLDe3Hmsoz51OSsjm2rKp%2F1xhYidqMCqI9syjC296lHssFE3R%2BNrl34co6WhwnwGNE8tlt7oaBUeaB9yK2NXx9YinDtXQZWzCCA3UwggJdoAMCAQICEEX%2FrKz3tK%2FgfWoke2LM6O4wDQYJKoZIhvcNAQELBQAwRDELMAkGA1UEBhMCSU4xETAPBgNVBAoTCENDQSBUZXN0MQwwCgYDVQQLEwNQS0kxFDASBgNVBAMTC0NDQSBUZXN0IENBMB4XDTIwMDUwNjE1MTA1OFoXDTMwMDMwNTA2MzAwMFowdDELMAkGA1UEBhMCSU4xJzAlBgNVBAoTHlZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dC4gTHRkLjEdMBsGA1UECxMUQ2VydGlmeWluZyBBdXRob3JpdHkxHTAbBgNVBAMTFFZlcmFzeXMgRGV2IGVTaWduIENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxbf8HPfzo9d95WW%2BYU%2FBYzEJwobv3E2oI9efoC%2FNenrvSgiCKvG9JE%2B7gDxWAHWnjTRqGoAzlXaq1KTDzH9DqBwEym8RlzMqgOBreOCOh%2B3sb7aqPh5E7zCHLOEp8wqEJObERKptYsF8mqNrM%2BC2jOJ3KJBLGG2iB4m7iijTMK5IXfHLbHc7bBKpMJj2dkAf6muD58CvNh%2FLMNO1pSK1vgp9fROY3JHtPkp894f4boPc%2B6G1r%2BAmCF%2B4Qco66S4O4rKjxFmU5pwevRRhxO3BqfNLflQACXOloeI2IwlGjONyk7%2FF48AeNG2J%2BNto8qklsbmVRzALGFU49P%2BkwCDQhQIDAQABozMwMTAPBgNVHRMBAf8EBTADAQH%2FMBEGA1UdDgQKBAhApa8a3TeCTDALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQELBQADggEBAE6JaHhLd%2B0QD6dc68vazuDtDaDYJoAmz6eANglDtN28ufmINCkKsb2x%2Fz0KK1jGVbcHFFLJi5pj%2F2J7%2FZzF%2FEgSE%2FWQgHvX6FHvHggN7HF2u00YEh4tQ7Z6buUbh9TD8hum240fXLDg6jdk2zADIfwRPIdrZ%2BDOJt08G%2Fv%2FsSb%2BQth5oIKQwafS3y184k5bUZK0XYP6C4eDraDVChb0Syig8vmpK%2B4PgOWbjGuA8hp71HyTGegUUE8yE5BUv%2FBon0keBcwE647kEPMzIOe7LbvehOP6V61JTIEgt88dA%2FZLmn00aXe7H27tImuIHo6d3rQJRm6%2FnmJFsDOgkbQ5jDGhggJwMIICbDCCAVQCAQEwDQYJKoZIhvcNAQELBQAwdDELMAkGA1UEBhMCSU4xJzAlBgNVBAoTHlZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dC4gTHRkLjEdMBsGA1UECxMUQ2VydGlmeWluZyBBdXRob3JpdHkxHTAbBgNVBAMTFFZlcmFzeXMgRGV2IGVTaWduIENBFw0yMDEyMjIxMTM0NDNaFw0yMDEyMjMxMzM0NDNaMIGFMCECEBscXrnCLjBeJ4kcpu1NZIcXDTIwMDYwOTEwNTkwOFowLwIQYvAwg6P0tfh04X4gfv%2BDdBcNMjAxMDAyMTMwNzA2WjAMMAoGA1UdFQQDCgEGMC8CEG4789kGY0ieUVPVmrMiQr8XDTIwMDYwMjE3MDQyN1owDDAKBgNVHRUEAwoBBqAkMCIwCwYDVR0UBAQCAgCBMBMGA1UdIwQMMAqACEClrxrdN4JMMA0GCSqGSIb3DQEBCwUAA4IBAQBJp%2BhCodm%2BFRWGeUNTMPeVW%2BdgbjNFTbdLU9aNbwHFc3kcaplvNsX1YJzZFhbnASNH6ZTtvx3xCtb8T2jP%2B40YArDhnGWPKrVAprnu5r%2FKDLaQ9ADuEC%2B8gzbFCoZ37%2FD2acbUXzy6Wv%2FoY54dLC3mUv5YsO6gdquM7MCuWaC0NnBtydDSbLcR3FlWcmE1gsTQhT5xhAW764alfO6IYzTDoyGw5YSv3F7boVGI27ekgTRSKe7nHCtQ9gGOT%2BIIe06jc0NVHoAslsh2pFdtoM%2Fj%2Fw850slMt7iiOCqglHtSuyK1Oeu3pjdF3bxidrID%2B1vbV8O%2BEBVrLvLL22c23idgMYIPqDCCD6QCAQEwfjB0MQswCQYDVQQGEwJJTjEnMCUGA1UEChMeVmVyYXN5cyBUZWNobm9sb2dpZXMgUHZ0LiBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEdMBsGA1UEAxMUVmVyYXN5cyBEZXYgZVNpZ24gQ0ECBkgtnPn%2BvjALBglghkgBZQMEAgGgggPIMBgGCSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8XDTIyMDUwMjA5MTYyOVowLwYJKoZIhvcNAQkEMSIEIEXADOJwHXi%2FMRNx9RKSXRpgElgsWY9eNC2tRQ0uzfIcMIHNBgsqhkiG9w0BCRACLzGBvTCBujCBtzCBtAQgeNgNotwgbi5vh504kBteMgJAYyeDfExYDFmSbOFbi1YwgYIweKR2MHQxCzAJBgNVBAYTAklOMScwJQYDVQQKEx5WZXJhc3lzIFRlY2hub2xvZ2llcyBQdnQuIEx0ZC4xHTAbBgNVBAsTFENlcnRpZnlpbmcgQXV0aG9yaXR5MR0wGwYDVQQDExRWZXJhc3lzIERldiBlU2lnbiBDQQIGSC2c%2Bf6%2BMAsGCWCGSAFlAwQCATCCAosGCSqGSIb3LwEBCDGCAnwwggJ4oIICdDCCAnAwggJsMIIBVAIBATANBgkqhkiG9w0BAQsFADB0MQswCQYDVQQGEwJJTjEnMCUGA1UEChMeVmVyYXN5cyBUZWNobm9sb2dpZXMgUHZ0LiBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEdMBsGA1UEAxMUVmVyYXN5cyBEZXYgZVNpZ24gQ0EXDTIwMTIyMjExMzQ0M1oXDTIwMTIyMzEzMzQ0M1owgYUwIQIQGxxeucIuMF4niRym7U1khxcNMjAwNjA5MTA1OTA4WjAvAhBi8DCDo%2FS1%2BHThfiB%2B%2F4N0Fw0yMDEwMDIxMzA3MDZaMAwwCgYDVR0VBAMKAQYwLwIQbjvz2QZjSJ5RU9WasyJCvxcNMjAwNjAyMTcwNDI3WjAMMAoGA1UdFQQDCgEGoCQwIjALBgNVHRQEBAICAIEwEwYDVR0jBAwwCoAIQKWvGt03gkwwDQYJKoZIhvcNAQELBQADggEBAEmn6EKh2b4VFYZ5Q1Mw95Vb52BuM0VNt0tT1o1vAcVzeRxqmW82xfVgnNkWFucBI0fplO2%2FHfEK1vxPaM%2F7jRgCsOGcZY8qtUCmue7mv8oMtpD0AO4QL7yDNsUKhnfv8PZpxtRfPLpa%2F%2Bhjnh0sLeZS%2Fliw7qB2q4zswK5ZoLQ2cG3J0NJstxHcWVZyYTWCxNCFPnGEBbvrhqV87ohjNMOjIbDlhK%2FcXtuhUYjbt6SBNFIp7uccK1D2AY5P4gh7TqNzQ1UegCyWyHakV22gz%2BP%2FDznSyUy3uKI4KqCUe1K7IrU567emN0XdvGJ2sgP7W9tXw74QFWsu8svbZzbeJ2AwDQYJKoZIhvcNAQELBQAEggEAKoFJjEY%2Bq2K0ncm5rdX3aRdZsmLoUSDwLnT9glrLqfLm6jRIUOs5BKJIzk8KV49peSOr9GMpct4KTpSnV%2FFheC5iMKbv0VvHZl0WvADtweVBi9QFlztB1PWKVvi%2BfnBTBtjzfmdWIJ6SIDUwRWWw6ZGkmEDbM%2BxKUTcf3YJ0mYayLfVSPNLF85EYfvjXryLgOvdd0QzLoMNCmSqzR1E%2BfFg0ihSI4jKR6v7oV6HEMbgtvN5ktfIg6QMLh7EXPKwNPyKYbYIGYhpz6%2B%2FqRZ08c3PHPZkNk1LCKkGhhwnLO%2BdW3bQZxtT%2BVDG1OkpfNQfequORigdLlpbkvfMpzHplO6GCCjEwggotBgsqhkiG9w0BCRACDjGCChwwggoYBgkqhkiG9w0BBwKgggoJMIIKBQIBAzENMAsGCWCGSAFlAwQCATCCAQkGCyqGSIb3DQEJEAEEoIH5BIH2MIHzAgEBBgZggmRkAwAwLzALBglghkgBZQMEAgEEIMqOEsSurk276Bt3DZLjducDmf%2B4tovbISbK0w%2FSwpGRAgwGiJnJ43MfgBh0HUkYDzIwMjIwNTAyMDkxNjI5WgIJALjyFrqkh6vKoIGKpIGHMIGEMQswCQYDVQQGEwJJTjEdMBsGA1UECxMUQ2VydGlmeWluZyBBdXRob3JpdHkxLTArBgNVBAoTJFZlcmFzeXMgVGVjaG5vbG9naWVzIFByaXZhdGUgTGltaXRlZDEnMCUGA1UEAxMeVmVyYXN5cyBUaW1lU3RhbXBpbmcgQXV0aG9yaXR5oIIFUzCCBU8wggQ3oAMCAQICBkiKQJ%2FYeDANBgkqhkiG9w0BAQsFADCB3TELMAkGA1UEBhMCSU4xJjAkBgNVBAoTHVZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dCBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEPMA0GA1UEERMGNDAwMDI1MRQwEgYDVQQIEwtNYWhhcmFzaHRyYTESMBAGA1UECRMJVi5TLiBNYXJnMTIwMAYDVQQzEylPZmZpY2UgTm8uIDIxLCAybmQgRmxvb3IsIEJoYXZuYSBCdWlsZGluZzEYMBYGA1UEAxMPVmVyYXN5cyBDQSAyMDE0MB4XDTIwMDUxNTA2MTczNFoXDTIyMDUxNTA2MTczNFowgYQxCzAJBgNVBAYTAklOMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEtMCsGA1UEChMkVmVyYXN5cyBUZWNobm9sb2dpZXMgUHJpdmF0ZSBMaW1pdGVkMScwJQYDVQQDEx5WZXJhc3lzIFRpbWVTdGFtcGluZyBBdXRob3JpdHkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCXn222nK8HQIIcPiRAInR4P8B7NBZvBChfmvAHVT%2Fdv0Y654fFTAzS82wPIs9vWsY%2BxULqJjDZZHA1EYzqhVAiTtaC4cTcKtBjoM9KNqXDyKWQa2ZEEPQ%2BUHVdbrElEMvsiy5mxM7Un%2B%2BrQAeg69xiN3%2Bog9%2B3NchjSfyWxrGTSYjQmym3aSt%2Fo%2BbEMqhC%2FVgCX%2F1ZWUHcjEA%2BdDdsTI7SWYzSey3MI6lxAJPCsmAcjwsMF3YvpCriqJqYC3Q7ro35erLWrrwtoxHwPvV%2BdYdQrIeQf80W5IzOvpT9ueeEy%2FBdGYY5lklEDTUyO%2Bt0QNIxQj1jflg8kp8sXuHlzNrZAgMBAAGjggFqMIIBZjAWBgNVHSUBAf8EDDAKBggrBgEFBQcDCDARBgNVHQ4ECgQIRdfsOLRoPEEwfwYDVR0gBHgwdjB0BgZggmRkAgMwajAvBggrBgEFBQcCARYjaHR0cHM6Ly93d3cudnNpZ24uaW4vcmVwb3NpdG9yeS9jcHMwNwYIKwYBBQUHAgIwKxopVmVyYXN5cyBDQSAyMDE0IFRpbWUgU3RhbXBpbmcgQ2VydGlmaWNhdGUwEwYDVR0jBAwwCoAISoaoo1R1i8IwaQYIKwYBBQUHAQEEXTBbMCAGCCsGAQUFBzABhhRodHRwOi8vb2NzcC52c2lnbi5pbjA3BggrBgEFBQcwAoYraHR0cHM6Ly93d3cudnNpZ24uaW4vcmVwb3NpdG9yeS92c2lnbmNhLmNlcjAOBgNVHQ8BAf8EBAMCB4AwKAYDVR0fBCEwHzAdoBugGYYXaHR0cHM6Ly9jYS52c2lnbi5pbi9jcmwwDQYJKoZIhvcNAQELBQADggEBAC79bSLDAM%2F7wcriu66lMY9Nj%2BTsbEZ9gwkhUfE7fA9ObLUuor3w0epNztV2dSu5riuLYLBzSWYbGtLa0ERFopDTTR2pfk68RNukPg9Q1OFQLo%2F%2FCJYHT%2Fp0wHMhfJiAMxP8nIOHFmYHduDTr5sZq3g%2FojCFwRkgG%2BTOYaEUEks0M6vMpvYOqreohNcrf9d8T%2FkRgD%2BeorDINAw2GaNSpEfuZ2XqQBQD8Jgu3VovtwzrAVDmTcsUKU0EofaeE1YGT1LyVPUBSLYM1jB2CVBPKXGVWwywXz5h%2B9ClfrpFEqMhrNj4TSHJwLFBO0EKRDfhHIIjGxiO77q99XKSLIKRbVcxggOLMIIDhwIBATCB6DCB3TELMAkGA1UEBhMCSU4xJjAkBgNVBAoTHVZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dCBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEPMA0GA1UEERMGNDAwMDI1MRQwEgYDVQQIEwtNYWhhcmFzaHRyYTESMBAGA1UECRMJVi5TLiBNYXJnMTIwMAYDVQQzEylPZmZpY2UgTm8uIDIxLCAybmQgRmxvb3IsIEJoYXZuYSBCdWlsZGluZzEYMBYGA1UEAxMPVmVyYXN5cyBDQSAyMDE0AgZIikCf2HgwCwYJYIZIAWUDBAIBoIIBdTAaBgkqhkiG9w0BCQMxDQYLKoZIhvcNAQkQAQQwLwYJKoZIhvcNAQkEMSIEICQv8TXahgFPcdwatN7VuYbyG91dKMICC7vDoEGJVHntMIIBJAYLKoZIhvcNAQkQAgwxggETMIIBDzCCAQswggEHBBTR72NCtexHbeyqX81NwbwxdCh4MTCB7jCB46SB4DCB3TELMAkGA1UEBhMCSU4xJjAkBgNVBAoTHVZlcmFzeXMgVGVjaG5vbG9naWVzIFB2dCBMdGQuMR0wGwYDVQQLExRDZXJ0aWZ5aW5nIEF1dGhvcml0eTEPMA0GA1UEERMGNDAwMDI1MRQwEgYDVQQIEwtNYWhhcmFzaHRyYTESMBAGA1UECRMJVi5TLiBNYXJnMTIwMAYDVQQzEylPZmZpY2UgTm8uIDIxLCAybmQgRmxvb3IsIEJoYXZuYSBCdWlsZGluZzEYMBYGA1UEAxMPVmVyYXN5cyBDQSAyMDE0AgZIikCf2HgwDQYJKoZIhvcNAQELBQAEggEANQdwBvbTJG5Bg%2BJv8kaXWG7%2BW1IjEblEGz5kK%2FqxXRmBJQh1OA%2BBR8oHXQ3jQEvZtevUFads7WiMsrOsquj2YkEGIXolOIkBOgi2Y7imFcXqAuXblHMYM5eC7VVlS9D6m6X8A%2FCQrZ4OH0rSPMkbBw8fiWKSTd74uxVq8TbJU6BMZi47HQf%2F6V0wRQ8KZahcJZak29uA6XvEN1PA%2Fqjo%2B%2BGAqk4aGkAe3THSlhSHEtKyMmh6ayGBNbI7jUQ48A8%2F0oXHMe%2BsbDOuc6SZFF0f30RwPWkieqoRzgi%2BtIi7iuZWLlFSJviBoXLeHH9%2BQw%2FrCr0CZ4sHy%2FCpkM%2Fms%2By%2Bsw%3D%3D%3C%2FDocSignature%3E%3C%2FSignatures%3E%3CSignature+xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23%22%3E%3CSignedInfo%3E%3CCanonicalizationMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2FTR%2F2001%2FREC-xml-c14n-20010315%23WithComments%22%2F%3E%3CSignatureMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23rsa-sha1%22%2F%3E%3CReference+URI%3D%22%22%3E%3CTransforms%3E%3CTransform+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23enveloped-signature%22%2F%3E%3C%2FTransforms%3E%3CDigestMethod+Algorithm%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmlenc%23sha256%22%2F%3E%3CDigestValue%3EBnHZc4B%2BuK72B9%2FDtSruzrtA7g%2FKG9ORWP88GIeE0SI%3D%3C%2FDigestValue%3E%3C%2FReference%3E%3C%2FSignedInfo%3E%3CSignatureValue%3EoSGbDihFcaqyQwuVKnY5ACKfbxYg9Q%2BLp77fQakXdC%2B%2BGd13NjGGdnn3FYea%2FJb1MAwXYBvilWIHsm%2B8GwnqtO7K9iFpKDm4fAgelDJ%2FQ%2BPFDYyCqVhNbs4tm3fsXJOaUoIi5kIZgb0f0IW7fyVPkcHbRxtBJ6P%2FxgrDJaJmwzYBEFui%2Fz1yj49VLhUC%2B9M%2Bqmf1wgbDoUxEm85DIu98Ip3at6aI3GaN1HYyIuhDe69ywjaqcQ1yzTFhEDrssRMF9W0YcDmN1DoQrj9%2FTqJhUydyZjt7kopLAwjVJ%2BmBAKJ3moN1lPIlTngGH5cf8HljSrC%2BP7ThP2l3wOgux22gkg%3D%3D%3C%2FSignatureValue%3E%3CKeyInfo%3E%3CX509Data%3E%3CX509SubjectName%3ECN%3DneXus+eSign+Provider%2CO%3DTechnology+Nexus+AB%2CC%3DIN%3C%2FX509SubjectName%3E%3CX509Certificate%3EMIIDSzCCAjOgAwIBAgICJyowDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCSU4xHDAaBgNVBAoTE1RlY2hub2xvZ3kgTmV4dXMgQUIxHjAcBgNVBAMTFU9mZmljZXIgYW5kIHN5c3RlbSBDQTAeFw0xNTA4MDcwNTE1MjZaFw0xNzA4MDcwNTE1MjZaMEoxCzAJBgNVBAYTAklOMRwwGgYDVQQKExNUZWNobm9sb2d5IE5leHVzIEFCMR0wGwYDVQQDExRuZVh1cyBlU2lnbiBQcm92aWRlcjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANsF2Z7lG9cs5rBxoqrspUULmxzJCHqdw4%2Faz1TlvgvZsVDSAYiPP7xAWMeisYCOAVYjJq2xEAR3Y3UkBnj31%2BpTgTKCvgRFU%2F1bWvxo6H7%2BqkykuO506bKy3E%2FHaXUtUCEylWSb3LI%2FAyCmnVQ%2FsQelFKnp91tsJzlcv%2B3bJcYcyv2jVmV%2F%2Bh5BMC%2Fx7EugsV4Lfcd0Q1m7u38aUVA2L57yKN2VQFaMLOyjoEz6sjilZ1VvG4WWhIv4vZ27vGuPSIfWmg8gkhXxzfhSlonTehangOLOzVx%2BOw6KIZbWD1ZGt1ujlfC0nt%2B3XSMIL%2Fy2yk68bjZZw3DD38BQYX94k2kCAwEAAaM6MDgwEQYDVR0OBAoECEipSQfyvOClMBMGA1UdIwQMMAqACEdcWBbGA%2BTMMA4GA1UdDwEB%2FwQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAvtXMr2P4uOnyG1WyNIre5pZymEN87FV%2FH%2F5DwzjVtlt8%2F0AUyVdC%2FzeNgj0WhQq4O7Ls2kH56UwH7g2ijIET1YbFRAS5weM%2BeE2R1TxZgrpwwXE3gF8zYL3aB0WdSychF2Q15EbVYQsusZ8zzavERm0o1XKQej51KnumPYzeafLN1YAX5Yz%2Bk1hv6dXalbNVkt6MkQVm6LuVs1S2lnJCYVq%2BX2ny1wo3JUN7Bhw729XQrxuq%2FNE3kc%2B6dI7M%2FuViQBbvkwg7oNxScpLdlPA2OYpZoezRg2ep%2FTD6gYIytww64wf50jbgrg2VrL6d8eqy1Y4%2Fdnz7qkGj2B0C64oJHQ%3D%3D%3C%2FX509Certificate%3E%3C%2FX509Data%3E%3C%2FKeyInfo%3E%3C%2FSignature%3E%3C%2FEsignResp%3E").trim();

            String decodedUrl=AspConstants.decodeUrlStringData(data);
            Log.d("Response: ", decodedUrl);

            GeneratePDFHash generatePDFHash=new GeneratePDFHash();
            Log.d("Append Signature","Inside append sign pdf method");
            generatePDFHash.signPdfv2(decodedUrl);
        }

        public void onLoadResource(WebView view, String url) {
            logLine(view, "onLoadResource() called: url = " + url);
        }

        public void logLine(WebView view, String msg) {
            try {
                FileOutputStream fos =
                        view.getContext()
                                .openFileOutput("Activity.log", Context.MODE_APPEND);
                OutputStreamWriter out = new OutputStreamWriter(fos);
                out.write((new Date()).toString() + ": " + msg + "\n");
                out.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        }
    }

    /**
     * This class allows you to listen to JavaScript calls, notifications of the current page,
     * such as console messages, warnings, page refresh progress, and other JavaScript calls.
     * Using WebChromeClient we can handle JS events.
     */

    private class PageWebChromeClient extends WebChromeClient {
        public boolean onConsoleMessage(ConsoleMessage cmsg) {
            /* process JSON */
            cmsg.message();
            return true;

        }

    }

        String getData(String url) throws IOException {
          //  HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            //add headers to the connection, or check the status if desired..

            // handle error response code it occurs
           // int responseCode = connection.getResponseCode();

            InputStream inputStream=new URL(url).openStream();;

//            if (200 <= responseCode && responseCode <= 299) {
//                inputStream = connection.getInputStream();
//            } else {
//                inputStream = connection.getErrorStream();
//            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            inputStream));

            StringBuilder response = new StringBuilder();
            String currentLine;

            while ((currentLine = in.readLine()) != null)
                response.append(currentLine);

            in.close();

            return response.toString();
        }
}