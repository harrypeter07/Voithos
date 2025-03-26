plugins {
    id("com.android.application") version "8.9.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false


}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
        classpath("com.google.gms:google-services:4.3.15")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
//plugins {
//    id("com.android.application") version "8.5.0" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
//}
//
//buildscript {
//    dependencies {
//        classpath("com.android.tools.build:gradle:8.5.0")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23")
//    }
//}