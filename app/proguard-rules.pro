# Add project specific ProGuard rules here.
# Obfuscation and Security configuration for SARIB TV

-repackageclasses ''
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Optimization passes
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Keep essential Android & Jetpack Compose components
-keep class androidx.compose.** { *; }
-keep class androidx.media3.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.MainActivity { *; }
-keep class com.example.SaribTvApplication { *; }
-keep class com.google.firebase.** { *; }
