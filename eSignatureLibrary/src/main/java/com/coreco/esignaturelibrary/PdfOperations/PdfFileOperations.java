package com.coreco.esignaturelibrary.PdfOperations;

import android.app.Activity;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.coreco.esignaturelibrary.Constants.AspConstants;
import com.coreco.esignaturelibrary.Model.responseModel.DocSignature;
import com.coreco.esignaturelibrary.Model.responseModel.EsignResp;
import com.coreco.esignaturelibrary.R;
import com.coreco.esignaturelibrary.XmlSignedInfoHash.XmlDigitalSigner;
import com.tom_roush.harmony.awt.geom.AffineTransform;
import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.io.RandomAccessFile;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDField;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDSignatureField;
import com.tom_roush.pdfbox.util.Hex;
import com.tom_roush.pdfbox.util.Matrix;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfFileOperations implements SignatureInterface {
    private static File imageFile;


    @RequiresApi(api = Build.VERSION_CODES.N)
    public Map<String, Object> generatePdfHash(Activity context, byte[] srcFileVal, String destPath,
                                               String ticImagePath,
                                               String signerName, String signingReason,
                                               String signingLocation,
                                               String signatureFieldName,
                                               float x, float y, float w, float h,
                                               boolean hashLowerCase, String infoPath,
                                               int signingTimeDelay, int signaturePredefinedSize,
                                               String pageNumber, String pdfSrcPath) throws IOException, NoSuchAlgorithmException {


        try {

            int pageNum = 0;

            // Creates destination path of pdf

            FileOutputStream destOutStream = new FileOutputStream(destPath); //Use the stream as usual to write into the file.


            String hashForExSigning = null;
            HashMap<String, Object> response = new HashMap<String, Object>();

            PDDocument doc;
            if (srcFileVal != null) {
                doc = PDDocument.load(srcFileVal);
            } else {
                File srcPdfFile = new File(pdfSrcPath);
                if (srcPdfFile == null || !srcPdfFile.exists()) {
                    throw new IOException("Document for signing does not exist : " + pdfSrcPath);
                }
                doc = PDDocument.load(srcPdfFile);
            }


            int accessPermissions = SigUtils.getMDPPermission(doc);
            if (accessPermissions == 1) {
                throw new IllegalStateException(
                        "No changes to the document are permitted due to DocMDP transform parameters dictionary");
            }

            PDSignature signature = null;
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDRectangle rect = null;

            if (acroForm != null && acroForm.getNeedAppearances()) {
                if (acroForm.getFields().isEmpty()) {
                    acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
                } else {
                    Log.i("/NeedAppearances is set"," signature may be ignored by Adobe Reader");
                }
            }

            if (signatureFieldName != null) {
                signature = findExistingSignature(acroForm, signatureFieldName);
                if (signature != null) {
                    rect = acroForm.getField(signatureFieldName).getWidgets().get(0).getRectangle();
                }
            }

            if (signature == null) {
                // create signature dictionary
                signature = new PDSignature();
            }

            if (rect == null) {
                RectF humanRect = new RectF(x, y, w, h);
                rect = createSignatureRectangle(doc, humanRect, pageNum);
            }


            // default filter
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);

            // subfilter for basic and PAdES Part 2 signatures
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

            signature.setName(signerName);
            signature.setLocation(signingLocation);
            signature.setReason(signingReason);

            Calendar signingTime = Calendar.getInstance();
            signingTime.add(Calendar.SECOND, signingTimeDelay);
            // the signing date, needed for valid signature
            signature.setSignDate(signingTime);

            // set null if external signing will be done, else need to implement
            // SignatureInterface
            SignatureInterface signatureInterface = this;

            // register signature dictionary and sign interface
            SignatureOptions signatureOptions = new SignatureOptions();
            signatureOptions.setVisualSignature(createVisualSignatureTemplateWithoutImage(doc, pageNum, rect, signature));
            signatureOptions.setPage(pageNum);

            if (signaturePredefinedSize > 0) {
                signatureOptions.setPreferredSignatureSize(signaturePredefinedSize);
            }

            doc.addSignature(signature, signatureInterface, signatureOptions);

            String totalSignatures = String.valueOf(doc.getSignatureDictionaries().size());

            Log.d("totalSignatures: ", totalSignatures);

            ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(destOutStream);

            byte[] dataForSign = IOUtils.toByteArray(externalSigning.getContent());

            externalSigning.getContent().close();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sha256Hash = digest.digest(dataForSign);

            hashForExSigning = Hex.getString(sha256Hash);
            // if (isLateExternalSigning())
            // {
            // this saves the file with a 0 signature
            externalSigning.setSignature(new byte[0]);

            // remember the offset (add 1 because of "<")
            int offset = signature.getByteRange()[1] + 1;

            doc.close();

            // Do not close signatureOptions before saving, because some COSStream objects
            // within
            // are transferred to the signed document.
            // Do not allow signatureOptions get out of scope before saving, because then
            // the COSDocument
            // in signature options might by closed by gc, which would close COSStream
            // objects prematurely.
            // See https://issues.apache.org/jira/browse/PDFBOX-3743
            IOUtils.closeQuietly(signatureOptions);

            if (hashLowerCase)
                response.put("hash", hashForExSigning.toLowerCase());
            else
                response.put("hash", hashForExSigning.toUpperCase());

//			String pnoData=String.valueOf(offset)+"|" +pno;

            String pnoData = String.valueOf(offset) + "|" + pageNumber + "|" + totalSignatures + "|" + x + "|" + y + "|" + w + "|" + h;
            Log.d("pnoData: ", pnoData);

            if (infoPath != null) {
                FileOutputStream fosSigInfo = new FileOutputStream(infoPath);
                fosSigInfo.write(pnoData.getBytes());
                fosSigInfo.close();
            } else {
                response.put("offset", offset);
            }
            return response;

        } catch (Exception e) {
            System.err.println("Exception" + e);
        }
        return null;
    }

    /**
     * create a template PDF document with empty signature and return it as a
     * stream.
     *
     * @param srcDoc
     * @param pageNum
     * @param rect
     * @param signature
     * @return
     * @throws IOException
     */

    private InputStream createVisualSignatureTemplateWithoutImage(PDDocument srcDoc, int pageNum, PDRectangle rect,
                                                                  PDSignature signature) throws IOException {
        try {
            PDDocument doc = new PDDocument();

            PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
            doc.addPage(page);
            PDAcroForm acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            Log.d("signatureField: ", signatureField.toString());
            PDAnnotationWidget widget = signatureField.getWidgets().get(0);
            List<PDField> acroFormFields = acroForm.getFields();
            acroForm.setSignaturesExist(true);
            acroForm.setAppendOnly(true);
            acroForm.getCOSObject().setDirect(true);
            acroFormFields.add(signatureField);

            widget.setRectangle(rect);

            // from PDVisualSigBuilder.createHolderForm()
            PDStream stream = new PDStream(doc);
            PDFormXObject form = new PDFormXObject(stream);
            PDResources res = new PDResources();
            form.setResources(res);
            form.setFormType(1);
            PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
            float height = bbox.getHeight();
            Matrix initialScale = null;
            switch (srcDoc.getPage(pageNum).getRotation()) {
                case 90:
                    form.setMatrix(AffineTransform.getRotateInstance(1));
                    initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(),
                            bbox.getHeight() / bbox.getWidth());
                    height = bbox.getWidth();
                    break;
                case 180:
                    form.setMatrix(AffineTransform.getRotateInstance(2));
                    break;
                case 270:
                    form.setMatrix(AffineTransform.getRotateInstance(3));
                    initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(),
                            bbox.getHeight() / bbox.getWidth());
                    height = bbox.getWidth();
                    break;
                case 0:
                default:
                    break;
            }
            form.setBBox(bbox);

            // from PDVisualSigBuilder.createAppearanceDictionary()
            PDAppearanceDictionary appearance = new PDAppearanceDictionary();
            appearance.getCOSObject().setDirect(true);
            PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
            appearance.setNormalAppearance(appearanceStream);
            widget.setAppearance(appearance);

            PDPageContentStream cs = new PDPageContentStream(doc, appearanceStream);

            // for 90 and 270scale ratio of width / height
            // not really sure about this
            // why does scale have no effect when done in the form matrix???
            if (initialScale != null) {
                cs.transform(initialScale);
            }

            // show background (just for debugging, to see the rect size + position)
            //cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(-5000, -5000, 10000, 10000);
            // cs.fill();

            // show background image
            // save and restore graphics if the image is too large and needs to be scaled
            cs.saveGraphicsState();
            cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
