# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entity and DAO classes (annotations stripped by R8 otherwise)
-keep class com.mealcycle.app.data.model.** { *; }
-keep class com.mealcycle.app.data.db.** { *; }

# Keep Kotlin metadata – required by Hilt and kotlinx.coroutines
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes Kotlin*

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class **_HiltComponents* { *; }
-keep class **_HiltModules* { *; }

# Keep DataStore / Protobuf internal classes
-keep class androidx.datastore.** { *; }

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.debug.**

# Coil — keep transformation & fetcher internals
-keep class coil.** { *; }
-dontwarn okhttp3.**

# Suppress warnings from Google Fonts provider
-dontwarn com.google.android.gms.fonts.**

# Keep line numbers in stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

