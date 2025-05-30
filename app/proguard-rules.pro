# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# make sure to keep the dummy-classes for the activity-aliases
-keep class ch.abwesend.privatecontacts.infrastructure.launcher.MainActivityAliasDefaultIcon { *; }
-keep class ch.abwesend.privatecontacts.infrastructure.launcher.MainActivityAliasCalculatorIcon { *; }

# Keep all classes and resources from ezvcard: the library uses reflection in some cases
# re-insterting those creates strange warnings => trying to go without, first
#-keep class ezvcard.** { *; }
#-keep interface ezvcard.** { *; }
#-keepclassmembers class ezvcard.** { *; }
-keepclassmembernames class ezvcard.** { *; }
