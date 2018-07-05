package com.liwg.jsbridge.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lwg on 17-6-27.<br/>
 * <b>js 插件类,需要注入的类，推荐继承JsPlugin，必须保留空参的构造函数</b>
 */
public class JsPlugin {
  public JsPlugin() {

  }

  protected JSONObject getJsonObject(String str) {
    JSONObject jsonObject = null;

    try {
      jsonObject = new JSONObject(str);
    } catch (JSONException var4) {
      var4.printStackTrace();
    }

    return jsonObject;
  }

  protected JSONArray getJsonArray(String str) {
    JSONArray jsonArray = null;

    try {
      jsonArray = new JSONArray(str);
    } catch (JSONException var4) {
      var4.printStackTrace();
    }

    return jsonArray;
  }
}
