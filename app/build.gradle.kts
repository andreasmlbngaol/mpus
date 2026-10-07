plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.google.services)
}

// Nothing is hardcoded: build config comes from the environment. Gradle does not read
// .env on its own, so load a local .env if it exists (handy without direnv), and let real
// environment variables win over it.
val dotenv: Map<String, String> = rootProject.file(".env").let { file ->
    if (!file.exists()) {
        emptyMap()
    } else {
        file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .associate { line ->
                line.substringBefore("=").trim() to line.substringAfter("=").trim().trim('"')
            }
    }
}

fun env(name: String, default: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: dotenv[name]?.takeIf { it.isNotBlank() }
        ?: default

fun envOrNull(name: String): String? =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: dotenv[name]?.takeIf { it.isNotBlank() }

// Escape a value into a Java/Kotlin string literal for buildConfigField.
fun quoted(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val apiBaseUrl = env("API_BASE_URL", "https://mpus.booroong.online")

// Release signing is optional locally and supplied from CI secrets on a tag build.
val keystoreFile = envOrNull("MPUS_KEYSTORE_FILE")?.let(::File)
val hasSigning = keystoreFile != null && keystoreFile.exists()

android {
    namespace = "id.andreasmlbngaol.mpus"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "id.andreasmlbngaol.mpus"
        minSdk = 29
        targetSdk = 37
        // A release tag must be "v$versionName"; the release workflow enforces it.
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", quoted(apiBaseUrl))
    }

    signingConfigs {
        if (hasSigning) {
            create("release") {
                storeFile = keystoreFile
                storePassword = envOrNull("MPUS_KEYSTORE_PASSWORD")
                keyAlias = envOrNull("MPUS_KEY_ALIAS")
                keyPassword = envOrNull("MPUS_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.osmdroid.android)
    implementation(libs.smooth.corner.rect.android.compose)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.android)
    implementation(libs.koin.core.viewmodel)
    implementation(libs.koin.androidx.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
