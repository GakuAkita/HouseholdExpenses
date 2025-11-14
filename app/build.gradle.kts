import java.util.Properties
import java.io.ByteArrayOutputStream

// Gitタグからバージョンを取得する関数
fun Project.getVersionName(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        val tag = stdout.toString().trim()
        if (tag.isNotEmpty() && tag.startsWith("v")) {
            tag.substring(1) // "v"を削除
        } else if (tag.isNotEmpty()) {
            tag
        } else {
            "1.0.0" // デフォルト値
        }
    } catch (e: Exception) {
        "1.0.0" // エラー時はデフォルト値
    }
}

// セマンティックバージョン（例：1.0.0）をバージョンコード（整数）に変換
// 各3桁で表現: major * 1000000 + minor * 1000 + patch
fun getVersionCode(versionName: String): Int {
    val parts = versionName.split(".")
    return try {
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        major * 1000000 + minor * 1000 + patch
    } catch (e: Exception) {
        1 // エラー時はデフォルト値
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")//version宣言しなくて大丈夫かな。kotlin("kapt") version "2.0.21"だとエラーでからこの書き方だけど。
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "gaku.original.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "gaku.original.myapplication"
        minSdk = 31//ここを上げる。上げないとkizitonwoseが使いづらくなる
        
        // Gitタグからバージョンを取得
        val versionNameFromGit = project.getVersionName()
        versionName = versionNameFromGit
        versionCode = getVersionCode(versionNameFromGit)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                localProperties.load(stream)
            }
        }

        //環境変数をBuildConfigに設定する
        buildConfigField("String", "WEB_CLIENT_ID", "\"${localProperties["WEB_CLIENT_ID"]}\"")
        buildConfigField("String", "REDIRECT_URI", "\"${localProperties["REDIRECT_URI"]}\"")
        // Firebase Emulator設定
        // USE_FIREBASE_EMULATOR=true のときはエミュレータを使用、false または未設定のときは本番環境を使用
        val useEmulator = (localProperties["USE_FIREBASE_EMULATOR"] as String?)?.toBoolean() ?: true
        buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", useEmulator.toString())
        
        // Firebase Emulator Host設定（Androidエミュレータ: 10.0.2.2, 実機: PCのIPアドレス）
        val emulatorHost = localProperties["FIREBASE_EMULATOR_HOST"] as String? ?: "10.0.2.2"
        buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"$emulatorHost\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    /* これがないとjava.lang.SecurityException: Unknown calling package name 'com.google.android.gms'というエラーが出る */
    /* ビルドで失敗するから一旦無視 */
//    implementation("com.google.android.gms:play-services:17.0.0")

    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)

    implementation(platform("androidx.compose:compose-bom:2025.01.01"))

    val calendar_version = "2.6.0"
    val scrollbar_version = "2.2.0"
    val viewModel_version = "2.8.5"
    val nav_version = "2.7.5"
    val room_version = "2.6.1"
    val hilt_version = "2.51.1"
    val gson_version = "2.10.1"

    //外部のライブラリいただく
    // The compose calendar library for Android
    implementation("com.kizitonwose.calendar:compose:${calendar_version}")

    //LazyColumnのスクロールバーのため
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:${scrollbar_version}")

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${viewModel_version}")

    //Navigation
    implementation("androidx.navigation:navigation-compose:$nav_version")

    //Splash-screen用
    implementation("androidx.core:core-splashscreen:1.0.1")

    //nestedClassを使うため
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //Gson
    implementation("com.google.code.gson:gson:${gson_version}")

    /* coil-compose paypayの画像共有で表示するために使用 */
    implementation(libs.coil.kt.coil.compose)

    /**************************Room DB用************************************/
    // Roomのコアライブラリ
    implementation("androidx.room:room-runtime:$room_version")
    // Roomのアノテーションプロセッサ
    kapt("androidx.room:room-compiler:$room_version")
    // Roomのコルーチンサポート（必要に応じて）
    implementation("androidx.room:room-ktx:$room_version")

    /************************** firebase ***********************************/
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    /************************** Dagger-hilt ***********************************/
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")//hiltViewModelを使うために必要

    //OpenInNewってアイコンがこれを追加しないと使えない？
    implementation("androidx.compose.material:material-icons-extended")

    /************************** OCR 関連 ***********************************/
    implementation(libs.text.recognition)
    // To recognize Japanese script
    implementation(libs.text.recognition.japanese)

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
    implementation(kotlin("reflect"))
}

kapt {
    javacOptions {
        option("-Adagger.hilt.android.internal.disableAndroidSuperclassValidation=true")
    }
}