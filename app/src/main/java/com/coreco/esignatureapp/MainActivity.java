package com.coreco.esignatureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.coreco.esignaturelibrary.FetchPdfDetails;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> pdf_details=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pdf_details.add("200");

        FetchPdfDetails.fetchDetails(this,pdf_details);
    }
}