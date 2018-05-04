package com.liwg.jsbridge.library;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author lwg
 * @e-mail liwg644@13322.com
 * @time 5/4/18
 * @desc webview 预加载
 * @version: V3.1.3
 */

public class PreLoadManager {
  private BridgeWebView webView;
  private static PreLoadManager manager;

  private PreLoadManager(Context context) {
    webView = new BridgeWebView(context.getApplicationContext());
  }

  public static PreLoadManager get(Context context) {
    if (manager == null) {
      synchronized (PreLoadManager.class) {
        if (manager == null) {
          manager = new PreLoadManager(context);
        }
      }
    }
    return manager;
  }

  /**
   * 预加载
   */
  public void preload(String... urls) {
    final LinkedList<String> list = new LinkedList(Arrays.asList(urls));
    WebSettings settings = webView.getSettings();
    settings.setDatabaseEnabled(true);
    settings.setAppCacheEnabled(true);
    webView.loadUrl(list.poll());
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String poll = list.poll();
        if(poll==null){
          return;
        }
        webView.loadUrl(poll);
      }
    });
  }

  public BridgeWebView getWebView() {
    ViewParent parent = webView.getParent();
    if (parent != null) {
      ((ViewGroup) parent).removeView(webView);
    }
    webView.loadUrl("about:black");
    return webView;
  }
}
