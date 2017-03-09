package com.rishabh.github.instagrabber;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class WebViewActivity extends AppCompatActivity {


  WebView mWebView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);
    mWebView = (WebView) findViewById(R.id.wvInstagram);

    WebSettings webSettings = mWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    String url=getIntent().getStringExtra("POST_URL");
    if(url!=null) {
      mWebView.loadUrl(url);
    }else{
      Toast.makeText(this, "URL Dead", Toast.LENGTH_SHORT).show();
    }
  }
}
