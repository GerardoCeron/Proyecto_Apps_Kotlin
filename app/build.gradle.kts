plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
}

android {

    namespace = "com.doctranslate"

    compileSdk = 35

    defaultConfig {

        applicationId = "com.doctranslate"

        minSdk = 24

        targetSdk = 35

        versionCode = 1

        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(platform(libs.androidx.compose.bom))

    implementation("androidx.compose.ui:ui")

    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.okhttp.logging)

    implementation(libs.pdfbox.android)

    implementation(libs.mlkit.text.recognition)
    
    implementation(libs.mlkit.translate)
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
