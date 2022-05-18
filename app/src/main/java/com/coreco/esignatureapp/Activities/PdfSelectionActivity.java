package com.coreco.esignatureapp.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.coreco.esignatureapp.Adapters.PageListAdapter;
import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.Constants.FileUtils;
import com.coreco.esignatureapp.Model.Coordinates;
import com.coreco.esignatureapp.Model.PageDetails;
import com.coreco.esignatureapp.R;
import com.coreco.esignaturelibrary.PdfOperations.PdfFileOperations;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PdfSelectionActivity extends AppCompatActivity {
    // Initialize variable
    Button buttonSelect, buttonCalculateHash, buttonMore;
    TextView textUri, textPath, textHash, textSelect, textNoData;
    private ProgressDialog progressDialog;
    RecyclerView recyclerView;
    PageListAdapter pageListAdapter;
    String sPath, sFileName;
    int pageCount = 0, count = 0;
    ArrayList<HashMap<String, PageDetails>> pageDetailsArrayList;
    ArrayList<Coordinates> coordinatesArrayList;
    private static final String LOG_TAG = "Pdf Selection";
    ActivityResultLauncher<Intent> resultLauncher;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    PdfFileOperations pdfFileOperations=new PdfFileOperations();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Pdf Selection");
        setContentView(R.layout.activity_pdf_selection);

        //Initialize tom rush pdf box library
        PDFBoxResourceLoader.init(getApplicationContext());
        init();


        // Initialize result launcher and get appropriate file path
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onActivityResult(
                            ActivityResult result) {
                        // Initialize result data
                        Intent data = result.getData();
                        // check condition
                        if (data != null) {
                            // When data is not equal to empty
                            // Get PDf uri
                            Uri sUri = data.getData();
                            // set Uri on text view
                            textUri.setText(Html.fromHtml(
                                    "<big><b>PDF Uri</b></big><br>"
                                            + sUri));

                            // Get PDF path
                            try {
                                sPath = FileUtils.getPath(PdfSelectionActivity.this, sUri);
                                sFileName = FileUtils.getFileName(sUri);
                                PDDocument doc = PDDocument.load(new File(sPath));
                                pageCount = doc.getNumberOfPages();
                                Log.d("count: ", String.valueOf(pageCount));
                                Log.d("sPath: ", sPath);
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Error: " + e);
                                Toast.makeText(PdfSelectionActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }

                            // Set path on text view
                            textPath.setText(Html.fromHtml(
                                    "<big><b>PDF Path</b></big><br>"
                                            + sPath + "\n\nPDF Json Inputs received from ASP Application"));

                            textSelect.setText("Pdf file is uploaded.");
                            buttonCalculateHash.setEnabled(false);
                            buttonSelect.setEnabled(false);
                            buttonMore.setEnabled(true);

                        }
                    }
                });

        /**
         * Button on click for Pdf hash Calculation and generating encrypted key
         *
         */
        buttonCalculateHash.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {


                try {
                    // Get Json Input data

                    // this method converts a list to JSON Array
                   /* String signDetails = "\"signaturedetails\":[";

                    for (int i = 0; i < pageDetailsArrayList.size(); i++) {
                        HashMap<String, PageDetails> hashDetails = pageDetailsArrayList.get(i);

                        for (Map.Entry<String, PageDetails> entry : hashDetails.entrySet()) {
                            String k = entry.getKey();
                            PageDetails pageDetails = entry.getValue();

                            signDetails += "{\"page:\"" + pageDetails.getPage() + "\",";

                            signDetails += "\"coordinates\":[";
                            for (int j = 1; j <= pageDetails.getCoordinates().size(); j++) {
                                Log.d("Value of J: ", String.valueOf(j));
                                Log.d("Value of Coordinates: ", String.valueOf(pageDetails.getCoordinates().size()));
                                if(j==pageDetails.getCoordinates().size()) {
                                    Log.d("","Inside if");
                                    Log.d("signDetails",signDetails);
                                    Log.d("signDetails X",pageDetails.getCoordinates().get(j).getX());
                                    signDetails += "{\"x\":\"" + pageDetails.getCoordinates().get(j).getX() + "\",\"y\":\"" + pageDetails.getCoordinates().get(j).getY() +
                                            "\",\"w\":\"" + pageDetails.getCoordinates().get(j).getW() + "\",\"h\":\"" + pageDetails.getCoordinates().get(j).getH() + "\"}";
                                }
                                else
                                {
                                    signDetails += "{\"x\":\"" + pageDetails.getCoordinates().get(j).getX() + "\",\"y\":\"" + pageDetails.getCoordinates().get(j).getY() +
                                            "\",\"w\":\"" + pageDetails.getCoordinates().get(j).getW() + "\",\"h\":\"" + pageDetails.getCoordinates().get(j).getH() + "\"},";
                                }
                            }
                            signDetails += "]},";
                        }
                    }
                    signDetails += "]";

                    Log.d("signDetails", signDetails.toString());
                    */


                    String pdfInputJson = AspConstants.getJsonInput(PdfSelectionActivity.this, sPath, sFileName, pageCount);


                    // Parse Json and Calculate hash value of PDF based on Signature coordinates.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        progressDialog.setTitle("Calculating Hash");
                        progressDialog.setMessage("Processing Data.....");
                        progressDialog.show();

                        // Performing all hash oprations also getting base 64 value of txn and rescode
                        JSONObject jsonResponse =pdfFileOperations.getTxnRef(PdfSelectionActivity.this,
                                pdfInputJson,AspConstants.ESPBASEURL,textHash);

                        progressDialog.cancel();


                        JSONObject resJsonObject = (JSONObject) jsonResponse;
                        String txnRef=resJsonObject.get("txnref").toString();
                        Log.d("txnRef",txnRef);
                        String txn=resJsonObject.get("txn").toString();
                        Log.d("txn",txn);
                        String status=resJsonObject.get("status").toString();
                        String resCode=resJsonObject.get("responseCode").toString();

                        if(status.equals("1")) {
                            navigateToAuthPage(txnRef, txn,resCode);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * Button click to select PDF file from storage
         */

        // Set click listener on button
        buttonSelect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check condition
                        if (ActivityCompat.checkSelfPermission(
                                PdfSelectionActivity.this,
                                Manifest.permission
                                        .READ_EXTERNAL_STORAGE)
                                != PackageManager
                                .PERMISSION_GRANTED) {
                            // When permission is not granted
                            // Result permission
                            ActivityCompat.requestPermissions(
                                    PdfSelectionActivity.this,
                                    new String[]{
                                            Manifest.permission
                                                    .READ_EXTERNAL_STORAGE},
                                    1);
                        } else {
                            // When permission is granted
                            // Create method
                            try {
                                selectPDF();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        // when clicked
        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // count++;

                // if (pageCount != count) {

                ImageView buttonCancel;
                EditText editPage, editX, editY, editW, editH;
                Button buttonClear, buttonSave;
                //will create a view of our custom dialog layout
                View alertCustomdialog = LayoutInflater.from(PdfSelectionActivity.this).inflate(R.layout.add_details, null);
                //initialize alert builder.
                AlertDialog.Builder alert = new AlertDialog.Builder(PdfSelectionActivity.this);

                //set our custom alert dialog to tha alertdialog builder
                alert.setView(alertCustomdialog);
                buttonCancel = alertCustomdialog.findViewById(R.id.buttonCancel);
                buttonClear = alertCustomdialog.findViewById(R.id.buttonClear);
                buttonSave = alertCustomdialog.findViewById(R.id.buttonSave);
                editPage = alertCustomdialog.findViewById(R.id.editPage);
                editX = alertCustomdialog.findViewById(R.id.editX);
                editY = alertCustomdialog.findViewById(R.id.editY);
                editW = alertCustomdialog.findViewById(R.id.editW);
                editH = alertCustomdialog.findViewById(R.id.editH);

                final AlertDialog dialog = alert.create();
                //this line removed app bar from dialog and make it transperent and you see the image is like floating outside dialog box.
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                //finally show the dialog box in android all
                dialog.show();
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                buttonClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editPage.setText("");
                        editX.setText("");
                        editY.setText("");
                        editW.setText("");
                        editH.setText("");
                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (editPage.getText().toString().equals("") || editX.getText().toString().equals("") || editY.getText().toString().equals("") || editW.getText().toString().equals("") || editH.getText().toString().equals("")) {
                            Toast.makeText(PdfSelectionActivity.this, "Fill the details", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, PageDetails> hashMap = new HashMap<>();
                            coordinatesArrayList = new ArrayList<>();
                            recyclerView.setVisibility(View.VISIBLE);
                            textNoData.setVisibility(View.GONE);
                            coordinatesArrayList.add(new Coordinates(editX.getText().toString(), editY.getText().toString(), editW.getText().toString(), editH.getText().toString()));
                            hashMap.put(editPage.getText().toString(), new PageDetails(editPage.getText().toString(), coordinatesArrayList));
                            pageDetailsArrayList.add(hashMap);
                            pageListAdapter = new PageListAdapter(pageDetailsArrayList);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(PdfSelectionActivity.this));
                            recyclerView.setAdapter(pageListAdapter);
                            buttonCalculateHash.setEnabled(true);
                            buttonSelect.setEnabled(false);
                            buttonMore.setText("Add More");

                            dialog.dismiss();

                        }
                    }
                });
            }

        });


    }

    /**
     * Initialize UI fields
     */
    public void init() {
        // assign variable
        buttonSelect = findViewById(R.id.buttonSelect);
        buttonSelect.setEnabled(true);
        buttonCalculateHash = findViewById(R.id.buttonCalculateHash);
        buttonCalculateHash.setEnabled(false);
        textSelect = findViewById(R.id.textSelect);
        textUri = findViewById(R.id.textUri);
        textPath = findViewById(R.id.textPath);
        textHash = findViewById(R.id.textHash);
        buttonMore = findViewById(R.id.buttonMore);
        progressDialog = new ProgressDialog(PdfSelectionActivity.this);
        pageDetailsArrayList = new ArrayList<>();
        coordinatesArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        textNoData = findViewById(R.id.textNoData);

        recyclerView.setVisibility(View.GONE);
        textNoData.setVisibility(View.VISIBLE);
    }

    /**
     * Select Pdf file from storage
     */

    private void selectPDF() throws IOException {
//
//        File file=AspConstants.getFileFromAssets(this,"sample.pdf");
//        sPath = file.getAbsolutePath();
//
//        // Set path on text view
//        textPath.setText(Html.fromHtml(
//                "<big><b>PDF Path</b></big><br>"
//                        + sPath));
//
//        textSelect.setText("Pdf file is uploaded.");
//
//

        buttonCalculateHash.setEnabled(true);
        buttonSelect.setEnabled(false);

        // Initialize intent
        Intent intent
                = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // set type
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Launch intent
        resultLauncher.launch(intent);
    }

    public void navigateToAuthPage(String txnRef, String txn, String resCode) {

        Intent intent = new Intent(PdfSelectionActivity.this, AuthPageActivity.class);
        intent.putExtra("txnref", txnRef);
        intent.putExtra("txn", txn);
        intent.putExtra("resCode", resCode);
        startActivity(intent);

    }


}