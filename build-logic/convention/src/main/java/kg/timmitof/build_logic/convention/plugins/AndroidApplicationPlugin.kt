package kg.timmitof.build_logic.convention.plugins

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import kg.timmitof.build_logic.convention.AndroidConfig
import kg.timmitof.build_logic.convention.androidTestImplementation
import kg.timmitof.build_logic.convention.debugImplementation
import kg.timmitof.build_logic.convention.implementation
import kg.timmitof.build_logic.convention.libs
import kg.timmitof.build_logic.convention.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("com.android.application") {
            configureApplication()
        }
    }

    private fun Project.configureApplication() {
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<BaseAppModuleExtension> {
            namespace = "kg.timmitof.keyboardfonts"
            compileSdkVersion(AndroidConfig.COMPILE_SDK)

            defaultConfig {
                applicationId = "kg.timmitof.keyboardfonts"
                minSdk = AndroidConfig.MIN_SDK
                targetSdk = AndroidConfig.TARGET_SDK
                versionCode = AndroidConfig.VERSION_CODE
                versionName = AndroidConfig.VERSION_NAME
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA_VERSION
                targetCompatibility = AndroidConfig.JAVA_VERSION
            }

            buildFeatures {
                compose = true
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(AndroidConfig.JVM_TARGET)
                }
            }

            dependencies {
                implementation(project(":core:ui"))
                implementation(project(":core:navigation"))

                implementation(libs.findLibrary("androidx-core-ktx").get())
                implementation(libs.findLibrary("androidx-activity-compose").get())
                implementation(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                implementation(libs.findLibrary("androidx-activity-compose").get())
                implementation(platform(libs.findLibrary("androidx-compose-bom").get()))
                implementation(libs.findLibrary("androidx-ui").get())
                implementation(libs.findLibrary("androidx-ui-graphics").get())
                implementation(libs.findLibrary("androidx-ui-tooling-preview").get())
                implementation(libs.findLibrary("androidx-material3").get())

                testImplementation(libs.findLibrary("junit").get())
                androidTestImplementation(libs.findLibrary("androidx-junit").get())
                androidTestImplementation(libs.findLibrary("androidx-espresso-core").get())
                androidTestImplementation(platform(libs.findLibrary("androidx-compose-bom").get()))
                androidTestImplementation(libs.findLibrary("androidx-ui-test-junit4").get())
                debugImplementation(libs.findLibrary("androidx-ui-tooling").get())
                debugImplementation(libs.findLibrary("androidx-ui-test-manifest").get())
            }
        }
    }
}