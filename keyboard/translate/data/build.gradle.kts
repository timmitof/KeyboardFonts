plugins { id("keyboardfonts.data") }

dependencies {
    implementation(libs.mlkit.translate)
    // Определение языка текста: модель встроена в библиотеку (~1 МБ), работает без сети.
    implementation(libs.mlkit.language.id)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
}
