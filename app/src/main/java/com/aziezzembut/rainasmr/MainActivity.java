package com.aziezzembut.rainasmr;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.InputStream;
import java.util.ArrayList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final int PICK_FILE = 1001;
    private static final String START_URL = "https://appassets.androidplatform.net/assets/index.html";

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.parseColor("#12233b"));
        getWindow().setNavigationBarColor(Color.parseColor("#0f1d31"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0b1626"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = params.createIntent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, PICK_FILE);
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "Tidak ada aplikasi pemilih file", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(START_URL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FILE || fileCallback == null) return;
        final ValueCallback<Uri[]> cb = fileCallback;
        fileCallback = null;
        final Uri[] picked = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
        if (picked == null || picked.length == 0) {
            cb.onReceiveValue(null);
            return;
        }
        Toast.makeText(this, "Menyiapkan file...", Toast.LENGTH_SHORT).show();
        // Salin dulu ke penyimpanan aplikasi: banyak pengelola file melaporkan ukuran/tanggal
        // yang tidak cocok sehingga WebView menolak membaca langsung (NotReadableError).
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Uri> out = new ArrayList<>();
                File dir = new File(getCacheDir(), "picked");
                deleteChildren(dir);
                dir.mkdirs();
                for (int i = 0; i < picked.length; i++) {
                    Uri src = picked[i];
                    if (src == null) continue;
                    File dst = new File(dir, i + "_" + displayName(src));
                    InputStream in = null;
                    OutputStream os = null;
                    try {
                        in = getContentResolver().openInputStream(src);
                        os = new FileOutputStream(dst);
                        byte[] buf = new byte[1 << 16];
                        int n;
                        while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
                        out.add(Uri.fromFile(dst));
                    } catch (Exception e) {
                        out.add(src); // cadangan: coba baca langsung
                    } finally {
                        try { if (in != null) in.close(); } catch (Exception ignored) { }
                        try { if (os != null) os.close(); } catch (Exception ignored) { }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cb.onReceiveValue(out.toArray(new Uri[0]));
                    }
                });
            }
        }).start();
    }

    private String displayName(Uri uri) {
        String name = null;
        Cursor c = null;
        try {
            c = getContentResolver().query(uri, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        if (name == null || name.trim().isEmpty()) {
            name = uri.getLastPathSegment();
        }
        if (name == null || name.trim().isEmpty()) name = "file";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static void deleteChildren(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    /** Dipanggil dari JavaScript untuk menyimpan file ke Download/AziezZembut (dikirim per potongan). */
    private class Bridge {
        private OutputStream out;
        private String location;

        @JavascriptInterface
        public boolean begin(String name, String mime) {
            try {
                closeQuietly();
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                    v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                    v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BerkahASMR");
                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                    if (uri == null) return false;
                    out = getContentResolver().openOutputStream(uri);
                    location = "Download/BerkahASMR/" + name;
                } else {
                    File dir = new File(getExternalFilesDir(null), "BerkahASMR");
                    if (!dir.exists() && !dir.mkdirs()) return false;
                    File f = new File(dir, name);
                    out = new FileOutputStream(f);
                    location = f.getAbsolutePath();
                }
                return out != null;
            } catch (Exception e) {
                out = null;
                return false;
            }
        }

        @JavascriptInterface
        public boolean chunk(String base64) {
            try {
                out.write(Base64.decode(base64, Base64.DEFAULT));
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public String end() {
            closeQuietly();
            return location == null ? "" : location;
        }

        @JavascriptInterface
        public void toast(final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

        private void closeQuietly() {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (Exception ignored) {
                }
                out = null;
            }
        }
    }
}
