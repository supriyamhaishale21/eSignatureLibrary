package com.coreco.esignatureapp.PdfHash;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.tom_roush.harmony.awt.geom.AffineTransform;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.io.IOUtils;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratePDFHash implements SignatureInterface{
    private static File imageFile;

    public Map<String, Object> generatePdfHash(Activity context, String srcPath, String ticImagePath,
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
            rect = createSignatureRectangle(doc,  x, y, w, h,pageNum);
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
            //File sigInfoFile = new File(infoPath);
            //sigInfoFile.createNewFile();
            File tempFile = new File(context.getCacheDir(),"temp_file.txt");
            FileOutputStream fosSigInfo = new FileOutputStream(tempFile);
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

    public  PDRectangle createSignatureRectangle(PDDocument doc, float x, float y, float w, float h, int pageNo) {
        PDPage page = doc.getPage(pageNo);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        // signing should be at the same position regardless of page rotation.
        switch (page.getRotation()) {
            case 90:
                rect.setLowerLeftY(x);
                rect.setUpperRightY(x + w);
                rect.setLowerLeftX(y);
                rect.setUpperRightX(y + h);
                break;
            case 180:
                rect.setUpperRightX(pageRect.getWidth() - x);
                rect.setLowerLeftX(pageRect.getWidth() - x - w);
                rect.setLowerLeftY(y);
                rect.setUpperRightY(y + h);
                break;
            case 270:
                rect.setLowerLeftY(pageRect.getHeight() - x - w);
                rect.setUpperRightY(pageRect.getHeight() - x);
                rect.setLowerLeftX(pageRect.getWidth() - y - h);
                rect.setUpperRightX(pageRect.getWidth() - y);
                break;
            case 0:
            default:
                rect.setLowerLeftX(x);
                rect.setUpperRightX(x + w);
                rect.setLowerLeftY(pageRect.getHeight() - y - h);
                rect.setUpperRightY(pageRect.getHeight() - y);
                break;
        }
        return rect;
    }


    @Override
    public byte[] sign(InputStream content) throws IOException {
        return new byte[0];
    }
}
