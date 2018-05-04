package com.src.wugang.jsbridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.liwg.jsbridge.library.BridgeWebView;
import com.liwg.jsbridge.library.PreLoadManager;

/**
 * @author lwg
 * @e-mail liwg644@13322.com
 * @time 5/4/18
 * @desc
 * @version: V3.1.3
 */

public class PreLoadActivity extends AppCompatActivity {
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    BridgeWebView webView = PreLoadManager.get(this).getWebView();
    setContentView(webView);
    webView.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if(request.hasGesture()){
          Intent intent = new Intent(PreLoadActivity.this,PreLoadActivity.class);
          intent.putExtra("url",request.getUrl());
          startActivity(intent);
          return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
      }
    });
    webView.loadUrl(getIntent().getStringExtra("url"));
  }
}
