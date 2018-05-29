package com.liwg.jsbridge.library;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsCallJava {
  String INJECT_JS = "if(!window.EasyJS){\n" +
          "    window.EasyJS = {\n" +
          "        __callbacks: {},\n" +
          "        injectFlag:0,\n" +
          "        isJson:function(obj){\n" +
          "            var isjson = typeof(obj) == \"object\" && Object.prototype.toString.call(obj).toLowerCase() == \"[object object]\" && !obj.length;   \n" +
          "            return isjson;  \n" +
          "        },\n" +
          "        invokeCallback: function (cbID, removeAfterExecute){\n" +
          "            var args = Array.prototype.slice.call(arguments);\n" +
          "            args.shift();\n" +
          "            args.shift();\n" +
          "            /*for (var i = 0, l = args.length; i < l; i++){\n" +
          "                args[i] = decodeURIComponent(args[i]);\t\t\n" +
          "            }*/\n" +
          "            var cb = EasyJS.__callbacks[cbID];\n" +
          "            if (removeAfterExecute){\n" +
          "                EasyJS.__callbacks[cbID] = undefined;\n" +
          "            }\n" +
          "            return cb.apply(null, args);\n" +
          "        },\n" +
          "        \n" +
          "        call: function (obj, functionName, args){\n" +
          "            var formattedArgs = [];\n" +
          "            for (var i = 0, l = args.length; i < l; i++){\n" +
          "                if (typeof args[i] == \"function\"){\n" +
          "                    formattedArgs.push(\"f\");\n" +
          "                    var cbID = \"__cb\" + parseInt((+new Date)*Math.random()*Math.random()*Math.random());\n" +
          "                    EasyJS.__callbacks[cbID] = args[i];\n" +
          "                    formattedArgs.push(cbID);\n" +
          "                }else if(EasyJS.isJson(args[i])){\n" +
          "                    formattedArgs.push(\"json\");\n" +
          "                    formattedArgs.push(encodeURIComponent(JSON.stringify(args[i])));\n" +
          "                }else{\n" +
          "                    formattedArgs.push(\"s\");\n" +
          "                    formattedArgs.push(encodeURIComponent(args[i]));\n" +
          "                }\n" +
          "            }\n" +
          "            \n" +
          "            var argStr = (formattedArgs.length > 0 ? \":\" + encodeURIComponent(formattedArgs.join(\":\")) : \"\");\n" +
          "            \n" +
          "            var iframe = document.createElement(\"IFRAME\");\n" +
          "            iframe.setAttribute(\"src\", \"easy-js:\" + obj + \":\" + encodeURIComponent(functionName) + argStr);\n" +
          "            document.documentElement.appendChild(iframe);\n" +
          "            iframe.parentNode.removeChild(iframe);\n" +
          "            iframe = null;\n" +
          "            \n" +
          "            var ret = EasyJS.retValue;\n" +
          "            EasyJS.retValue = undefined;\n" +
          "            \n" +
          "            if (ret){\n" +
          "                return decodeURIComponent(ret);\n" +
          "            }\n" +
          "        },\n" +
          "        \n" +
          "        inject: function (obj, methods){\n" +
          "            if(typeof(window[obj])!='undefined')\n" +
          "                return;\n" +
          "            window[obj] = {};\n" +
          "            var jsObj = window[obj];\n" +
          "            for (var i = 0, l = methods.length; i < l; i++){\n" +
          "                (function (){\n" +
          "                    var method = methods[i];\n" +
          "                    var jsMethod = method.replace(new RegExp(\":\", \"g\"), \"\");\n" +
          "                    jsObj[jsMethod] = function (){\n" +
          "                        return EasyJS.call(obj, method, Array.prototype.slice.call(arguments));\n" +
          "                    };\n" +
          "                })();\n" +
          "            }\n" +
          "        }\n" +
          "    };\n" +
          "}";
  //返回值回调队列
  private Map<String, JSFunction> arrayMap;

  private Handler handler = new Handler(Looper.getMainLooper());

  private IJSBridge utils;

  public JsCallJava(IJSBridge utils) {
    this.utils = utils;
  }

  public void addJavascriptInterfaces(BridgeWebView bridgeWebView) {
    //预注入一个获取js返回值的对象
    bridgeWebView.addJavascriptInterface(this, JSFunction.INJECT_OBJ_NAME);
  }

  /**
   * javascript 注入一个获取返回值的方法
   */
  @JavascriptInterface public void returnValue(String callbackId, String result) {
    JSFunction jsFunction = arrayMap.get(callbackId);
    if (jsFunction == null) return;
    JsReturnValueCallback returnValueCallback = jsFunction.returnValueCallback;
    if (returnValueCallback != null) {
      returnValueCallback.onReturnValue(result);
      arrayMap.remove(callbackId);
    }
  }

  public void inject(WebView view) {
    loadJs(view, utils.getJsCode());
  }

  private void loadJs(final WebView view, final String jsCode) {
    handler.post(new Runnable() {
      @Override public void run() {
        view.loadUrl("javascript:{" + INJECT_JS + "\n" + jsCode + "}");
      }
    });
  }

  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (url.startsWith("easy-js:")) {
      String[] strings = url.split(":");
      //js调用的对象
      String obj = strings[1];
      //js调用的方法名
      String methodName = strings[2];
      //js调用对象对应的 java对象
      Object destJavaObj = utils.queryJavaObject(obj);
      try {
        List<Object> javaMethodParams = new ArrayList<>();
        if (strings.length > 3) {//表示有参数
          String[] args = URLDecoder.decode(strings[3], "UTF-8").split(":");
          for (int i = 0, j = 0, l = args.length; i < l; i += 2, j++) {
            String argsType = args[i];
            String argsValue = args[i + 1];
            if ("f".equals(argsType)) {//f 表示这个参数是一个函数
              JSFunction func = new JSFunction();
              javaMethodParams.add(func);
              if (arrayMap == null) arrayMap = new HashMap<>();
              String key = new String(Base64.encode(url.getBytes(), Base64.DEFAULT)).trim();
              func.initWithWebView((BridgeWebView) view, argsValue, key);
              arrayMap.put(key, func);
            } else if ("s".equals(argsType)) {
              javaMethodParams.add(URLDecoder.decode(argsValue, "UTF-8"));
            } else if("json".equals(argsType)){
              javaMethodParams.add(new JSONObject(URLDecoder.decode(argsValue, "UTF-8")));
            }
          }
        }
        invoke(destJavaObj, methodName, javaMethodParams.toArray());
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    }
    return false;
  }

  private void invoke(Object javaObj, String methodName, Object[] objects)
      throws InvocationTargetException, IllegalAccessException {
    Method[] declaredMethods = javaObj.getClass().getDeclaredMethods();
    for (int i = 0; i < declaredMethods.length; i++) {
      String name = declaredMethods[i].getName();
      if (methodName != null && methodName.equals(name)) {
        Class<?>[] parameterTypes = declaredMethods[i].getParameterTypes();
        int length = parameterTypes.length;
        //如果方法声明的参数长度和 实际参数个数不想等
        if (length != objects.length) continue;
        declaredMethods[i].invoke(javaObj, getValueByType(parameterTypes, objects));
        return;
      }
    }
  }

  private Object[] getValueByType(Class<?>[] parameterTypes, Object[] objects) {
    List<Object> objectList = new ArrayList<>();
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> type = parameterTypes[i];
      if (type == int.class) {
        objectList.add(Integer.parseInt(objects[i].toString()));
      } else if (type == double.class) {
        objectList.add(Double.parseDouble(objects[i].toString()));
      } else if (type == float.class) {
        objectList.add(Float.parseFloat(objects[i].toString()));
      } else if (type == byte.class) {
        objectList.add(Byte.parseByte(objects[i].toString()));
      } else if (type == long.class) {
        objectList.add(Long.parseLong(objects[i].toString()));
      } else if (type.isArray()) {
        if (type.getComponentType() == JSFunction.class) {
          JSFunction[] jsFunctions = new JSFunction[objects.length - i];
          System.arraycopy(objects, i, jsFunctions, 0, jsFunctions.length);
          objectList.add(jsFunctions);
        } else {
          objectList.add(objects);
        }
        return objectList.toArray();
      } else {
        objectList.add(objects[i]);
      }
    }
    return objectList.toArray();
  }
}