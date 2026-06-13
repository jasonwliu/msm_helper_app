plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  id("maven-publish")
}

android {
    namespace = "com.example.msmhelper"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.msmhelper"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
      buildConfig = false
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

  // Core Android dependencies
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
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // Serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

  // Icons
  implementation("androidx.compose.material:material-icons-extended")

  // Google Sign-In & Google Drive API Client
  implementation("com.google.android.gms:play-services-auth:20.7.0")
  implementation("com.google.api-client:google-api-client-android:2.2.0") {
      exclude(group = "org.apache.httpcomponents", module = "httpclient")
  }
  implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0") {
      exclude(group = "org.apache.httpcomponents", module = "httpclient")
  }
  implementation("com.google.http-client:google-http-client-gson:1.43.3") {
      exclude(group = "org.apache.httpcomponents", module = "httpclient")
  }
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            groupId = "com.example.msmhelper"
            artifactId = "msmhelper"
            version = "1.0.0"

            artifact(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")) {
                classifier = "debug"
                extension = "apk"
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY")}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
