package com.xuqinyang.md;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.xuqinyang.writer.R;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private WebView mWebView;
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    protected class CustomWebChromeClient extends WebChromeClient
    {
        // maneja la accion de seleccionar archivos
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            // asegurar que no existan callbacks
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }

            mUploadMessage = filePathCallback;

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*"); // set MIME type to filter

            MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FILECHOOSER_RESULTCODE );

            return true;
        }

}   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // manejo de seleccion de archivo
        if (requestCode == FILECHOOSER_RESULTCODE) {

            if (null == mUploadMessage || intent == null || resultCode != RESULT_OK) {
                return;
            }

            Uri[] result = null;
            String dataString = intent.getDataString();

            if (dataString != null) {
                result = new Uri[]{ Uri.parse(dataString) };
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new CustomWebChromeClient());
        // REMOTE RESOURCE
        //mWebView.loadUrl("https://story.xuqinyang.top");

        // LOCAL RESOURCE
        mWebView.loadUrl("file:///android_asset/index.html");
        mWebView.addJavascriptInterface(new DownloadBlobFileJSInterface(this), "Android");
        mWebView.setDownloadListener(new DownloadListener() {
            /**
             * @param url  应该下载的内容的完整url
             * @param userAgent 用于下载的用户代理。
             * @param contentDisposition 内容处置http标头（如果存在）。
             * @param mimetype 服务器报告的内容的mimetype
             * @param contentLength  服务器报告的文件大小
             */
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                if (url.startsWith("blob")) {
                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    // 3. 执行JS
                    mWebView.evaluateJavascript("javascript:get_filename()", new ValueCallback<String>(){
                        public void onReceiveValue(String value) {
                            if (value.replace("\"","").trim().length() == 0) {
                                value = "\"untitled\"";
                            }
                            mWebView.loadUrl(DownloadBlobFileJSInterface.getBase64StringFromBlobUrl(url, mimetype, value.replace("\"","")+fileName.replace(".bin",".pdf").replace(".txt",".md").substring(fileName.lastIndexOf('.'))));
                        }
                        });
                    return;
                }

                //TODO 其他协议下载方式  http https
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
