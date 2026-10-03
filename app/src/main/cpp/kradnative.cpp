// Krad Weather native hisoblashlar (C++).
// Og'ir sikllar UI oqimidan tashqarida, -O2 bilan yig'iladi.
// Barcha funksiyalar Kotlin fallback'ga ega — kutubxona yuklanmasa ham ilova ishlaydi.

#include <jni.h>
#include <cmath>
#include <string>

// Ob-havo bayroqlari (SmartAlertAnalyzer bilan bir xil qiymatlar)
constexpr jint FLAG_HEAT = 1;        // 2^0
constexpr jint FLAG_COLD = 2;        // 2^1
constexpr jint FLAG_RAIN = 4;        // 2^2
constexpr jint FLAG_SNOW = 8;        // 2^3
constexpr jint FLAG_STORM = 16;      // 2^4
constexpr jint FLAG_WIND = 32;       // 2^5
constexpr jint FLAG_FOG = 64;        // 2^6
constexpr jint FLAG_TEMP_DROP = 128; // 2^7
constexpr jint FLAG_GOOD = 256;      // 2^8

extern "C" {

JNIEXPORT jint JNICALL
Java_com_krad_weather_util_NativeWeather_analyzeWeatherNative(
        JNIEnv *env, jobject /* this */,
        jdouble temp, jdouble feelsLike, jdouble windSpeed,
        jdouble precipMm, jint visibility, jdouble maxPop,
        jint conditionId, jdouble tempChange6h) {
    jint flags = 0;
    if (temp >= 35.0 || feelsLike >= 38.0) flags |= FLAG_HEAT;
    if (temp <= -10.0 || feelsLike <= -15.0) flags |= FLAG_COLD;
    if (precipMm >= 0.5 || maxPop >= 0.7) flags |= FLAG_RAIN;
    if (conditionId >= 600 && conditionId < 623) flags |= FLAG_SNOW;
    if (conditionId >= 200 && conditionId < 233) flags |= FLAG_STORM;
    if (windSpeed >= 12.0) flags |= FLAG_WIND;
    if (visibility > 0 && visibility <= 2000) flags |= FLAG_FOG;
    if (tempChange6h <= -8.0) flags |= FLAG_TEMP_DROP;
    if (flags == 0 && maxPop < 0.2 && windSpeed < 8.0 &&
        temp > 15.0 && temp < 30.0) {
        flags |= FLAG_GOOD;
    }
    return flags;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_krad_weather_util_NativeWeather_smoothTempsNative(
        JNIEnv *env, jobject /* this */,
        jdoubleArray temps) {
    jsize n = env->GetArrayLength(temps);
    jdouble *src = env->GetDoubleArrayElements(temps, nullptr);
    jdoubleArray out = env->NewDoubleArray(n);
    if (src == nullptr || out == nullptr) {
        if (src != nullptr) env->ReleaseDoubleArrayElements(temps, src, JNI_ABORT);
        return out;
    }
    // Markaziy sirpanuvchi o'rtacha (oyna=3) — grafik chizig'i silliq bo'ladi
    for (jsize i = 0; i < n; i++) {
        double sum = src[i];
        double cnt = 1.0;
        if (i > 0) {
            sum += src[i - 1];
            cnt += 1.0;
        }
        if (i + 1 < n) {
            sum += src[i + 1];
            cnt += 1.0;
        }
        double v = sum / cnt;
        env->SetDoubleArrayRegion(out, i, 1, &v);
    }
    env->ReleaseDoubleArrayElements(temps, src, JNI_ABORT);
    return out;
}

JNIEXPORT jdouble JNICALL
Java_com_krad_weather_util_NativeWeather_dewPointNative(
        JNIEnv *env, jobject /* this */,
        jdouble tempC, jdouble humidity) {
    // Magnus formulasi (Selsiy kirish/chiqish)
    double rh = humidity;
    if (rh < 1.0) rh = 1.0;
    if (rh > 100.0) rh = 100.0;
    const double a = 17.27, b = 237.7;
    double alpha = (a * tempC) / (b + tempC) + log(rh / 100.0);
    return (b * alpha) / (a - alpha);
}

static double epaIndex(double conc,
                       const double *cLo, const double *cHi,
                       const double *iLo, const double *iHi, int n) {
    for (int i = 0; i < n; i++) {
        if (conc <= cHi[i]) {
            return (iHi[i] - iLo[i]) / (cHi[i] - cLo[i]) * (conc - cLo[i]) + iLo[i];
        }
    }
    return 500.0;
}

JNIEXPORT jint JNICALL
Java_com_krad_weather_util_NativeWeather_epaAqiNative(
        JNIEnv *env, jobject /* this */,
        jdouble pm25, jdouble pm10) {
    // US EPA AQI (PM2.5 va PM10 dan kattasi)
    const double c25[] = {0.0, 12.1, 35.5, 55.5, 150.5, 250.5};
    const double c25h[] = {12.0, 35.4, 55.4, 150.4, 250.4, 500.0};
    const double c10[] = {0.0, 55.0, 155.0, 255.0, 355.0, 425.0};
    const double c10h[] = {54.0, 154.0, 254.0, 354.0, 424.0, 604.0};
    const double ilo[] = {0.0, 51.0, 101.0, 151.0, 201.0, 301.0};
    const double ihi[] = {50.0, 100.0, 150.0, 200.0, 300.0, 500.0};
    double a25 = pm25 < 0.0 ? -1.0
        : epaIndex(pm25, c25, c25h, ilo, ihi, 6);
    double a10 = pm10 < 0.0 ? -1.0
        : epaIndex(pm10, c10, c10h, ilo, ihi, 6);
    double m = a25 > a10 ? a25 : a10;
    if (m < 0.0) return -1;
    return (jint) llround(m);
}

JNIEXPORT jdouble JNICALL
Java_com_krad_weather_util_NativeWeather_moonPhaseNative(
        JNIEnv *env, jobject /* this */,
        jlong epochMillis) {
    // 2000-01-06 18:14 UTC dagi yangi oydan boshlab sinodik davr
    const double newMoonMs = 947182440000.0;
    const double synodicDays = 29.53058867;
    double days = (epochMillis - newMoonMs) / 86400000.0;
    double phase = fmod(days / synodicDays, 1.0);
    if (phase < 0.0) phase += 1.0;
    return phase; // 0=yangi oy, 0.25=birinchi chorak, 0.5=to'lin oy
}

JNIEXPORT jdouble JNICALL
Java_com_krad_weather_util_NativeWeather_moonIlluminationNative(
        JNIEnv *env, jobject /* this */,
        jlong epochMillis) {
    const double newMoonMs = 947182440000.0;
    const double synodicDays = 29.53058867;
    double days = (epochMillis - newMoonMs) / 86400000.0;
    double phase = fmod(days / synodicDays, 1.0);
    if (phase < 0.0) phase += 1.0;
    const double pi = 3.141592653589793;
    return (1.0 - cos(2.0 * pi * phase)) / 2.0;
}

JNIEXPORT jint JNICALL
Java_com_krad_weather_util_NativeWeather_scoreCityNative(
        JNIEnv *env, jobject /* this */,
        jstring query, jstring name) {
    if (query == nullptr || name == nullptr) return 0;
    const char *q = env->GetStringUTFChars(query, nullptr);
    const char *n = env->GetStringUTFChars(name, nullptr);
    if (q == nullptr || n == nullptr) {
        if (q != nullptr) env->ReleaseStringUTFChars(query, q);
        if (n != nullptr) env->ReleaseStringUTFChars(name, n);
        return 0;
    }
    std::string qs(q), ns(n);
    env->ReleaseStringUTFChars(query, q);
    env->ReleaseStringUTFChars(name, n);
    if (qs.empty() || ns.empty()) return 0;
    if (ns == qs) return 100;
    if (ns.rfind(qs, 0) == 0) return 80;
    // So'z boshidan moslik
    bool wordStart = true;
    for (size_t i = 0; i < ns.size(); i++) {
        char c = ns[i];
        if (c == ' ' || c == '-' || c == '.') {
            wordStart = true;
            continue;
        }
        if (wordStart && ns.compare(i, qs.size(), qs) == 0) return 70;
        wordStart = false;
    }
    if (ns.find(qs) != std::string::npos) return 60;
    return 0;
}

JNIEXPORT jdouble JNICALL
Java_com_krad_weather_util_NativeWeather_heatIndexNative(
        JNIEnv *env, jobject /* this */,
        jdouble tempC, jdouble humidity) {
    // Rothfusz regressiyasi (Selsiy kirish/chiqish)
    double tF = tempC * 9.0 / 5.0 + 32.0;
    double rh = humidity;
    if (tF < 80.0) return tempC;
    double hi = -42.379 + 2.04901523 * tF + 10.14333127 * rh
                - 0.22475541 * tF * rh - 0.00683783 * tF * tF
                - 0.05481717 * rh * rh + 0.00122874 * tF * tF * rh
                + 0.00085282 * tF * rh * rh - 0.00000199 * tF * tF * rh * rh;
    return (hi - 32.0) * 5.0 / 9.0;
}

} // extern "C"
