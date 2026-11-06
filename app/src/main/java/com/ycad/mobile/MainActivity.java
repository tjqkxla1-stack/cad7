package com.ycad.mobile;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_RESULT_CODE = 1001;
    private static final int NATIVE_OPEN_DOC_CODE = 1002;
    private static final int NATIVE_SAVE_DOC_CODE = 1003;

    private String pendingSaveContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.addJavascriptInterface(new YCadNativeBridge(), "YCadNativeBridge");
        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Force "*/*" so Android "내 파일 / 다운로드" NEVER greys out .dwg or .dxf files!
                intent.setType("*/*");
                setDownloadFolderInitialUri(intent);

                try {
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/ycad.html");
    }

    private void setDownloadFolderInitialUri(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Uri downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload");
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);
            } catch (Exception ignored) {}
        }
    }

    public class YCadNativeBridge {
        @JavascriptInterface
        public void openFileFromDownloads() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                setDownloadFolderInitialUri(intent);
                startActivityForResult(intent, NATIVE_OPEN_DOC_CODE);
            });
        }

        @JavascriptInterface
        public void openFileFromPicker() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, NATIVE_OPEN_DOC_CODE);
            });
        }

        @JavascriptInterface
        public String saveFileToDownloads(String subFolder, String fileName, String content) {
            try {
                File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File targetDir = (subFolder != null && !subFolder.trim().isEmpty())
                        ? new File(baseDir, subFolder.trim())
                        : baseDir;
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                File outFile = new File(targetDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(content.getBytes(StandardCharsets.UTF_8));
                    fos.flush();
                }
                return outFile.getAbsolutePath();
            } catch (Exception e) {
                // Fallback to SAF folder picker if direct file write is restricted by scoped storage
                saveFileWithFolderPicker(fileName, content);
                return "Download/" + fileName;
            }
        }

        @JavascriptInterface
        public void saveFileWithFolderPicker(String fileName, String content) {
            pendingSaveContent = content != null ? content : "";
            runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                setDownloadFolderInitialUri(intent);
                startActivityForResult(intent, NATIVE_SAVE_DOC_CODE);
            });
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1 && result != null) result = result.substring(cut + 1);
        }
        return result != null ? result : "DRAWING.dwg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        } else if (requestCode == NATIVE_OPEN_DOC_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    String fileName = getFileNameFromUri(uri);
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (is != null) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] chunk = new byte[16384];
                        int nRead;
                        while ((nRead = is.read(chunk, 0, chunk.length)) != -1) {
                            buffer.write(chunk, 0, nRead);
                        }
                        is.close();
                        String b64 = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
                        String safeName = fileName.replace("'", "\'");
                        webView.evaluateJavascript("window.onAndroidFilePicked('" + safeName + "', '" + b64 + "');", null);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "파일 열기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == NATIVE_SAVE_DOC_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(pendingSaveContent.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(this, "선택한 폴더에 도면이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "파일 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
