import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

fun generateVersionName(): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd.HH.mm")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}

fun generateVersionCode(): Int {
    val formatter = SimpleDateFormat("yyMMddHH")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date()).toIntOrDefault(1)
}

fun String.toIntOrDefault(default: Int): Int {
    return try {
        this.toInt()
    } catch (e: Exception) {
        default
    }
}

android {
    namespace = "bangla.English.bekaron"
    compileSdk = 34

    defaultConfig {
        applicationId = "bangla.English.bekaron"
        minSdk = 24
        targetSdk = 34
        versionCode = generateVersionCode()
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INDEX/*"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ONNX Runtime Android
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
