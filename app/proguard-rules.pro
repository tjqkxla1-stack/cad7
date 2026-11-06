# Keep WebView JS Bridge for YCAD DWG/DXF Native I/O
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
