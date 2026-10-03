# Krad Weather v3.1 ⛅

**Krad Weather** — Android uchun premium darajadagi ob-havo ilovasi. Ikkita API birlashgan: **OpenWeatherMap** (asosiy) + **WeatherAPI.com** (UV, rasmiy ogohlantirishlar, zaxira).

## ✨ Imkoniyatlar

- 🌤️ **Joriy ob-havo** — harorat, his qilinishi, min/max, namlik, bosim, shamol, ko'rinish, bulutlilik
- ⏱️ **Soatlik prognoz** — batafsil dialog bilan (bossangiz ochiladi)
- 📅 **5 kunlik prognoz** — kunlik dialog bilan
- 📊 **Harorat grafigi** — C++ engine silliqlaydi
- 🧭 **Shamol kompasi** — animatsiyali yo'nalish + kuchayish
- 🌫️ **Havo sifati (AQI)** — OWM indeksi + US EPA
- ☀️ **UV indeksi** — WeatherAPI.com dan
- ⚠️ **Ogohlantirish banneri** — smart tahlil + rasmiy ogohlantirishlar
- 🌧️ **Yog'ingarchilik** — mm da + ehtimol %
- 👗 **«Bugun nima kiyaman?»** — maslahat kartasi
- 🔄 **Pastga tortib yangilash** + kesh belgisi + yangilangan vaqti
- ⭐ **Sevimlilar** — jonli harorat bilan
- 🕘 **Qidiruv tarixi** — reytingli qidiruv (C++ scoring)
- 🧭 **Bottom-navigatsiya / yon rail** — planshet va landshaftda
- 👋 **Onboarding** — birinchi ochilishda
- 🎨 **Mavzu** — Avto / Ochiq / Tungi + dinamik fon
- 🔔 **Smart bildirishnomalar** — holatga mos matnlar, ertalab dayjest
- 📱 **Vidjet** — 4 qavatli avto-yangilanish
- 🌍 **3 til** — uz / ru / en (darhol almashadi, tavsiflar tarjima qilinadi)
- 🛡️ **Aniq xatoliklar** — 3 tilda, retry tizimi bilan
- 🧪 **28 unit test** — yashil

## 🛠 Texnologiyalar

- **Kotlin 2.0** + **Jetpack Compose** (Material 3)
- **Hilt** (+ hilt-work) — DI
- **Retrofit + OkHttp + Gson** — 4 ta API manbasi
- **Room v3** — favoritlar + kesh + qidiruv tarixi
- **DataStore** — sozlamalar
- **WorkManager** — fon + bildirishnoma + vidjet
- **AppWidget** — klassik vidjet
- **NDK (C++)** — `kradnative`: tahlil, AQI, oy fazasi, scoring (fallback bilan)
- **Navigation Compose, Play Location, Accompanist Permissions**
- **JUnit4** — unit testlar

## 🚀 O'rnatish

1. Android Studio (Ladybug+) → **Open** → shu papka.
2. `local.properties.example` dan nusxa oling:
   ```
   copy local.properties.example local.properties   (Windows)
   cp local.properties.example local.properties    (macOS/Linux)
   ```
   va o'z API kalitlaringizni yozing (`OPEN_WEATHER_API_KEY`, `WEATHERAPI_KEY`).
3. Gradle sync → **Run**. Yoki terminalda:
   ```
   ./gradlew :app:assembleDebug
   ```

## 🧪 Test

```
./gradlew :app:testDebugUnitTest
```

## 👨‍💻 Dasturchi

**machine_dev** — https://t.me/machine_dev

## Litsenziya

MIT — [LICENSE](LICENSE)
