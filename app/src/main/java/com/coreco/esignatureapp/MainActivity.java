package com.coreco.esignatureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.coreco.esignaturelibrary.FetchPdfDetails;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FetchPdfDetails.s(this,"Library included successfully.");
    }
}