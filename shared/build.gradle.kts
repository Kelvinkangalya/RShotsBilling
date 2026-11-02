import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.1.0"
}

kotlin {
    jvm() {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.androidx.lifecycle.runtime.compose)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.serialization.kotlinx.json)
                
                // GitLive Firebase in commonMain
                api(libs.firebase.gitlive.auth)
                api(libs.firebase.gitlive.firestore)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("com.itextpdf:itextg:5.5.10")
                implementation("org.apache.poi:poi-ooxml:5.2.3")
                implementation("org.apache.poi:poi:5.2.3")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("firebase", "11.1.0")) // Official Firebase JS SDK via npm
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation("com.itextpdf:itextpdf:5.5.13.3") // Standard iText for JVM
                implementation("org.apache.poi:poi-ooxml:5.2.3")
                implementation("org.apache.poi:poi:5.2.3")
            }
        }
    }
}

// Global exclusion to ensure NO Android components leak into JVM
project.configurations.all {
    if (name.contains("jvmRuntimeClasspath", ignoreCase = true) || name.contains("jvmCompileClasspath", ignoreCase = true)) {
        exclude(group = "androidx.test")
        exclude(group = "androidx.test.ext")
        exclude(group = "androidx.test.espresso")
    }
}

android {
    namespace = "com.example.rshotsbilling.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
