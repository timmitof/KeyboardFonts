plugins { id("keyboardfonts.data") }

dependencies {
    // Разрешение com.android.vending.BILLING библиотека добавляет в манифест сама.
    implementation(libs.billing.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
