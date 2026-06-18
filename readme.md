# MusicPlayerFX

MusicPlayerFX merupakan aplikasi pemutar musik berbasis Java yang dirancang dengan antarmuka pengguna (UI) bertema *Dark Mode* (Zinc & Violet). Aplikasi ini dilengkapi dengan fitur *Audio Visualizer* yang merepresentasikan spektrum audio secara *real-time* selaras dengan irama musik yang sedang diputar.

## Cara Menjalankan Program

Aplikasi ini dapat dijalankan melalui dua metode eksekusi sebagai berikut:

1. **Eksekusi melalui `Start.vbs`** (Direkomendasikan)
   Metode ini akan menjalankan aplikasi sepenuhnya di latar belakang (*background process*) dan menampilkan antarmuka *Setup Menu* (Auto-Installer) tanpa memunculkan jendela *Command Prompt*.
2. **Eksekusi melalui `run.bat`**
   Script ini akan menginisiasi proses eksekusi dan secara otomatis mengalihkan perintah ke file `Start.vbs`.

Kedua metode di atas akan memunculkan antarmuka *launcher* secara tertata sebelum aplikasi utama dimuat.

## Keunggulan Utama

- **Dukungan Multi-Ekstensi (.mp3 & .mov)**: Selain mendukung format audio standar seperti `.mp3` dan `.wav`, instrumen JavaFX yang tertanam memiliki kemampuan untuk mengekstraksi dan memutar *audio stream* dari file media kompleks seperti **`.mov`**, `.m4a`, dan `.aac`.
- **Auto-Installer Independen**: Aplikasi ini memiliki mekanisme pengunduhan dependensi secara mandiri. Apabila *library* yang dibutuhkan tidak ditemukan pada sistem, aplikasi akan meminta konfirmasi pengguna untuk **mengunduh dan mengekstraknya secara otomatis** tanpa memerlukan perangkat lunak tambahan.
- **In-App Debug Terminal**: Tersedia fitur terminal *debug* internal yang dapat diakses melalui tombol **"🛠 Debug"**. Fitur ini berfungsi untuk memantau log sistem dan merekam informasi teknis tanpa mengganggu antarmuka utama.

## Library dan Teknologi yang Digunakan

Aplikasi ini dikembangkan menggunakan bahasa pemrograman Java dengan integrasi beberapa *library* utama:

1. **JavaFX (OpenJFX)**
   Digunakan sebagai *library* utama untuk pembentukan antarmuka grafis (GUI). Modul spesifik yang diimplementasikan meliputi:
   - `javafx.controls`: Berfungsi untuk pengelolaan elemen visual interaktif (seperti tombol dan *slider*).
   - `javafx.media`: Berperan sebagai pemutar media berkinerja tinggi yang bertugas menyediakan data *Audio Spectrum* secara *real-time*.
2. **JLayer (JavaZoom - jl1.0.1.jar)**
   Diimplementasikan sebagai *Fallback Engine*. Apabila sistem operasi pengguna tidak mendukung *codec* JavaFX Media, sistem akan secara otomatis memindahkan proses pemutaran berkas `.mp3` kepada JLayer.
3. **Java Swing & AWT (`javax.swing`, `java.awt`)**
   Diaplikasikan pada file `Launcher.java` untuk merender menu *Splash Screen* dan proses *Auto-Installer* sebelum modul JavaFX dimuat ke dalam memori aplikasi.
4. **Java Sound API (`javax.sound.sampled`)**
   Merupakan *library* bawaan JDK yang difungsikan sebagai mesin *fallback* murni untuk mengeksekusi format audio *lossless* seperti `.wav`.
