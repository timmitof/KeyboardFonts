# =====================================================================
# ProGuard/R8 правила для release-сборки Keyboard Fonts
# Базовые правила берутся из proguard-android-optimize.txt (см. build.gradle.kts)
# Здесь — только то, что специфично для этого проекта.
# =====================================================================

# --- Сохраняем номера строк для читаемых стектрейсов в Play Console ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# =====================================================================
# Gson — раскладки клавиатуры парсятся рефлексией из JSON-ассетов
# (keyboard/data/.../repository/KeyboardLayoutRepositoryImpl.kt).
# Имена полей DTO берутся из JSON, поэтому их нельзя переименовывать.
# =====================================================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# DTO-модели раскладок — сохраняем классы и все поля целиком.
-keep class kg.timmitof.keyboard.data.models.** { *; }

# Общие правила Gson (на случай сериализации/десериализации через рефлексию).
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# =====================================================================
# kotlinx.serialization — type-safe навигация (core:navigation graphs).
# =====================================================================
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Сохраняем сгенерированные сериализаторы.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class kg.timmitof.**$$serializer { *; }
-keepclassmembers class kg.timmitof.** {
    *** Companion;
}
-keepclasseswithmembers @kotlinx.serialization.Serializable class kg.timmitof.** {
    <init>(...);
}

# =====================================================================
# Enum-ы (Gson/сериализация используют values()/valueOf()).
# =====================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =====================================================================
# Parcelable (на случай передачи моделей между компонентами).
# =====================================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
