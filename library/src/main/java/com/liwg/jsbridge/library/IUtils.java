package com.liwg.jsbridge.library;

/**
 * @author lwg
 * @e-mail liwg644@13322.com
 * @time 5/4/18
 * @desc
 * @version: V3.1.3
 */
public interface IUtils {
  String getJsCode();

  void register(String name, Object obj);

  Object queryJavaObject(String name);

  //void callJsReady(BridgeWebView webView);
}