//		PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, doc);
//		cs.drawImage(img, 0, 0);
            cs.restoreGraphicsState();

            // show text
            float fontSize = 10;
            float leading = fontSize * 1.5f;
            PDFont font = PDType1Font.TIMES_ITALIC;
            Log.d("PDFont: ", font.toString());
//		PDFont font = PDType1Font.HELVETICA;

            cs.beginText();
            cs.setFont(font, fontSize);
            Log.d("PDFont1: ", "font is set");
            //cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(fontSize, height - leading);
            cs.setLeading(leading);

            // X509Certificate cert = (X509Certificate) getCertificateChain()[0];

            // https://stackoverflow.com/questions/2914521/
            // X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
            // RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
            // String name = IETFUtils.valueToString(cn.getFirst().getValue());

            // See https://stackoverflow.com/questions/12575990
            // for better date formatting
            String name = signature.getName();
            String reason = signature.getReason();
            cs.showText("Digitally Signed by");
            cs.newLine();

            cs.showText("Name : " + name);
            cs.newLine();

            reason = reason.replaceAll("\r", "").replaceAll("\n\n", "\n");
            reason = reason.replaceAll("\n", "\\|");
            for (String str : reason.split("\\|")) {
                cs.showText("Reason : " + str);
                cs.newLine();
            }

            String date = signature.getSignDate().getTime().toString();
            cs.showText("Date : " + date);
            cs.newLine();

            cs.endText();
            cs.close();

            // no need to set annotations and /P entry
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            doc.close();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception ex) {
            Log.e("Exception: ", ex.getMessage());
        }
        return null;
    }


    /**
     * Find an existing signature (assumed to be empty). You will usually not need this.
     *
     * @param acroForm
     * @param sigFieldName
     * @return
     */
    private PDSignature findExistingSignature(PDAcroForm acroForm, String sigFieldName) throws IllegalStateException {
        PDSignature signature = null;
        PDSignatureField signatureField;
        if (acroForm != null) {
            signatureField = (PDSignatureField) acroForm.getField(sigFieldName);
            if (signatureField != null) {
                // retrieve signature dictionary
                signature = signatureField.getSignature();
                if (signature == null) {
                    signature = new PDSignature();
                    // after solving PDFBOX-3524
                    // signatureField.setValue(signature)
                    // until then:
                    signatureField.getCOSObject().setItem(COSName.V, signature);
                } else {
                    throw new IllegalStateException("The signature field " + sigFieldName + " is already signed.");
                }
            } else {
                throw new IllegalStateException("The signature field " + sigFieldName + " is not found");
            }
        } else {
            throw new IllegalStateException("The PDAcroForm not found for - " + sigFieldName);
        }
        return signature;
    }

    /**
     * Method to draw rectangular box on pdf with given coordinates.
     *
     * @param doc
     * @param pdfRect
     * @param pageNo
     * @return
     */

    public PDRectangle createSignatureRectangle(PDDocument doc, RectF pdfRect, int pageNo) {
        Log.d("Inside", "createSignatureRectangle");
        Log.d("pageNo: ", String.valueOf(pageNo));

        PDPage page = doc.getPage(0);
        Log.d("PDPage: ", page.toString());

        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        // signing should be at the same position regardless of page rotation.
        Log.d("Outside", "Switch");
        switch (page.getRotation()) {
            case 90:
                rect.setLowerLeftY(pdfRect.left);
                rect.setUpperRightY(pdfRect.top + pdfRect.right);
                rect.setLowerLeftX(pdfRect.top);
                rect.setUpperRightX(pdfRect.top + pdfRect.bottom);
                break;
            case 180:
                rect.setUpperRightX(pageRect.getWidth() - pdfRect.left);
                rect.setLowerLeftX(pageRect.getWidth() - pdfRect.left - pdfRect.right);
                rect.setLowerLeftY(pdfRect.top);
                rect.setUpperRightY(pdfRect.top + pdfRect.bottom);
                break;
            case 270:
                rect.setLowerLeftY(pageRect.getHeight() - pdfRect.left - pdfRect.right);
                rect.setUpperRightY(pageRect.getHeight() - pdfRect.left);
                rect.setLowerLeftX(pageRect.getWidth() - pdfRect.top - pdfRect.bottom);
                rect.setUpperRightX(pageRect.getWidth() - pdfRect.top);
                break;
            case 0:
            default:
                rect.setLowerLeftX(pdfRect.left);
                rect.setUpperRightX(pdfRect.left + pdfRect.right);
                rect.setLowerLeftY(pageRect.getHeight() - pdfRect.top - pdfRect.bottom);
                rect.setUpperRightY(pageRect.getHeight() - pdfRect.top);
                break;
        }
        return rect;
    }


    @Override
    public byte[] sign(InputStream content) throws IOException {
        return new byte[0];
    }

    /**
     * Method to append Signature on pdf
     *
     * @param context
     * @param reqval
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String signPdf(Activity context, String reqval) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject resJsonObj = new JSONObject();
            //Object reqeustobj = parser.parse(reqval);
            //JSONObject reqJsonObject = (JSONObject) reqeustobj;
            //if(reqJsonObject.containsKey("responseXML")) {
            EsignResp esignResp = AspConstants.XMLToEsignResp(reqval, true);
            String txnDirVal = esignResp.getTxn();
            resJsonObj.put("responseCode", esignResp.getResCode());
            resJsonObj.put("txn", txnDirVal);
            if (esignResp.getStatus().equals("1")) {
                String res = "";
                if (esignResp.getSignatures().getDocSignature().get(0).getError() != null
                        && !esignResp.getSignatures().getDocSignature().get(0).getError().isEmpty()) {
                    //Show error
                    resJsonObj.put("status", "0");
                    resJsonObj.put("errorMessage", "Signature append step 1 failed");
                    resJsonObj.put("errorCode", esignResp.getSignatures().getDocSignature().get(0).getError());
                } else {
                    // Get common name from user certificate
                    String cn = getNameFromCert(esignResp.getUserX509Certificate());
                    Log.d("cn: ", cn);
                    String signDocumentPath = "", signInfoPath = "";
                    Log.d("Size: ", String.valueOf(esignResp.getSignatures().getDocSignature().size()));
                    for (int i = 0; i < esignResp.getSignatures().getDocSignature().size(); i++) {
                        Log.d("Inside For Loop: ", String.valueOf(esignResp.getSignatures().getDocSignature().size()));
                        DocSignature docSig = esignResp.getSignatures().getDocSignature().get(i);


                        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.dest_file_path)+"/" + txnDirVal + "/" + (i + 1) + "/"+context.getResources().getString(R.string.dest_file_name));
                        if (pdfFile.exists()) {
                            signDocumentPath = pdfFile.getAbsolutePath();
                        }

                        File infoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.info_file_path) + "/" + txnDirVal + "/" + (i + 1) + "/"+context.getResources().getString(R.string.info_file_name));
                        if (infoFile.exists()) {
                            signInfoPath = infoFile.getAbsolutePath();
                        }
                        Log.d("signDocumentPath", signDocumentPath);
                        Log.d("signInfoPath", signInfoPath);

                        Log.d("Doc Signature Value", docSig.getValue());

                        // append Signture on pdf file with name
                        res = appendExternalSignatureToTempPdfAndUpdateName(
                                context,
                                signDocumentPath,
                                signInfoPath,
                                Base64.getDecoder().decode(docSig.getValue()), cn);

                        if (res != null && res.equals("success"))
                            resJsonObj.put("signDocumentPath" + docSig.getId() + "", signDocumentPath);
                    }
                    if (res != null && res.equals("success")) {
                        resJsonObj.put("status", "1");
                        resJsonObj.put("msg", "signature inserted successfully");
                    } else if (res.equals("error1")) {
                        resJsonObj.put("status", "0");
                        resJsonObj.put("errorMessage", "signature append step 1 failed");
                    } else {
                        resJsonObj.put("status", "0");
                        resJsonObj.put("errorMessage", "signature append step 2 failed");
                    }
                }
            } else {
                //Show error
                resJsonObj.put("status", esignResp.getStatus());
                resJsonObj.put("errorMessage", esignResp.getErrMsg());
                resJsonObj.put("errorCode", esignResp.getErrCode());
            }

            return resJsonObj.toString();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    /**
     * Method to signature widgets to the specified page.
     * And updates the COSDictionary iteratively on each addition.
     *
     * @param pdDocument
     * @param pdPage
     * @param rectangle
     * @param signature
     * @throws IOException
     */
    public void addSignatureFieldWithoutImg(PDDocument pdDocument, PDPage pdPage, PDRectangle rectangle, PDSignature signature) throws IOException {
        Log.d("addSignatureField ", "addSignatureFieldWithoutImg");


        PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
        List<PDField> acroFormFields = acroForm.getFields();

        Log.d("PDField: ", acroFormFields.toString());


        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setValue(signature);

        Log.d("signature Fielse: ", signatureField.toString());

        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        acroFormFields.add(signatureField);

        widget.setRectangle(rectangle);
        widget.setPage(pdPage);

        // from PDVisualSigBuilder.createHolderForm()
        PDStream stream = new PDStream(pdDocument);
        PDFormXObject form = new PDFormXObject(stream);
        PDResources res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        PDRectangle bbox = new PDRectangle(rectangle.getWidth(), rectangle.getHeight());
        float height = bbox.getHeight();
        Matrix initialScale = null;
        switch (pdPage.getRotation()) {

            case 90:
                Log.d("Inside Switch1", "Case 90");
                form.setMatrix(AffineTransform.getRotateInstance(1));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(),
                        bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 180:
                Log.d("Inside Switch2", "Case 100");
                form.setMatrix(AffineTransform.getRotateInstance(2));
                break;
            case 270:
                Log.d("Inside Switch3", "Case 270");
                form.setMatrix(AffineTransform.getRotateInstance(3));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(),
                        bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 0:
                Log.d("Inside Switch4", "Case 0");
            default:
                break;
        }
        form.setBBox(bbox);

        // from PDVisualSigBuilder.createAppearanceDictionary()
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);
        PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
        appearance.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearance);

        try (PDPageContentStream cs = new PDPageContentStream(pdDocument, appearanceStream)) {

            // not really sure about this
            // why does scale have no effect when done in the form matrix???
            if (initialScale != null) {
                cs.transform(initialScale);
            }

            // show background (just for debugging, to see the rect size + position)
            // cs.setNonStrokingColor(Color.BLACK);
            cs.addRect(-5000, -5000, 10000, 10000);
            //cs.fill();

            // show background image
            // save and restore graphics if the image is too large and needs to be scaled
            cs.saveGraphicsState();
            cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
           // PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, pdDocument);
           // cs.drawImage(img, 0, 0);
            cs.restoreGraphicsState();

            // show text
            float fontSize = 10;
            float leading = fontSize * 1.5f;
            PDFont font = PDType1Font.TIMES_ITALIC;

            cs.beginText();
            cs.setFont(font, fontSize);
            // cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(fontSize, height - leading);
            cs.setLeading(leading);

            String date = signature.getSignDate().getTime().toString();
            String name = signature.getName();
            Log.d("Signature name:", name);
            String reason = signature.getReason();
            Log.d("Signature reason:", reason);
            cs.showText("Digitally Signed by");
            cs.newLine();
            cs.showText("Name : " + name);
            cs.newLine();
            reason = reason.replaceAll("\r", "").replaceAll("\n\n", "\n");
            reason = reason.replaceAll("\n", "\\|");
            for (String str : reason.split("\\|")) {
                cs.showText("Reason : "+str);
                cs.newLine();
            }
            cs.showText("Date : " + date);
            cs.newLine();
            cs.endText();
        }

        pdPage.getAnnotations().add(widget);

        COSDictionary pageTreeObject = pdPage.getCOSObject();
        while (pageTreeObject != null) {
            pageTreeObject.setNeedToBeUpdated(true);
            pageTreeObject = (COSDictionary) pageTreeObject.getDictionaryObject(COSName.PARENT);
        }

    }


    /**
     * Method to append Doc Signature and also update the name getting from certificate
     *
     * @param context
     * @param destPdfPath
     * @param infoPath
     * @param cmsSignature
     * @param signerName
     * @return
     * @throws Exception
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String appendExternalSignatureToTempPdfAndUpdateName(Activity context, String destPdfPath, String infoPath, byte[] cmsSignature, String signerName) throws Exception {

        String pageValue = "", coordinates = "";
        Log.d("Inside ", "appendExternalSignatureToTempPdfAndUpdateNamev2");
        File infoFile = new File(infoPath);
        Log.d("infoFile ", infoFile.getAbsolutePath());
        String line = "";
        StringBuilder text = new StringBuilder();

        // Read the info file and get data from it
        try {
            FileReader fReader = new FileReader(infoFile.getAbsolutePath());
            BufferedReader bReader = new BufferedReader(fReader);

            while ((line = bReader.readLine()) != null) {
                text.append(line + "\n");
            }
            Log.d("text", String.valueOf(text));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sigInfoData = new String(text);
        int ot=Integer.parseInt(sigInfoData.split("\\|")[0].toString());
        String pageno=sigInfoData.split("\\|")[1];
        String totalSignatures=sigInfoData.split("\\|")[2];
        float x=0;
        float y=0;
        float w=0;
        float h=0;
        Log.d("Sign append info for ", destPdfPath + " : ");
        Log.d("ot of ", destPdfPath + " : " + ot);
        Log.d("pagno of ", destPdfPath + " : " + pageno);
        Log.d("totalSignatures of ", destPdfPath + " : " + totalSignatures);
        Log.d("x of ", destPdfPath + " : " + x);
        Log.d("y of ", destPdfPath + " : " + y);
        Log.d("w of ", destPdfPath + " : " + w);
        Log.d("h of ", destPdfPath + " : " + h);


        try {
            RandomAccessFile raf = new RandomAccessFile(new File(destPdfPath), "rw");
            raf.seek(ot);
            raf.write(Hex.getBytes(cmsSignature));
            raf.close();
        } catch (Exception e) {
            System.err.println("Exception while Signature append step 1  " + e);

            return "error1";
        }

        //LOGGER.info("Signature append step 1 completed ");


            int existingSignatures = Integer.valueOf(totalSignatures);
            PDDocument document = PDDocument.load(new File(destPdfPath));
            PDSignature signature = document.getSignatureDictionaries().get(existingSignatures - 1);
            Log.d("signerName", signerName);
            if (signerName != null) {
                Log.d("inside signerName", signerName);
                signature.setName(signerName);
            }
            Log.d("PDSign signer name: ", signature.getName());
            document.getSignatureDictionaries().remove(existingSignatures - 1);
            document.getDocumentCatalog().getAcroForm().getFields().remove(existingSignatures - 1);

            // Parsing the PageNo value which is in Json format
        JSONParser parser = new JSONParser();
        JSONArray json = (JSONArray) parser.parse(pageno);

        for (Object object : json) {
            JSONObject jsonOb=(JSONObject)object;
            if(jsonOb.get("page").toString().equals("all")) {
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                    for (Object object2 : jsonCoordinates) {
                        JSONObject jsonCo=(JSONObject)object2;
                        x=Float.parseFloat(jsonCo.get("x").toString());
                        y=Float.parseFloat(jsonCo.get("y").toString());
                        w=Float.parseFloat(jsonCo.get("w").toString());
                        h=Float.parseFloat(jsonCo.get("h").toString());
                        RectF humanRect = new RectF(x, y, w, h);
                        PDRectangle rec =createSignatureRectangle(document, humanRect, (i));
                        addSignatureFieldWithoutImg(document, document.getPage((i)), rec, signature);
                    }

                }
            }else if(jsonOb.get("page").toString().contains(",")) {
                String[] pageNost = jsonOb.get("page").toString().split(",");
                int[] pageN = Arrays.asList(pageNost).stream().mapToInt(Integer::parseInt).toArray();
                for (int i = 0; i < pageN.length; i++) {
                    JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                    for (Object object2 : jsonCoordinates) {
                        JSONObject jsonCo=(JSONObject)object2;
                        x=Float.parseFloat(jsonCo.get("x").toString());
                        y=Float.parseFloat(jsonCo.get("y").toString());
                        w=Float.parseFloat(jsonCo.get("w").toString());
                        h=Float.parseFloat(jsonCo.get("h").toString());
                        RectF humanRect = new RectF(x, y, w, h);
                        PDRectangle rec =createSignatureRectangle(document, humanRect, (pageN[i]-1));
                        addSignatureFieldWithoutImg(document, document.getPage((pageN[i]-1)), rec, signature);
                    }
                }

            }else {
                int pnumber=Integer.parseInt(jsonOb.get("page").toString());
                JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                for (Object object2 : jsonCoordinates) {
                    JSONObject jsonCo=(JSONObject)object2;
                    x=Float.parseFloat(jsonCo.get("x").toString());
                    y=Float.parseFloat(jsonCo.get("y").toString());
                    w=Float.parseFloat(jsonCo.get("w").toString());
                    h=Float.parseFloat(jsonCo.get("h").toString());
                    RectF humanRect = new RectF(x, y, w, h);
                    PDRectangle rec =createSignatureRectangle(document, humanRect, (pnumber-1));
                    addSignatureFieldWithoutImg(document, document.getPage((pnumber-1)), rec, signature);
                }
            }

        }
            document.getDocumentCatalog().getCOSObject().setNeedToBeUpdated(true);

            //document.saveIncremental(new FileOutputStream(destPdfPath));
            document.saveIncremental(new FileOutputStream(destPdfPath.toLowerCase().replace(".pdf", "Final.pdf")));
            document.close();


        //LOGGER.info("Signature append successfully");
        return "success";
    }

    /**
     * Method to collect CN (Common Name) from User Certificate
     *
     * @param usercervalue
     * @return
     */
    public String getNameFromCert(String usercervalue) {
        String cn = "";
        try {
            usercervalue = "-----BEGIN CERTIFICATE-----\n" + usercervalue + "\n-----END CERTIFICATE-----";
            InputStream is = null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            if (usercervalue != null)
                Log.d("usercervalue:", usercervalue.getBytes().toString());
            is = new ByteArrayInputStream(usercervalue.getBytes());
            Log.d("Input Stream:", String.valueOf(is.available()));

            X509Certificate x509Certificate = null;
            if (is != null)
                x509Certificate = (X509Certificate) cf.generateCertificate(is);

            if (x509Certificate != null) {
                String sub = x509Certificate.getSubjectX500Principal().getName();
                try {
                    x509Certificate.checkValidity();
                } catch (Exception e) {
                    Log.e("Check exception:", e.getMessage());
                }

                String[] splitedValues = sub.split(",");
                for (int i = 0; i < splitedValues.length; i++) {

                    if (splitedValues[i].contains("CN=")) {
                        cn = splitedValues[i].replaceAll("CN=", "");
                        Log.d("Common Name: ", cn);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
            e.printStackTrace();
        }
        return cn;
    }

    /**
     * Method to get Pdf details from Json i/p
     * Calculate PDF hash
     * Generate Signed hash of signature and append it to predefined xml
     * send predefined xml payload request to ESP server
     *
     * @param context
     * @param reqval
     * @param espurl
     * @param textHash
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getTxnRef(Activity context, String reqval, String espurl, TextView textHash) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject resJsonObj = new JSONObject();
            Object reqeustobj = parser.parse(reqval);
            String inputHash = "";
            JSONObject reqJsonObject = (JSONObject) reqeustobj;
            String txnDirVal = reqJsonObject.get("txn").toString();


            //Version 2.1

            JSONArray pdfDocList = (JSONArray) reqJsonObject.get("pdfdetails");
            for (int i = 0; i < pdfDocList.size(); i++) {
                JSONObject array_element = (JSONObject) pdfDocList.get(i);
                JSONArray pageNo = (JSONArray) array_element.get("signaturedetails");

                if (!new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.dest_file_path)+ "/" + txnDirVal + "/" + (i + 1)).exists()) {
                    new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.dest_file_path)+ "/" + txnDirVal + "/" + (i + 1)).mkdirs();
                }


                if (!new File(reqJsonObject.get("tempInfoPath").toString() + "/" + txnDirVal + "/" + (i + 1)).exists()) {
                    new File(reqJsonObject.get("tempInfoPath").toString() + "/" + txnDirVal + "/" + (i + 1)).mkdirs();
                }
                String signatureFieldName = null;
                byte[] pdfFileVal = null;
                String pdfSrcPathVal = null;
                if (reqJsonObject.get("fileType") != null && reqJsonObject.get("fileType").equals("file")) {
                    pdfFileVal = Base64.getDecoder().decode(array_element.get("pdfbase64val").toString().getBytes());
                } else {
                    pdfSrcPathVal = array_element.get("pdfbase64val").toString();
                }
                Map<String, Object> responseMap = new HashMap<String, Object>();
                responseMap = generatePdfHash(context,
                        pdfFileVal,
                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + context.getResources().getString(R.string.dest_file_path)+"/" + txnDirVal + "/" + (i + 1) + "/"+context.getResources().getString(R.string.dest_file_name)
                        , "",
                        "",
                        array_element.get("reason").toString(), "", signatureFieldName,
                        Float.parseFloat(array_element.get("coordinates").toString().split(",")[0]),
                        Float.parseFloat(array_element.get("coordinates").toString().split(",")[1]),
                        Float.parseFloat(array_element.get("coordinates").toString().split(",")[2]),
                        Float.parseFloat(array_element.get("coordinates").toString().split(",")[3]),
                        true
                        , reqJsonObject.get("tempInfoPath").toString() + "/" + txnDirVal + "/" + (i + 1) + "/"+context.getResources().getString(R.string.info_file_name),
                        60, 10000,
                        pageNo.toJSONString()
                        , pdfSrcPathVal
                );

                textHash.setText("Pdf Hash: " + responseMap.get("hash"));


                if (responseMap != null && responseMap.containsKey("hash")) {
                    inputHash += "<InputHash id=\"" + (i + 1) + "\" hashAlgorithm=\"SHA256\" docInfo=\"" + array_element.get("docInfo") + "\" "
                            + "responseSigType=\"pkcs7complete\" docUrl=\"" + array_element.get("docUrl") + "\">" + responseMap.get("hash") + "</InputHash>";

                    Log.d("inputHash", "getTxnRef: " + inputHash);
                }


            }

            // Create XML Payload
            String eSignRequest = "<?xml version='1.0' encoding='UTF-8'?><Esign aspId=\""
                    + reqJsonObject.get("aspId") + "\" ekycIdType=\"A\" "
                    + "responseSigType=\"pkcs7pdf\" responseUrl=\"" + reqJsonObject.get("responseUrl")
                    + "\" sc=\"Y\" ts=\"" + AspConstants.generateTsValue() + "\" "
                    + " txn=\"" + reqJsonObject.get("txn") + "\" ver=\""+reqJsonObject.get("ver")+"\" AuthMode=\""
                    + reqJsonObject.get("AuthMode") + "\"><Docs>" + inputHash + "</Docs></Esign>";
            XmlDigitalSigner xmlDigitalSigner = new XmlDigitalSigner();
            String signedEsignReqXml = xmlDigitalSigner.signXML(reqJsonObject.get("pfxPath").toString(),
                    reqJsonObject.get("pfxPassword").toString(), reqJsonObject.get("pfxAlias").toString(), eSignRequest, true);

            Log.d("signedEsignReqXml: ", signedEsignReqXml);
            String esignResponseXml = callESPSerivceAPI(signedEsignReqXml, espurl);
            EsignResp esignResp = AspConstants.XMLToEsignResp(esignResponseXml, false);
            if (esignResp.getStatus().equals("1")) {
                resJsonObj.put("status", esignResp.getStatus());
                resJsonObj.put("errorCode", "");
                resJsonObj.put("errorMessage", "");
                resJsonObj.put("txn", esignResp.getTxn());
                resJsonObj.put("responseCode", esignResp.getResCode());
                resJsonObj.put("requestXML", new String(Base64.getEncoder().encode(signedEsignReqXml.getBytes())));
                resJsonObj.put("responseXML", new String(Base64.getEncoder().encode(esignResponseXml.getBytes())));
                String txnRefStr = reqJsonObject.get("txn") + "|" + esignResp.getResCode();
                resJsonObj.put("txnref", Base64.getEncoder().encodeToString(txnRefStr.getBytes()));
            } else {
                resJsonObj.put("status", esignResp.getStatus());
                resJsonObj.put("errorCode", esignResp.getErrMsg());
                resJsonObj.put("errorMessage", "");
                resJsonObj.put("txn", esignResp.getTxn());
                resJsonObj.put("responseCode", esignResp.getResCode());
                resJsonObj.put("requestXML", new String(Base64.getEncoder().encode(signedEsignReqXml.getBytes())));
                resJsonObj.put("responseXML", new String(Base64.getEncoder().encode(esignResponseXml.getBytes())));
                resJsonObj.put("txnref", "");
            }

            return resJsonObj;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param eSignXml
     * @return
     */
    public String callESPSerivceAPI(String eSignXml, String espUrl) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StringBuilder eSignXmlResponse = new StringBuilder();
        try {
            URL url = new URL(espUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            /**
             *
             *   Set timeout as per needs
             */

            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);

            /**
             * Set DoOutput to true if you want to use URLConnection for output.
             * Default is false
             */

            connection.setDoOutput(true);

            connection.setUseCaches(true);

            connection.setRequestMethod("POST");


            /**
             * Set Header
             */
            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/xml");

            /**
             * Convert String XML into byte array and Send Request to ESP Server
             */

            OutputStream outputStream = connection.getOutputStream();


            byte[] b = eSignXml.getBytes("UTF-8");

            outputStream.write(b);
            outputStream.flush();
            outputStream.close();

            /**
             * Collect the response
             */
            InputStream inputStream = connection.getInputStream();

            byte[] res = new byte[2048];
            int i = 0;

            while (true) {

                if (!((i = inputStream.read(res)) != -1)) break;

                eSignXmlResponse.append(new String(res, 0, i));
            }

            inputStream.close();

            System.out.println("Response= " + eSignXmlResponse.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eSignXmlResponse.toString();
    }


}
