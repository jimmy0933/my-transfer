#!/bin/bash
mkdir -p CameraTranslator
cd CameraTranslator

# Root files
cat << 'EOF2' > settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "CameraTranslator"
include(":app")
EOF2

cat << 'EOF2' > build.gradle.kts
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}
EOF2

cat << 'EOF2' > gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
EOF2

mkdir -p app/src/main/java/com/example/cameratranslator
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/test/java/com/example/cameratranslator
mkdir -p app/src/androidTest/java/com/example/cameratranslator

cat << 'EOF2' > app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.cameratranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cameratranslator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // CameraX
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:\$camerax_version")
    implementation("androidx.camera:camera-camera2:\$camerax_version")
    implementation("androidx.camera:camera-lifecycle:\$camerax_version")
    implementation("androidx.camera:camera-video:\$camerax_version")
    implementation("androidx.camera:camera-view:\$camerax_version")
    implementation("androidx.camera:camera-extensions:\$camerax_version")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-japanese:16.0.0")

    // ML Kit Translation
    implementation("com.google.mlkit:translate:17.0.2")

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:\$room_version")
    kapt("androidx.room:room-compiler:\$room_version")
    implementation("androidx.room:room-ktx:\$room_version")

    // Tesseract (tess-two or similar)
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.5.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
EOF2

cat << 'EOF2' > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature
        android:name="android.hardware.camera"
        android:required="false" />

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".CameraTranslatorApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CameraTranslator"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.CameraTranslator">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <meta-data
            android:name="com.google.mlkit.vision.DEPENDENCIES"
            android:value="ocr,ocr_japanese" />
    </application>

</manifest>
EOF2

cat << 'EOF2' > app/src/main/res/values/strings.xml
<resources>
    <string name="app_name">即視翻譯</string>
</resources>
EOF2

cat << 'EOF2' > app/src/main/res/values/themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.CameraTranslator" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
EOF2

mkdir -p app/src/main/res/xml
cat << 'EOF2' > app/src/main/res/xml/backup_rules.xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content xmlns:android="http://schemas.android.com/apk/res/android">
    <include domain="sharedpref" path="."/>
</full-backup-content>
EOF2

cat << 'EOF2' > app/src/main/res/xml/data_extraction_rules.xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="."/>
    </cloud-backup>
</data-extraction-rules>
EOF2

cat << 'EOF2' > app/src/main/java/com/example/cameratranslator/CameraTranslatorApp.kt
package com.example.cameratranslator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CameraTranslatorApp : Application()
EOF2

cat << 'EOF2' > app/src/main/java/com/example/cameratranslator/MainActivity.kt
package com.example.cameratranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
EOF2

# Setup basic Gradle wrapper
gradle wrapper --gradle-version 8.8

echo "Project structure created"
