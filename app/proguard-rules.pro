-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses

# Gson DTOlar + keshlanadigan domen modellar (R8 field nomlarini o'zgartirmasin!)
-keep class com.krad.weather.data.remote.dto.** { *; }
-keep class com.krad.weather.data.local.** { *; }
-keep class com.krad.weather.domain.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room — generated impl'lar
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt / Dagger — generated kod
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.ComponentManager { *; }

# WorkManager — Worker'lar refleksiya orqali topiladi
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class androidx.hilt.work.HiltWorkerFactory { *; }
-keep class com.krad.weather.worker.WeatherWorker { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep interface retrofit2.** { *; }

# Coil olib tashlangan — qoidalar kerak emas
