package kg.timmitof.build_logic.convention

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object ProjectConfig {
    /** Базовый префикс namespace всех модулей проекта. */
    const val BASE_NAMESPACE = "kg.timmitof"

    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val TARGET_SDK = 36

    const val VERSION_CODE = 4
    const val VERSION_NAME = "1.1.0"

    val JAVA_VERSION = JavaVersion.VERSION_11
    val JVM_TARGET = JvmTarget.JVM_11
}