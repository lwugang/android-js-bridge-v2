# android-js-bridge-v2
### android js 互相调用 第二版
- ##### 支持js匿名函数接收
- ##### 支持js json对象接收
- ##### 支持js函数返回值获取
- ##### 支持js调用原生直接获取返回值
- ##### 通过注解注入js方法
- ##### 优化第一版的反射注入方式，采用注解处理器编译时生成注入代码,提高运行效率
- ##### 加入简单的 webview 预加载功能

![](https://github.com/lwugang/android-js-bridge-v2/blob/master/device-2018-07-05-112940.gif)

Add it in your root build.gradle at the end of repositories:
~~~gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
~~~

Add the dependency
~~~gradle
  dependencies {
      implementation 'com.github.lwugang.android-js-bridge-v2:library:v2.0.3'
	    implementation 'com.github.lwugang.android-js-bridge-v2:js-bridge-anno:v2.0.3'
	    annotationProcessor 'com.github.lwugang.android-js-bridge-v2:js-bridge-compiler:v2.0.3'
	}

~~~

#### 使用方式
~~~xml
	<com.wugang.jsbridge.library.BridgeWebView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/web_view"/>
~~~
### Activity
-  所有需要注入的对象推荐继承JsPlugin这个类并且 加上 @JsInject 注解标记
- 被 @JsInject 标记的类会被自动注入，并调用空参的构造创建对象，如果有自定义构造 可以使用 ***webView.getJsBridge().register()***
- 如果该类中的方法不希望被注入可以 使用 @JsInject 注解上的 filter参数过滤掉
~~~java
    public class MainActivity extends AppCompatActivity {

       @Override protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main);
          BridgeWebView webView = (BridgeWebView) findViewById(R.id.web_view);
          // 如果需要自定义构造函数，需要手动注册
          webView.getJsBridge().register("AB",new AB(this));
          webView.loadUrl("file:///android_asset/test.html");
          WebView.setWebContentsDebuggingEnabled(true);
       }

      @JsInject public static class AB extends JsPlugin {
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
          public String getResult(){
              return "nativeResult";
          }
          public void test1() {
            Log.e("-------", "test1: ");
          }

          public void test2() {
            Log.e("-------", "test2: ");
          }
      }
    }
~~~
HTML&JS代码
~~~js
<html>
<script>
        window.JSBridgeReady=function(){
            console.log("---window EasyJSReady---")
        }
        function test(){
             AB.test({"aa":123},function(ret){
                alert("native 回调我"+JSON.stringify(ret))
            })
        }
</script>
<body>
<button onclick="javascript:location.reload()">refresh</button>
<button onclick="test()">Test</button>
<!--获取android返回的结果-->
<button onclick="javascript:alert(AB.getResult())">getNativeResult</button>
</body>
</html>
~~~
#### 网页预加载
~~~java
   //预加载，推荐在Application中调用
   PreLoadManager.get(this).preload("http://www.baidu.com", "http://www.youku.com");
~~~
###### Activity 中使用
~~~java
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
~~~

#### 推荐使用者不要混淆代码
        -keep com.liwg.jsbridge.library.**{*;}

[参考项目https://github.com/lwugang/safe-java-js-webview-bridge](https://github.com/lwugang/safe-java-js-webview-bridge)

[参考项目https://github.com/dukeland/EasyJSWebView](https://github.com/dukeland/EasyJSWebView)
