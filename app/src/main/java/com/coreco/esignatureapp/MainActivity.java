package com.coreco.esignatureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.coreco.esignaturelibrary.AppendDigitalSignOnPdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> pdf_details=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pdf_details.add("200");

        AppendDigitalSignOnPdf.GetPDFeSignParams(this,"getTxnRefreq.json");
    }
}