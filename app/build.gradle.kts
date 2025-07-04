plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlinx-serialization") // ✅ Add this

}

android {
    namespace = "com.humblecoders.teachersapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.humblecoders.teachersapp"
        minSdk = 24
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
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Firebase Firestore
    implementation(libs.firebase.firestore.ktx)

// Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation ("androidx.compose.material:material-icons-extended:1.5.4")

    // DataStore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

// Coroutines
    implementation (libs.kotlinx.coroutines.android)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")


    implementation ("androidx.navigation:navigation-compose:2.7.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")


}