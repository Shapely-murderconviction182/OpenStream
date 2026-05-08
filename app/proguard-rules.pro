-keep class com.openstream.app.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}
-dontwarn kotlinx.coroutines.**
