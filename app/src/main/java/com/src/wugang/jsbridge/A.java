package com.src.wugang.jsbridge;

import com.liwg.jsbridge.anno.JsInject;
import com.liwg.jsbridge.library.JsPlugin;

@JsInject(value = "Plugin",filter = {"test2"}) public class A extends JsPlugin {
  public static void test() {
  }

  public static void test1() {
  }

  public static void test2() {
  }
}