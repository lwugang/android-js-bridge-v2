package com.liwg.jsbridge.inject;

import com.google.auto.service.AutoService;
import com.liwg.jsbridge.anno.JsInject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 注解处理器
 */
@AutoService(Processor.class) public class InjectProcessor extends AbstractProcessor {

  /**
   * 文件相关的辅助类
   */
  private Filer mFiler;
  /**
   * 元素相关的辅助类
   */
  private Elements mElementUtils;
  /**
   * 日志相关的辅助类
   */
  private Messager mMessager;

  //返回注解处理器可处理的注解操作
  //@Override public Set<String> getSupportedOptions() {
  //  return getSupportedAnnotationTypes();
  //}

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_7;
  }

  //得到注解处理器可以支持的注解类型
  @Override public Set<String> getSupportedAnnotationTypes() {
    HashSet<String> objects = new HashSet<>();
    objects.add(JsInject.class.getName());
    return objects;
  }

  //执行一些初始化逻辑
  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    mFiler = processingEnv.getFiler();
    mElementUtils = processingEnv.getElementUtils();
    mMessager = processingEnv.getMessager();
  }

  //核心方法，扫描，解析并处理自定义注解，生成***.java文件
  @Override public boolean process(Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {
    try {
      processImpl(roundEnv);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private void processImpl(RoundEnvironment roundEnv) throws ClassNotFoundException, IOException {
    Set<? extends Element> elementsAnnotatedWith =
        roundEnv.getElementsAnnotatedWith(JsInject.class);
    Iterator<? extends Element> iterator = elementsAnnotatedWith.iterator();
    String objectJs = "if(typeof(window.%s)=='undefined'){ window.%s = {";
    String methodJs = "%s:function(){"
        + " return EasyJS.call('%s', '%s', Array.prototype.slice.call(arguments));},";
    final StringBuilder objectSb = new StringBuilder();
    Map<String, String> map = new HashMap<>();
    while (iterator.hasNext()) {
      Element next = iterator.next();
      List<? extends Element> methodElements = next.getEnclosedElements();
      JsInject jsInject = next.getAnnotation(JsInject.class);
      //String classType = next.asType().toString();
      //error(next.getEnclosingElement() + "-----" + next.getKind() + "-----" + next.getSimpleName()
      //        +"----------"+next.getEnclosedElements()+"------"+next.asType().toString()+"--------",
      //    next);
      TypeElement typeElement = (TypeElement) next;
//      if (!typeElement.getSuperclass().toString().equals("com.liwg.jsbridge.library.JsPlugin")) {
//        info("cover JsInject note class must extends JsPlugin", next);
//      }
      String objectName = jsInject.value();
      if (objectName == null || objectName.length() < 1) {
        objectName = typeElement.getSimpleName().toString();
      }
      map.put(objectName, String.format("new %s()", typeElement.getQualifiedName().toString()));
      objectSb.append(String.format(objectJs, objectName, objectName));
      final StringBuilder methodSb = new StringBuilder();
      for (int i = 0; i < methodElements.size(); i++) {
        Element element = methodElements.get(i);
        if(!(element instanceof ExecutableElement))
          continue;
        ExecutableElement method = (ExecutableElement) element;
        String methodName = method.getSimpleName().toString();
        // 存在一个 <init>方法，过滤掉
        if (methodName.contains("<")) continue;
        if (!method.getModifiers().contains(Modifier.PUBLIC)) {
          //必须是public 修饰的方法
          continue;
        }
        if (!filterMethod(jsInject.filter(), methodName)) {
          //不需要过滤次方法
          methodSb.append(String.format(methodJs, methodName, objectName, methodName));
        }
      }
      if (methodSb.length() > 0) methodSb.deleteCharAt(methodSb.length() - 1);
      objectSb.append(methodSb);
      objectSb.append("}}");
    }
    objectSb.append("if(window.EasyJS&&window.EasyJS.injectFlag==0){if(JSBridgeReady){JSBridgeReady();window.EasyJS.injectFlag=1}}");
    String jsCode = objectSb.toString();
    JavaFileObject sourceFile = mFiler.createSourceFile("com.liwg.jsbridge.library.JSBridge");
    Writer writer = null;
    try {
      writer = sourceFile.openWriter();
      writer.write("package com.liwg.jsbridge.library;\n\n");
      writer.write("final class JSBridge implements com.liwg.jsbridge.library.IJSBridge{\n");
      writer.write("    public static final JSBridge INSTANCE = new JSBridge();\n");
      writer.write("    public java.util.Map<String,Object> map = new java.util.HashMap<>();\n");
      writer.write("    private JSBridge(){\n");
      for (Map.Entry<String, String> entry : map.entrySet()) {
        writer.write(String.format("     map.put(\"%s\",%s); \n", entry.getKey(), entry.getValue()));
      }
      writer.write("    }\n");
      writer.write("    public static final JSBridge get(){\n");
      writer.write("      return INSTANCE;\n");
      writer.write("    }\n");
      writer.write("    public String getJsCode(){\n");
      writer.write("      return \"" + jsCode + "\";\n");
      writer.write("    }\n");
      writer.write(
          "    /**注册对象， 被@JsPlugin注解标记的对象会自动注入，并调用空参的构造函数，\n  如果需要重写构造，需保留空参的构造，并调用此方法注册\n  */\n");
      writer.write("    public void register(String name,Object obj){\n");
      writer.write("      map.put(name,obj);\n");
      writer.write("    }\n");
      writer.write("    public Object queryJavaObject(String name){\n");
      writer.write("     return map.get(name);\n");
      writer.write("    }\n");
/*      writer.write("    public void callJsReady(com.liwg.jsbridge.library.BridgeWebView webview){\n");
      writer.write("     webview.callJsMethod(\"JSBridgeReady()\");\n");
      writer.write("    }\n");*/
      writer.write("  }\n");
    } finally {
      writer.close();
    }
  }

  private boolean filterMethod(String[] filter, String methodName) {
    int length = filter == null ? 0 : filter.length;
    for (int i = 0; i < length; i++) {
      if (filter[i].equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  void error(CharSequence msg, Element element) {
    mMessager.printMessage(Diagnostic.Kind.ERROR, msg, element);
  }

  void info(CharSequence msg, Element element) {
    mMessager.printMessage(Diagnostic.Kind.WARNING, msg, element);
  }
}
