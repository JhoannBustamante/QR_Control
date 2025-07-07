plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Apply the Google Services and Crashlytics plugins
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    id("com.google.firebase.firebase-perf")

}

android {
    namespace = "com.espoch.qrcontrol"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.espoch.qrcontrol"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    //
    implementation(libs.gson)
    // Navigation dependencies
    implementation(libs.androidx.navigation.compose)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.storage.ktx)



//    implementation(libs.play.services.auth)
    implementation(libs.google.auth)
    // QR Code dependencies
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

    // Cámara y ML Kit dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.barcode.scanning)

    implementation(libs.androidx.camera.view)

    // Coroutines dependencies
    implementation(libs.kotlinx.coroutines.android)

    // Utilidades dependencies
    implementation(libs.coil.compose)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Permisos Compose dependencies
    implementation(libs.accompanist.permissions)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
}