package com.liwg.jsbridge.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by lwg on 17-6-27.
 */

public class BridgeWebView extends WebView {

  private JsCallJava inject;
  private BridgeWebViewClient bridgeWebViewClient;
  private BridgeChromeClient bridgeChromeClient;
  private static IJSBridge utils;

  public BridgeWebView(Context context) {
    super(context);
    init();
  }

  public BridgeWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public BridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public static IJSBridge getJsBridge() {
    return utils;
  }

  static {
    try {
      utils = (IJSBridge) Class.forName("com.liwg.jsbridge.library.JSBridge")
          .getMethod("get")
          .invoke(null);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void init() {
    inject = new JsCallJava(utils);
    inject.addJavascriptInterfaces(this);
    removeSearchBoxJavaBridge();
    if (!getSettings().getJavaScriptEnabled()) getSettings().setJavaScriptEnabled(true);
  }

  @Override public void setWebViewClient(WebViewClient client) {
    super.setWebViewClient(bridgeWebViewClient = new BridgeWebViewClient(client, inject));
  }

  @Override public void setWebChromeClient(WebChromeClient client) {
    super.setWebChromeClient(
        bridgeChromeClient = new BridgeChromeClient(client, inject, this, utils));
  }

  public void loadUrl(final String url) {
    initClient();
    super.loadUrl(url);
  }

  @Override public void loadData(String data, String mimeType, String encoding) {
    initClient();
    super.loadData(data, mimeType, encoding);
  }

  @Override public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
    initClient();
    super.loadUrl(url, additionalHttpHeaders);
  }

  @Override
  public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding,
      String historyUrl) {
    initClient();
    super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
  }

  private void initClient() {
    if (bridgeWebViewClient == null) {
      this.setWebViewClient(new WebViewClient());
    }
    if (bridgeChromeClient == null) this.setWebChromeClient(new WebChromeClient());
  }

  /**
   * 解决WebView远程执行代码漏洞，避免被“getClass”方法恶意利用（在loadUrl之前调用，如：MyWebView(Context context, AttributeSet
   * attrs)里面）；
   * 漏洞详解：http://drops.wooyun.org/papers/548
   * <p/>
   * function execute(cmdArgs)
   * {
   * for (var obj in window) {
   * if ("getClass" in window[obj]) {
   * alert(obj);
   * return ?window[obj].getClass().forName("java.lang.Runtime")
   * .getMethod("getRuntime",null).invoke(null,null).exec(cmdArgs);
   * }
   * }
   * }
   */
  @TargetApi(11) protected boolean removeSearchBoxJavaBridge() {
    removeJavascriptInterface("searchBoxJavaBridge_");
    return false;
  }

  /**
   * 调用js方法
   */
  public void callJsMethod(String method) {
    if (Build.VERSION.SDK_INT >= 19) {
      evaluateJavascript(method, new ValueCallback<String>() {
        @Override public void onReceiveValue(String value) {
          Log.e("----------", "onReceiveValue: " + value);
        }
      });
    } else {
      super.loadUrl("javascript:" + method);
    }
  }
}
