package com.coreco.esignatureapp.Activities;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coreco.esignatureapp.APIHandler.RetrofitAPI;
import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.R;
import com.coreco.esignaturelibrary.Model.responseModel.DataResponse;
import com.coreco.esignaturelibrary.Model.responseModel.EsignResp;
import com.coreco.esignaturelibrary.PdfOperations.PdfFileOperations;
import java.io.File;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppendSignStatusActivity extends AppCompatActivity {
    ImageView imageStatus;
    TextView textStatus;
    Button buttonShow;
    String txnVal="";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_append_sign_status);
        init();

         txnVal=getIntent().getExtras().getString("txn");
        Log.d("txn: ",txnVal);
        // Get response from webpage
        getXmlResponse(txnVal);


        // Verify Storage Access
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPDF();
            }
        });
    }

    /**
     * Initialization of Views
     */
    public void init()
    {
        imageStatus=findViewById(R.id.imageStatus);
        textStatus=findViewById(R.id.textStatus);
        buttonShow=findViewById(R.id.buttonShow);
    }

    /**
     * Retrieve the final xm response
     */
    public void getXmlResponse(String txnVal) {
        progressDialog=new ProgressDialog(AppendSignStatusActivity.this);
        progressDialog.setMessage("Appending Signature....");
        progressDialog.show();

        Log.d("Inside","getXmlResponse");
        Retrofit retrofit = new Retrofit.Builder().baseUrl(AspConstants.ASPBASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("Url: ",retrofit.baseUrl().toString());
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<EsignResp> stringResponse = retrofitAPI.receiveEsignRsponse(txnVal);
        Log.d("stringResponse",stringResponse.toString());

        stringResponse.enqueue(new Callback<EsignResp>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EsignResp> call, Response<EsignResp> response) {
                if(response.isSuccessful() && response.body().getData()!=null) {
                    progressDialog.cancel();

                    EsignResp responseReceived = response.body();
                    DataResponse dataResponse=responseReceived.getData();

                    Log.d("responseReceived:", dataResponse.getXmlData());


                    //if(responseReceived.getStatus().equals("1"))
                    //{
                        PdfFileOperations pdfFileOperations=new PdfFileOperations();
                        String resObject=pdfFileOperations.signPdf(AppendSignStatusActivity.this,dataResponse.getXmlData());

                        if(resObject!="") {
                            Integer imgResId = R.drawable.img_success;

                            imageStatus.setImageResource(imgResId);
                            textStatus.setText("Signature is appended on PDF Successfully.");
                        }

                }
                else
                {
                    Log.d("responseReceived:", "Response is empty");
                }
            }

            @Override
            public void onFailure(Call<EsignResp> call, Throwable t) {
                Log.e("failure: ",t.getMessage());
                progressDialog.cancel();
                 Integer imgResId = R.drawable.img_error;

                imageStatus.setImageResource(imgResId);

                textStatus.setText("Signature is not appended on PDF.");


            }
        });

    }

    /**
     * Access pdf from storage and using to Intent get options to view application in available applications.
     */

    private void openPDF() {

        // Get the File location and file name.
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/Signature Pdf Details/SignedPDF/"+ txnVal +"/" + "1/tempSignFinal.pdf");
        Log.d("pdfFIle", "" + file);

        // Get the URI Path of file.
        Uri uriPdfPath = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Log.d("pdfPath", "" + uriPdfPath);

        // Start Intent to View PDF from the Installed Applications.
        Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
        pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenIntent.setClipData(ClipData.newRawUri("", uriPdfPath));
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf");
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |  Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            startActivity(pdfOpenIntent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(this,"There is no app to load corresponding PDF",Toast.LENGTH_LONG).show();

        }
    }
}