# =====================================================================
# ProGuard/R8 правила для release-сборки Keyboard Fonts
# Базовые правила берутся из proguard-android-optimize.txt (см. build.gradle.kts)
# Здесь — только то, что специфично для этого проекта.
# =====================================================================

# --- Сохраняем номера строк для читаемых стектрейсов в Play Console ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# =====================================================================
# kotlinx.serialization — type-safe навигация (core:navigation graphs)
# и JSON-ассеты клавиатуры (keyboard/data/.../models).
# =====================================================================
-keepattributes Signature
-keepattributes *Annotation*
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
# Enum-ы (сериализация использует values()/valueOf()).
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
