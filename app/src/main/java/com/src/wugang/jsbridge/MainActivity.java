package com.src.wugang.jsbridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import com.apkfuns.jsbridge.JsBridge;
import com.liwg.jsbridge.anno.JsInject;
import com.liwg.jsbridge.library.BridgeWebView;
import com.liwg.jsbridge.library.JSFunction;
import com.liwg.jsbridge.library.JsPlugin;
import com.liwg.jsbridge.library.PreLoadManager;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    PreLoadManager.get(this).preload("http://www.baidu.com", "http://www.youku.com");
    BridgeWebView webView = (BridgeWebView) findViewById(R.id.web_view);
    webView.getJsBridge().register("AB",new AB(this));
    webView.loadUrl("file:///android_asset/test.html");
    WebView.setWebContentsDebuggingEnabled(true);
  }

  public void youku(View v) {
    Intent intent = new Intent(this, PreLoadActivity.class);
    intent.putExtra("url", "http://www.youku.com");
    startActivity(intent);
  }

  public void baidu(View v) {
    Intent intent = new Intent(this, PreLoadActivity.class);
    intent.putExtra("url", "http://www.baidu.com");
    startActivity(intent);
  }

  @JsInject public static class AB {
    private Context context;

    public AB() {
    }

    public AB(Context context) {
      this.context = context;
    }

    public void test(String s, JSFunction jsFunction) throws JSONException {
      Toast.makeText(context, "js调用我", 1).show();
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("aa",12);
      jsFunction.execute(jsonObject.toString());
    }

    public void test1() {
      Log.e("-------", "test1: ");
    }

    public void test2() {
      Log.e("-------", "test2: ");
    }
  }
}
