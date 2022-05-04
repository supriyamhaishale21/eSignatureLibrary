package com.coreco.esignatureapp.PdfHash;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.coreco.esignatureapp.Constants.AspConstants;
import com.coreco.esignatureapp.DocSignature;
import com.coreco.esignatureapp.EsignResp;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneratePDFHash implements SignatureInterface{
    private static File imageFile;
    String signedPdfPath="",signedPdfInfoPath="";

    public Map<String, Object> generatePdfHash(Activity context,String srcPath, String ticImagePath,
                                               String signerName, String signingReason, String signingLocation,
                                               String signatureFieldName, float x, float y, float w, float h,
                                               int[] pageNo, boolean hashLowerCase, String infoPath,
                                               int signingTimeDelay, int signaturePredefinedSize,
                                               boolean signAllPages, String pageNumber) throws IOException, NoSuchAlgorithmException {


        File srcPdfFile=new File(srcPath);
        imageFile = new File(ticImagePath);

        String hashForExSigning = null;
        int pageNum=0;
        HashMap<String, Object> response = new HashMap<String, Object>();

        if (srcPdfFile == null || !srcPdfFile.exists()) {
            throw new IOException("Document for signing does not exist");
        }

        PDDocument doc = PDDocument.load(srcPdfFile);

        // Note that PDFBox has a bug that visual signing on certified files with
        // permission 2
        // doesn't work properly, see PDFBOX-3699. As long as this issue is open, you
        // may want to
        // be careful with such files

        // creating output document and prepare the IO streams.

       File myFile = new File(context.getCacheDir(),"temp_sign.pdf");

        FileOutputStream desFile = new FileOutputStream(myFile);


        PDSignature signature = null;
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDRectangle rect=null;


        if (signature == null) {
            // create signature dictionary
            signature = new PDSignature();
        }

        if (rect == null) {
            RectF pdfRect=new RectF(x,y,w,h);
            rect = createSignatureRectangle(doc, pdfRect,pageNum);
        }

        if (acroForm != null && acroForm.getNeedAppearances()) {
            // PDFBOX-3738 NeedAppearances true results in visible signature becoming
            // invisible
            // with Adobe Reader
            if (acroForm.getFields().isEmpty()) {
                // we can safely delete it if there are no fields
                acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
                // note that if you've set MDP permissions, the removal of this item
                // may result in Adobe Reader claiming that the document has been changed.
                // and/or that field content won't be displayed properly.
                // ==> decide what you prefer and adjust your code accordingly.
            } else {
                Log.d("","/NeedAppearances is set, signature may be ignored by Adobe Reader");
            }
        }

        // default filter
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);

        // subfilter for basic and PAdES Part 2 signatures
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

        signature.setName(signerName);
        signature.setLocation(signingLocation);
        signature.setReason(signingReason);

        // the signing date, needed for valid signature

        Calendar signingTime = Calendar.getInstance();
        signingTime.add(Calendar.SECOND, signingTimeDelay);
        signature.setSignDate(signingTime);

        // set null if external signing will be done, else need to implement
        // SignatureInterface
        SignatureInterface signatureInterface = this;

        // register signature dictionary and sign interface
        SignatureOptions signatureOptions = new SignatureOptions();
        signatureOptions.
                setVisualSignature
                        (createVisualSignatureTemplate(doc, pageNum, rect, signature));
        signatureOptions.setPage(pageNum);
        doc.addSignature(signature, signatureInterface, signatureOptions);

        String totalSignatures=String.valueOf(doc.getSignatureDictionaries().size());


        ExternalSigningSupport externalSigning = doc.saveIncrementalForExternalSigning(desFile);

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

        // write incremental (only for signing purpose)
        doc.saveIncremental(desFile);

        doc.close();

        IOUtils.closeQuietly(signatureOptions);

        if (hashLowerCase)
            response.put("hash", hashForExSigning.toLowerCase());
        else
            response.put("hash", hashForExSigning.toUpperCase());

        String pnoData=String.valueOf(offset)+"|" +pageNumber+"|"+totalSignatures+"|"+x+"|"+y+"|"+w+"|"+h;

       // logTextView.setText(hashForExSigning.toLowerCase());
        if(infoPath!=null) {
            File sigInfoFile = new File(infoPath);
            sigInfoFile.createNewFile();

            FileOutputStream fosSigInfo = new FileOutputStream(sigInfoFile);
            fosSigInfo.write(pnoData.getBytes());
            fosSigInfo.close();
        }else {
            response.put("offset", offset);
        }

    return response;
    }

    // create a template PDF document with empty signature and return it as a
    // stream.
    private InputStream createVisualSignatureTemplate(PDDocument srcDoc, int pageNum, PDRectangle rect,
                                                      PDSignature signature) throws IOException {
        PDDocument doc = new PDDocument();

        PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
        doc.addPage(page);
        PDAcroForm acroForm = new PDAcroForm(doc);
        doc.getDocumentCatalog().setAcroForm(acroForm);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
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
        // cs.setStrokingColor(Color.BLUE/255f);
        cs.addRect(-5000, -5000, 10000, 10000);
       // cs.fill();

        // show background image
        // save and restore graphics if the image is too large and needs to be scaled
        cs.saveGraphicsState();
        cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
//        PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, doc);
//        cs.drawImage(img, 0, 0);
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

        // X509Certificate cert = (X509Certificate) getCertificateChain()[0];

        // https://stackoverflow.com/questions/2914521/
        // X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
        // RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
        // String name = IETFUtils.valueToString(cn.getFirst().getValue());

        // See https://stackoverflow.com/questions/12575990
        // for better date formatting
        String name = signature.getName();
        String reason = signature.getReason();
        String date = signature.getSignDate().getTime().toString();
        Log.d("name : ",name);
        cs.showText("Digitally Signed by");
        cs.newLine();
        cs.showText("Name : " + name);
        cs.newLine();
        cs.showText("Reason : " + reason);
        cs.newLine();
        cs.showText("Date : " + date);
        cs.newLine();
        cs.endText();

        cs.close();

        // no need to set annotations and /P entry
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public  PDRectangle createSignatureRectangle(PDDocument doc, RectF pdfRect, int pageNo) {
        PDPage page = doc.getPage(pageNo);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        // signing should be at the same position regardless of page rotation.
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    public  String signPdfv2(String reqval){
        try {
            //JSONParser parser = new JSONParser();
            JSONObject resJsonObj = new JSONObject();
            //Object reqeustobj = parser.parse(reqval);
            //JSONObject reqJsonObject = (JSONObject) reqeustobj;
           // if(reqJsonObject.containsKey("responseXML")) {
                EsignResp esignResp = AspConstants.XMLToEsignResp(reqval);
               // String txnDirVal=esignResp.getTxn().replace(":", "-");
                resJsonObj.put("responseCode", esignResp.getResCode());
                resJsonObj.put("txn", esignResp.getTxn());
                if(esignResp.getStatus().equals("1")) {
                    String res="";
                    if(esignResp.getSignatures().getDocSignature().get(0).getError()!=null
                            &&!esignResp.getSignatures().getDocSignature().get(0).getError().isEmpty()) {
                        //Show error
                        resJsonObj.put("status", "0");
                        resJsonObj.put("errorMessage", "Signature append step 1 failed");
                        resJsonObj.put("errorCode", esignResp.getSignatures().getDocSignature().get(0).getError());
                    }else {
                        String cn=getNameFromCert(esignResp.getUserX509Certificate());
                        String signDocumentPath="",signInfoPath="";
                        for (int i = 0; i <esignResp.getSignatures().getDocSignature().size(); i++) {
                            DocSignature docSig = esignResp.getSignatures().getDocSignature().get(i);
                            signDocumentPath=findFiles("","/temp_sign.pdf");
                            signInfoPath=findFiles("","/temp_info.pdf");
                            res=appendExternalSignatureToTempPdfAndUpdateNamev2(
                                    signDocumentPath,
                                    signInfoPath,
                                    Base64.getDecoder().decode(docSig.getValue()),cn);
                            if(res!=null&&res.equals("success"))
                                resJsonObj.put("signDocumentPath"+docSig.getId()+"", signDocumentPath);
                        }
                        if(res!=null&&res.equals("success")) {
                            resJsonObj.put("status", "1");
                            resJsonObj.put("msg", "signature inserted successfully");
                        }else if(res.equals("error1")){
                            resJsonObj.put("status", "0");
                            resJsonObj.put("errorMessage", "signature append step 1 failed");
                        }else {
                            resJsonObj.put("status", "0");
                            resJsonObj.put("errorMessage", "signature append step 2 failed");
                        }
                    }
                }else {
                    //Show error
                    resJsonObj.put("status", esignResp.getStatus());
                    resJsonObj.put("errorMessage", "");
                    resJsonObj.put("errorCode", esignResp.getError());
                }

                return resJsonObj.toString();
            } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        //}


        }catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }


    /**Method to signature widgets to the specified page.
     * And updates the COSDictionary iteratively on each addition.
     * @param pdDocument
     * @param pdPage
     * @param rectangle
     * @param signature
     * @throws IOException
     */
    public void addSignatureFieldWithoutImg(PDDocument pdDocument, PDPage pdPage, PDRectangle rectangle, PDSignature signature) throws IOException {
        PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
        List<PDField> acroFormFields = acroForm.getFields();

        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setValue(signature);
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

        try (PDPageContentStream cs = new PDPageContentStream(pdDocument, appearanceStream))
        {

            // not really sure about this
            // why does scale have no effect when done in the form matrix???
            if (initialScale != null) {
                cs.transform(initialScale);
            }

            // show background (just for debugging, to see the rect size + position)
            cs.setNonStrokingColor(Color.BLACK);
            cs.addRect(-5000, -5000, 10000, 10000);
            cs.fill();

            // show background image
            // save and restore graphics if the image is too large and needs to be scaled
//    		cs.saveGraphicsState();
//    		cs.transform(Matrix.getScaleInstance(0.25f, 0.25f));
//    		PDImageXObject img = PDImageXObject.createFromFileByExtension(imageFile, pdDocument);
//    		cs.drawImage(img, 0, 0);
//    		cs.restoreGraphicsState();

            // show text
            float fontSize = 10;
            float leading = fontSize * 1.5f;
            PDFont font = PDType1Font.TIMES_ITALIC;

            cs.beginText();
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(fontSize, height - leading);
            cs.setLeading(leading);

            String name = signature.getName();
            String reason = signature.getReason();
            cs.showText("Digitally Signed by");
            cs.newLine();
            cs.showText("Name : " + name);
            cs.newLine();
            reason=reason.replaceAll("\r", "").replaceAll("\n\n", "\n");
            reason=reason.replaceAll("\n", "\\|");
            for (String str : reason.split("\\|")) {
                cs.showText(str);
                cs.newLine();
            }
//    		cs.showText("Date : " + date);
//    		cs.newLine();
            cs.endText();
        }

        pdPage.getAnnotations().add(widget);

        COSDictionary pageTreeObject = pdPage.getCOSObject();
        while (pageTreeObject != null) {
            pageTreeObject.setNeedToBeUpdated(true);
            pageTreeObject = (COSDictionary) pageTreeObject.getDictionaryObject(COSName.PARENT);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public String appendExternalSignatureToTempPdfAndUpdateNamev2(String destPdfPath, String infoPath, byte[] cmsSignature, String signerName) throws Exception {

        //LOGGER.info("Reading temp file :"+infoPath);
        File infoFile = new File(infoPath);
        String sigInfoData = new String(Files.readAllBytes(Paths.get(infoFile.getAbsolutePath())));
        int ot=Integer.parseInt(sigInfoData.split("\\|")[0].toString());
        String pageno=sigInfoData.split("\\|")[1];
        String totalSignatures=sigInfoData.split("\\|")[2];
        float x=0;
        float y=0;
        float w=0;
        float h=0;
        //LOGGER.info("Sign append info for "+destPdfPath+" : ");
        //LOGGER.info("ot of "+destPdfPath+" : "+ot);
        System.out.println("pagno of "+destPdfPath+" : "+pageno);
        //LOGGER.info("totalSignatures of "+destPdfPath+" : "+totalSignatures);

        try {
            RandomAccessFile raf = new RandomAccessFile(new File(destPdfPath), "rw");
            raf.seek(ot);
            raf.write(Hex.getBytes(cmsSignature));
            raf.close();
        } catch (Exception e) {
            System.err.println("Exception while Signature append step 1  "+e);

            return "error1";
        }

        //LOGGER.info("Signature append step 1 completed ");
        try {

            int existingSignatures = Integer.valueOf(totalSignatures);
            PDDocument document = PDDocument.load(new File(destPdfPath));
            PDSignature signature = document.getSignatureDictionaries().get(existingSignatures - 1);
            if(signerName != null) {
                signature.setName(signerName);
            }
            document.getSignatureDictionaries().remove(existingSignatures - 1);
            document.getDocumentCatalog().getAcroForm().getFields().remove(existingSignatures - 1);


            JSONParser parser = new JSONParser();
            JSONArray json = (JSONArray) parser.parse(pageno);

            for (int i = 0; i < json.length() ; i++) {
                JSONObject jsonOb = json.getJSONObject(i);
                if(jsonOb.get("page").toString().equals("all")) {
                    for (int j= 0; j < document.getNumberOfPages(); j++) {
                        JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                        for (int k = 0; k < jsonCoordinates.length(); k++) {

                            JSONObject jsonCo=jsonCoordinates.getJSONObject(k);
                            x=Float.parseFloat(jsonCo.get("x").toString());
                            y=Float.parseFloat(jsonCo.get("y").toString());
                            w=Float.parseFloat(jsonCo.get("w").toString());
                            h=Float.parseFloat(jsonCo.get("h").toString());
                            RectF humanRect = new RectF(x, y, w, h);
                            PDRectangle rec =createSignatureRectangle(document, humanRect, (j));
                            addSignatureFieldWithoutImg(document, document.getPage(i), rec, signature);
                        }

                    }
                }else if(jsonOb.get("page").toString().contains(",")) {
                    String[] pageNost = jsonOb.get("page").toString().split(",");
                    int[] pageN = Arrays.asList(pageNost).stream().mapToInt(Integer::parseInt).toArray();
                    for (int l = 0; l < pageN.length; l++) {
                        JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                        for (int j = 0; j < jsonCoordinates.length(); j++) {
                            JSONObject jsonCo=jsonCoordinates.getJSONObject(j);
                            x=Float.parseFloat(jsonCo.get("x").toString());
                            y=Float.parseFloat(jsonCo.get("y").toString());
                            w=Float.parseFloat(jsonCo.get("w").toString());
                            h=Float.parseFloat(jsonCo.get("h").toString());
                            RectF humanRect = new RectF(x, y, w, h);
                            PDRectangle rec =createSignatureRectangle(document, humanRect, (pageN[l]-1));
                            addSignatureFieldWithoutImg(document, document.getPage((pageN[l]-1)), rec, signature);
                        }
                    }

                }else {
                    int pnumber=Integer.parseInt(jsonOb.get("page").toString());
                    JSONArray jsonCoordinates = (JSONArray)jsonOb.get("coordinates");
                    for (int m = 0; m < jsonCoordinates.length(); m++) {
                        JSONObject jsonCo=jsonCoordinates.getJSONObject(m);
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
            document.saveIncremental(new FileOutputStream(new File(destPdfPath)));
//	        document.saveIncremental(new FileOutputStream(new File(destPdfPath.toLowerCase().replace(".pdf", "Final.pdf"))));
            document.close();



        } catch (Exception e) {
            System.err.println("Exception while Signature append step 2   "+e);
            return "error2";
        }finally {

        }
        //LOGGER.info("Signature append successfully");
        return "success";
    }

    public String getNameFromCert(String usercervalue)  {
        String cn="";
        try {
            usercervalue="-----BEGIN CERTIFICATE-----\n"+usercervalue+"-----END CERTIFICATE-----";
            InputStream is=null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            if(usercervalue !=null)
                is = new ByteArrayInputStream(usercervalue.toString().getBytes());
            X509Certificate x509Certificate=null;
            if(is!=null)
                x509Certificate = (X509Certificate)cf.generateCertificate(is);

            if(x509Certificate!=null){
                String sub = x509Certificate.getSubjectX500Principal().getName();
                try {
                    x509Certificate.checkValidity();
                } catch (Exception e) {
                }

                String[] splitedValues = sub.split(",");
                for(int i=0;i<splitedValues.length;i++){

                    if(splitedValues[i].contains("CN=")){
                        cn = splitedValues[i].replaceAll("CN=","");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String findFiles(String pathVal, String fileExtension)
            throws IOException {


        Path path=Paths.get(pathVal);
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path must be a directory!");
        }

        List<String> result;

        try (Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.toString().toLowerCase())
                    .filter(f -> f.endsWith(fileExtension))
                    .collect(Collectors.toList());
        }
        return result.get(0);
    }

}
