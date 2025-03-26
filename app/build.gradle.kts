plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.voithos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.voithos"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {

    implementation(platform("androidx.compose:compose-bom:2024.04.00"))  // BOM
    implementation("androidx.compose.ui:ui")               // No version!
    implementation("androidx.compose.material3:material3")   // No version!
    implementation("androidx.compose.ui:ui-tooling-preview")  // No version!
    implementation("androidx.activity:activity-compose")     // No version!
    debugImplementation("androidx.compose.ui:ui-tooling")    // No version!

    // Other dependencies (POI, PDFBox, etc.) go *AFTER* Compose dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.xmlbeans:xmlbeans:5.0.0")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("commons-codec:commons-codec:1.15")
    // Existing dependencies...

    // JUnit 4
    testImplementation("junit:junit:4.13.2")

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Optional -- UI testing with Compose
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")  // For debug builds
}
