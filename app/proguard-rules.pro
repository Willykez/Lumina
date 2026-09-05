# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/android-sdk/tools/proguard/proguard-android-optimize.txt

# Jetpack Compose, Coroutines, and Coil ship their own consumer-proguard
# rules bundled in their AARs, so no extra keep rules are required for
# them under normal use of this app.

# Uncomment if you add reflection-based libraries (e.g. Retrofit, Gson,
# Moshi) later and see runtime crashes only in release builds:
# -keepattributes Signature
# -keepattributes *Annotation*

# Keep line numbers for readable release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
