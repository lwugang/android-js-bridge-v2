/**
 * Summary: 应用中使用的WebChromeClient基类
 * Version 1.0
 * Date: 13-11-8
 * Time: 下午2:31
 * Copyright: Copyright (c) 2013
 */

package com.liwg.jsbridge.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BridgeWebViewClient extends WebViewClient {
  private WebViewClient webViewClient;

  private JsCallJava inject;

  public BridgeWebViewClient(WebViewClient webViewClient, JsCallJava inject) {
    this.webViewClient = webViewClient;
    this.inject = inject;
  }

  //   ----------------需要处理的方法 start-------------------
  @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
    //iInject.inject(view);
    webViewClient.onPageStarted(view, url, favicon);
  }

  @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return webViewClient.shouldOverrideUrlLoading(view, url);
  }
  //   ----------------需要处理的方法 end-------------------

  @Override public void onFormResubmission(WebView view, Message dontResend, Message resend) {
    webViewClient.onFormResubmission(view, dontResend, resend);
  }

  @Override public void onLoadResource(WebView view, String url) {
    //iInject.inject(view);
    webViewClient.onLoadResource(view, url);
  }


  @Override public void onPageFinished(WebView view, String url) {
    //iInject.inject(view);
    webViewClient.onPageFinished(view, url);
  }

  @Override
  public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host,
      String realm) {
    webViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    webViewClient.onReceivedError(view, errorCode, description, failingUrl);
  }

  @Override public void onReceivedHttpError(WebView view, WebResourceRequest request,
      WebResourceResponse errorResponse) {
    webViewClient.onReceivedHttpError(view, request, errorResponse);
  }

  @Override
  public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
    webViewClient.onReceivedLoginRequest(view, realm, account, args);
  }

  @Override public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler,
      SslError sslError) {
    webViewClient.onReceivedSslError(webView, sslErrorHandler, sslError);
    sslErrorHandler.proceed();
  }


  @Override public void onScaleChanged(WebView view, float oldScale, float newScale) {
    webViewClient.onScaleChanged(view, oldScale, newScale);
  }

  @Override public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
    webViewClient.onUnhandledKeyEvent(view, event);
  }

  @Override public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
    webViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
  }


  @Override public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
    return webViewClient.shouldOverrideKeyEvent(view, event);
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
    //iInject.inject(view);
    return webViewClient.shouldInterceptRequest(view, request);
  }

  @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    //iInject.inject(view);
    return webViewClient.shouldInterceptRequest(view, url);
  }

  @Override public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
    //iInject.inject(view);
    webViewClient.doUpdateVisitedHistory(view, url, isReload);
  }

  @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    return webViewClient.shouldOverrideUrlLoading(view, request);
  }

  @Override public void onPageCommitVisible(WebView view, String url) {
    webViewClient.onPageCommitVisible(view, url);
  }

  @Override
  public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
    webViewClient.onReceivedError(view, request, error);
  }

  @Override public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
    webViewClient.onReceivedClientCertRequest(view, request);
  }
}
