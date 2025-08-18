// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
    id("com.google.dagger.hilt.android") version "2.57" apply false

    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false // this version matches your Kotlin version

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.3" apply false

}

buildscript {
    dependencies {
        classpath("com.yanzhenjie.andserver:plugin:2.1.12")
    }
}