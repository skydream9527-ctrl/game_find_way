# Add project specific ProGuard rules here.
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep class com.gameway.** { *; }
-keepclassmembers class * {
    @kotlin.Metadata <methods>;
}