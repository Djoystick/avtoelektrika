plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
  alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.autoelectricai"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.autoelectricai"
        minSdk = 24
        targetSdk = 36
        versionCode = 48
        versionName = "1.7.14"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.icons.extended)
  implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.1")

  // Splash Screen
  implementation(libs.splashscreen)

  // Navigation
  implementation(libs.androidx.navigation.compose)
  implementation(libs.hilt.navigation.compose)

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // Hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  // Retrofit + OkHttp
  implementation(libs.retrofit)
  implementation(libs.retrofit.gson)
  implementation(libs.okhttp.logging)

  // Gson
  implementation(libs.gson)

  // Jsoup (парсинг веб-страниц)
  implementation(libs.jsoup)

  // DataStore (хранение настроек: API ключи)
  implementation(libs.datastore.preferences)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Firebase
  val firebaseBom = platform(libs.firebase.bom)
  implementation(firebaseBom)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.storage)

  // Auth / Credentials
  implementation("androidx.credentials:credentials:1.3.0-rc01")
  implementation("androidx.credentials:credentials-play-services-auth:1.3.0-rc01")
  implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

  // WorkManager
  implementation(libs.work.runtime)
  implementation(libs.hilt.work)
  ksp(libs.hilt.work.compiler)

  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
