#---------------------------------------------------Android通用---------------------------------------------
# 指定代码的压缩级别 0 - 7(指定代码进行迭代优化的次数，在Android里面默认是5，这条指令也只有在可以优化时起作用。)
-optimizationpasses 5
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
#不进行优化，建议使用此选项，
-dontoptimize
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
# 屏蔽警告
-ignorewarnings
# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
-dontwarn android.annotation
#避免混淆内部类
-keepattributes InnerClasses
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
 #优化时允许访问并修改有修饰符的类和类的成员，这可以提高优化步骤的结果。
# 比如，当内联一个公共的getter方法时，这也可能需要外地公共访问。
# 虽然java二进制规范不需要这个，要不然有的虚拟机处理这些代码会有问题。当有优化和使用-repackageclasses时才适用。
#指示语：不能用这个指令处理库中的代码，因为有的类和类成员没有设计成public ,而在api中可能变成public
-allowaccessmodification
#当有优化和使用-repackageclasses时才适用。
-repackageclasses ''
 # 混淆时记录日志(打印混淆的详细信息)
 # 这句话能够使我们的项目混淆后产生映射文件
 # 包含有类名->混淆后类名的映射关系
-verbose
# 避免混淆Android基本组件，下面是兼容性比较高的规则

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService


# 保留support下的所有类及其内部类
-keep class android.support.* {*;}
-keep interface android.support.* {*;}
-keep public class * extends android.support.v4.*
-keep public class * extends android.support.v7.*
-keep public class * extends android.support.annotation.*
-dontwarn android.support.**

# 保留androidx下的所有类及其内部类
-keep class androidx.* {*;}
-keep public class * extends androidx.*
-keep interface androidx.* {*;}
-keep class com.google.android.material.* {*;}
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**


# 保持Activity中与View相关方法不被混淆
-keepclassmembers class * extends android.app.Activity{
        public void *(android.view.View);
}

# 避免混淆所有native的方法,涉及到C、C++
-keepclasseswithmembernames class * {
        native <methods>;
}

# 避免混淆自定义控件类的get/set方法和构造函数
-keep public class * extends android.view.View{
        *** get*();
        void set*(***);
        public <init>(android.content.Context);
        public <init>(android.content.Context,android.util.AttributeSet);
        public <init>(android.content.Context,android.util.AttributeSet,int);
}
-keepclasseswithmembers class * {
        public <init>(android.content.Context, android.util.AttributeSet);
        public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 避免混淆枚举类
-keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
}

# 避免混淆序列化类
# 不混淆Parcelable和它的实现子类，还有Creator成员变量
-keep class * implements android.os.Parcelable {
        public static final android.os.Parcelable$Creator *;
}

# 不混淆Serializable和它的实现子类、其成员变量
-keep public class * implements java.io.Serializable {*;}
-keepclassmembers class * implements java.io.Serializable {
        static final long serialVersionUID;
        private static final java.io.ObjectStreamField[] serialPersistentFields;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
}


# 资源ID不被混淆
#-keep class **.R$* {*;}
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 回调函数事件不能混淆
-keepclassmembers class * {
        void *(**On*Event);
        void *(**On*Listener);
}

# restore test method
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**


-keep class android.content.res.Resources{
     *;
}

 # X5内核忽略混淆
 -keep class com.tencent.* { *; }

 -keepclassmembers class android.webkit.WebView {
    public *;
 }
 -keepclassmembers class * extends android.webkit.WebViewClient {
     public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
     public boolean *(android.webkit.WebView, java.lang.String);
 }
 -keepclassmembers class * extends android.webkit.WebViewClient {
     public void *(android.webkit.WebView, java.lang.String);
 }

 # app HTML5 JavaScript
 -keepclassmembers class com.ljd.example.JSInterface {
     <methods>;
 }

# 使用GSON、fastjson等框架时，所写的JSON对象类不混淆，否则无法将JSON解析成对应的对象
-keepclassmembers class * {
         public <init>(org.json.JSONObject);
}

#（可选）避免Log打印输出
-assumenosideeffects class android.util.Log {
        public static *** v(...);
        public static *** d(...);
        public static *** i(...);
        public static *** w(...);
        public static *** e(...);
}

# kotlin 相关
-dontwarn kotlin.**
-keep class kotlin.* { *; }
-keep interface kotlin.* { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclasseswithmembers @kotlin.Metadata class * { *; }
-keepclassmembers class *.WhenMappings {
    <fields>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep class kotlinx.* { *; }
-keep interface kotlinx.* { *; }
-dontwarn kotlinx.**
-dontnote kotlinx.serialization.SerializationKt

-keep class org.jetbrains.* { *; }
-keep interface org.jetbrains.* { *; }
-dontwarn org.jetbrains.**

#---------------------------------------------------Android通用---------------------------------------------


# bean
-keep class com.moneycollect.android.model.* { *; }


# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    *;
}
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
  public static * inflate(android.view.LayoutInflater);
}
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}


# gson
# Gson specific classes
-keep class sun.misc.Unsafe.* { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.* { *; }
-keep class com.google.gson.stream.* { *; }
-keepattributes EnclosingMethod


# Lifecycle
# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}


# glide
-keep class com.bumptech.glide.*{ *; }
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-keep public class * extends com.bumptech.glide.module.GlideModule


# okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keep class io.reactivex.*{*;}
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


# rx
-keep class rx.internal.util.unsafe.* { *; }
-keep class android.databinding.* { *; }


# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.* { *; }
-keepattributes Exceptions
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn org.codehaus.mojo.**
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes EnclosingMethod
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.* <methods>;
}


# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}