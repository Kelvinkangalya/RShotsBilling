plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
}

android {

    namespace = "com.example.rshotsbilling"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.rshotsbilling"

        minSdk = 26
        targetSdk = 36

        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {

        jvmTarget = "17"
    }

    buildFeatures {

        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":composeApp"))
    // =========================
    // Compose BOM
    // =========================
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.itextpdf:itextg:5.5.10")
    implementation(
        platform(
            "androidx.compose:compose-bom:2024.09.03"
        )
    )

    // =========================
    // Compose UI
    // =========================

    implementation(
        "androidx.activity:activity-compose:1.10.1"
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // =========================
    // Navigation
    // =========================

    implementation(
        "androidx.navigation:navigation-compose:2.8.9"
    )

    // =========================
    // Firebase BOM
    // =========================

    implementation(
        platform(
            "com.google.firebase:firebase-bom:33.10.0"
        )
    )
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.poi:poi:5.2.3")


    // =========================
    // Firebase Authentication
    // =========================

    implementation(
        "com.google.firebase:firebase-auth"
    )

    // =========================
    // Charts (Vico)
    // =========================
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.1")

    // =========================
    // Networking (M-Pesa API)
    // =========================
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // =========================
    // Room Database
    // =========================
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // =========================
    // WorkManager
    // =========================
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // =========================
    // Debug Tools
    // =========================

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )

    debugImplementation(
        "androidx.compose.ui:ui-test-manifest"
    )
}
