# 🚍 SITREK – Sistem Tracking Kendaraan Umum Real-Time

![Platform](https://img.shields.io/badge/platform-android-blue.svg)
![Firebase](https://img.shields.io/badge/backend-Firebase-orange)
![Status](https://img.shields.io/badge/status-selesai-blue)

SITREK adalah aplikasi Android yang memungkinkan pelacakan **kendaraan umum secara real-time**, menggunakan **Firebase Realtime Database** dan metodologi **ICONIX Process** dalam pengembangannya. Aplikasi ini dirancang untuk membantu **pengguna (penunggu)** dalam melihat posisi kendaraan dan **penyedia lokasi** untuk membagikan lokasi kendaraan yang mereka tumpangi.

---

## ✨ Fitur Utama

- 🔍 **Mode Penunggu:** Melihat lokasi kendaraan secara langsung di peta.
- 📤 **Mode Penyedia:** Mengirim data lokasi kendaraan ke Firebase secara berkala.
- 🗳️ **Resolusi Konflik Lokasi:** Sistem voting mayoritas + prioritas pengirim pertama.
- 📍 **Google Maps Integration:** Visualisasi posisi kendaraan secara real-time.
- 🛡️ **Manajemen Izin Lokasi** sesuai standar Android 11+.

---

## 📱 Tampilan Antarmuka

| Splash | Pilih Mode | Pilih Transportasi |
|--------|------------|--------------------|
| ![splash](screenshots/splash.png) | ![mode](screenshots/mode.png) | ![pilih](screenshots/pilih.png) |

---

## ⚙️ Teknologi yang Digunakan

- Kotlin (Android Studio)
- Firebase Realtime Database
- Google Maps API
- ICONIX Process (UML: Use Case, Class, Sequence Diagram)
- MVVM Pattern (Modularized)
- Fused Location Provider (LocationServices)

---

## 🚀 Cara Menjalankan Aplikasi

1. Clone repository ini:
    ```bash
    git clone https://github.com/username/SITREK.git
    ```

2. Buka folder di **Android Studio**.

3. Aktifkan Google Maps API dan Firebase:
    - Tambahkan `google-services.json` ke dalam folder `app/`
    - Konfigurasikan Firebase Realtime Database rules (untuk testing):

      ```json
      {
        "rules": {
          ".read": true,
          ".write": true
        }
      }
      ```

4. Jalankan aplikasi di emulator atau perangkat fisik.

---

## 📁 Struktur Folder

```bash
SITREK/
├── app/
│   ├── data/          # Data models: Lokasi, Transportasi, DataLokasi
│   ├── handler/       # Helper logic: izin lokasi, simpan pilihan
│   ├── service/       # LocationUpdateService (foreground)
│   ├── ui/            # Seluruh Activity dan Adapter UI
│   └── utils/         # FirebaseManager, ProximityChecker
