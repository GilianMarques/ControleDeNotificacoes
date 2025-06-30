// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.0")
    }
}

plugins {

    //noinspection GradleDependency
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.android.application") version "8.11.0" apply false

    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("androidx.room") version "2.7.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false

}