plugins { id("keyboardfonts.data") }

dependencies {
    implementation(libs.mlkit.translate)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
}
