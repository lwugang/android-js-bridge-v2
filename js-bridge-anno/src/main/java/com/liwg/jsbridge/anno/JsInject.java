package com.liwg.jsbridge.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lwg on 17-7-3.
 * 只注入被该注解标记的类,类中的方法默认会全部注入
 */
@Retention(RetentionPolicy.CLASS) @Target({ ElementType.TYPE }) public @interface JsInject {
  /**
   * 注入的对象名
   */
  String value() default "";

  /**
   * 过滤类中的某个方法，不注入
   * @return
   */
  String[] filter() default {};
}
