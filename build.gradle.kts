buildscript {
    dependencies {
        //classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.gms:google-services:4.4.2")

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    //id("com.android.library") version "7.1.3" apply false

    // Add the dependency for the Crashlytics Gradle plugin
    //id("com.google.firebase.crashlytics") version "3.0.1" apply false
}