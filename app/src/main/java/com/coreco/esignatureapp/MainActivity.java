package com.coreco.esignatureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coreco.esignaturelibrary.AppendDigitalSignOnPdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> pdf_details=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView txtJsonInput=findViewById(R.id.txtJsonInput);
        TextView txtTitle=findViewById(R.id.txtTitle);
        TextView txtXMLRequest=findViewById(R.id.txtXMLRequest);

        Button btnXmlRequest=findViewById(R.id.btnXmlRequest);

       String pdfJsonInput="{\"tempInfoPath\":\"C:/Users/Admin/Desktop/esign/AspDotNetSample/temp/\"" +
               ",\"signerid\":\"\",\"redirectUrl\":\"http://localhost:55691/esignresposen.aspx\"" +
               ",\"responseUrl\":\"http://localhost:55691/esignresposen.aspx\"" +
               ",\"txn\":\"abc1234561237776274\",\"aspId\":\"VTL001\",\"pfxPath\"" +
               ":\"C:/Users/Admin/Desktop/esign/demo/Prashant_Lad_Signature_ds.pfx\"" +
               ",\"pfxPassword\":\"12345678\",\"pfxAlias\":\"prashant v lad\"" +
               ",\"signingAlgorithm\":\"RSA\",\"maxWaitPeriod\":\"1440\"" +
               ",\"ver\":\"21\",\"AuthMode\":\"1\",\"fileType\":\"path\"," +
               "\"pdfdetails\":" +
               "[{\"pdfbase64val\":\"C:\\\\Users\\\\Admin\\\\Desktop\\\\esign\\\\AspDotNetSample" +
               "\\\\NEE1111121666.pdf\",\"docInfo\":\"eSignSignedFile.pdf\"," +
               "\"docUrl\":\"https://esign.verasys.in/\",\"reason\":\"Signedby 63 moon\"," +
               "\"coordinates\":\"30,700,225,60\",\"pagenos\":\"1\"," +
               "\"signaturedetails\":[{\"page\":\"2,3\"," +
               "\"coordinates\":[{\"x\":\"30\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
               "{\"x\":\"330\",\"y\":\"700\",\"w\":\"225\",\"h\":\"60\"}," +
               "{\"x\":\"30\",\"y\":\"110\",\"w\":\"225\",\"h\":\"60\"}]}]}]}";

        txtJsonInput.setText(pdfJsonInput);


        btnXmlRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppendDigitalSignOnPdf.GetPDFeSignParams(MainActivity.this,pdfJsonInput,txtJsonInput,txtTitle,txtXMLRequest);
            }
        });

    }
}