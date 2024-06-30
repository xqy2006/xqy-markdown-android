package com.xuqinyang.md;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;

public class DownloadBlobFileJSInterface {
    private static String mimeType = "";
    private static String fileName = "";
    private Context mContext;
    private DownloadGifSuccessListener mDownloadGifSuccessListener;

    public DownloadBlobFileJSInterface(Context context) {
        this.mContext = context;
    }

    public void setDownloadGifSuccessListener(DownloadGifSuccessListener listener) {
        mDownloadGifSuccessListener = listener;
    }

    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) {
        convertToGifAndProcess(base64Data);
    }

    public static String getBase64StringFromBlobUrl(String blobUrl, String fileMimeType, String urlFileName) {
        if (blobUrl.startsWith("blob")) {
                mimeType = fileMimeType;
                fileName = urlFileName;
            return "javascript: var xhr = new XMLHttpRequest();" + "xhr.open('GET', '" + blobUrl + "', true);" + "xhr.setRequestHeader('Content-type','" + fileMimeType + ";charset=UTF-8');" + "xhr.responseType = 'blob';" + "xhr.onload = function(e) {" + "    if (this.status == 200) {" + "        var blobFile = this.response;" + "        var reader = new FileReader();" + "        reader.readAsDataURL(blobFile);" + "        reader.onloadend = function() {" + "            base64data = reader.result;" + "            Android.getBase64FromBlobData(base64data);" + "        }" + "    }" + "};" + "xhr.send();";

        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    private void convertToGifAndProcess(String base64) {
        String mypath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        if (fileName.endsWith(".jpg")){
            mypath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        }
        File dir = new File(mypath+ "/MarkdownEditor/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File gifFile = new File(mypath + "/MarkdownEditor/" + fileName);
        int a = 1;
        while (gifFile.exists()) {
            gifFile = new File(mypath + "/MarkdownEditor/" + a + "_" + fileName);
            a++;
        }
        saveGifToPath(base64, gifFile);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(gifFile)));
        Toast.makeText(mContext, "保存成功"+gifFile, Toast.LENGTH_SHORT).show();
        if (mDownloadGifSuccessListener != null) {
            mDownloadGifSuccessListener.downloadGifSuccess(gifFile.getAbsolutePath());
        }
    }

    private void saveGifToPath(String base64, File gifFilePath) {
        try {
            byte[] fileBytes = Base64.decode(base64.replaceFirst("data:" + mimeType + ";base64,", ""), 0);
            FileOutputStream os = new FileOutputStream(gifFilePath, false);
            os.write(fileBytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DownloadGifSuccessListener {
        void downloadGifSuccess(String absolutePath);
    }

}
