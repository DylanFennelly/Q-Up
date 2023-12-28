// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

//https://developers.google.com/maps/documentation/places/android-sdk/secrets-gradle-plugin#kotlin
//assists with using values placed inside local.properties (api key)

buildscript{
    dependencies{
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}