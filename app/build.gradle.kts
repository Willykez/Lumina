plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.willykez.lumina"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.willykez.lumina"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // -------------------------------------------------------
    // Release signing
    //
    // Supplied by GitHub Actions:
    //
    // REPOMASTER_RELEASE_STORE_FILE
    // REPOMASTER_RELEASE_STORE_PASSWORD
    // REPOMASTER_RELEASE_KEY_ALIAS
    // REPOMASTER_RELEASE_KEY_PASSWORD
    // -------------------------------------------------------

    val releaseStoreFile =
        findProperty("REPOMASTER_RELEASE_STORE_FILE") as String?

    val releaseStorePassword =
        findProperty("REPOMASTER_RELEASE_STORE_PASSWORD") as String?

    val releaseKeyAlias =
        findProperty("REPOMASTER_RELEASE_KEY_ALIAS") as String?

    val releaseKeyPassword =
        findProperty("REPOMASTER_RELEASE_KEY_PASSWORD") as String?

    val hasReleaseSigning =
        !releaseStoreFile.isNullOrBlank() &&
            !releaseStorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Never use debug signing for release.
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // Fast, unminified iterative builds — no signing config needed,
            // Android Gradle Plugin uses the auto-generated debug keystore.
        }
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
