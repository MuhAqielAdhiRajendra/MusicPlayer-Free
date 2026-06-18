# MusicPlayerFX

🇬🇧 [**English Version**](#english-version) | 🇮🇩 [**Versi Bahasa Indonesia**](#versi-bahasa-indonesia)

---

## English Version

MusicPlayerFX is a Java-based music player application designed with a modern *Dark Mode* user interface (Zinc & Violet theme). This application is equipped with an *Audio Visualizer* feature that represents the audio spectrum in *real-time*, perfectly synchronized with the rhythm of the playing music.

### How to Run the Program

This application can be executed through the following two methods:

1. **Execution via `Start.vbs`** (Recommended)
   This method will run the application entirely in the background and display the *Setup Menu* (Auto-Installer) interface without prompting the *Command Prompt* window.
2. **Execution via `run.bat`**
   This script will initiate the execution process and automatically redirect the command to the `Start.vbs` file.

Both methods above will elegantly display the *launcher* interface before the main application is loaded into memory.

### Key Features

- **Integrated Direct Downloader**: This program is designed not only to play your locally saved songs but is also capable of directly downloading songs via a given link. The files are automatically downloaded and ready to be played within the program, eliminating the hassle of converting links using external third-party applications or websites.
- **Extensive Format Support (.mp3, .wav, .m4a, .aac, .aiff, .mov)**: In addition to supporting standard audio formats such as `.mp3` and `.wav`, the embedded JavaFX engine is fully capable of playing Advanced Audio Coding formats (`.m4a`, `.aac`), high-quality lossless `.aiff`.
- **Independent Auto-Installer**: This application has an autonomous dependency download mechanism. If the required *libraries* are not found on the local system, the application will prompt the user for confirmation to **automatically download and extract them** through the internet protocol without requiring any third-party software.
- **In-App Debug Terminal**: An internal *debug* terminal feature is available and can be accessed via the **"🛠 Debug"** button. This feature serves to monitor system logs and record technical information without disrupting the main user interface.

### Libraries and Technologies Used

This application was developed using the Java programming language with the integration of several primary *libraries*:

1. **JavaFX (OpenJFX)**
   Used as the primary *library* for constructing the graphical user interface (GUI). Specific modules implemented include:
   - `javafx.controls`: Functions for managing interactive visual elements (such as buttons and *sliders*).
   - `javafx.media`: Acts as a high-performance media player responsible for providing *Audio Spectrum* data in *real-time*.
2. **JLayer (JavaZoom - jl1.0.1.jar)**
   Implemented as a *Fallback Engine*. If the user's operating system does not support the JavaFX Media *codec*, the system will automatically transfer the `.mp3` file playback process to JLayer.
3. **Java Swing & AWT (`javax.swing`, `java.awt`)**
   Applied to the `Launcher.java` file to render the *Splash Screen* menu and the *Auto-Installer* process before the JavaFX module is loaded into the application memory.
4. **Java Sound API (`javax.sound.sampled`)**
   A built-in JDK *library* utilized as a pure *fallback* engine to execute *lossless* audio formats such as `.wav`.

---

## Versi Bahasa Indonesia

MusicPlayerFX merupakan aplikasi pemutar musik berbasis Java yang dirancang dengan antarmuka pengguna (UI) bertema *Dark Mode* (Zinc & Violet). Aplikasi ini dilengkapi dengan fitur *Audio Visualizer* yang merepresentasikan spektrum audio secara *real-time* selaras dengan irama musik yang sedang diputar.

### Cara Menjalankan Program

Aplikasi ini dapat dijalankan melalui dua metode eksekusi sebagai berikut:

1. **Eksekusi melalui `Start.vbs`** (Direkomendasikan)
   Metode ini akan menjalankan aplikasi sepenuhnya di latar belakang (*background process*) dan menampilkan antarmuka *Setup Menu* (Auto-Installer) tanpa memunculkan jendela *Command Prompt*.
2. **Eksekusi melalui `run.bat`**
   Script ini akan menginisiasi proses eksekusi dan secara otomatis mengalihkan perintah ke file `Start.vbs`.

Kedua metode di atas akan memunculkan antarmuka *launcher* secara tertata sebelum aplikasi utama dimuat.

### Keunggulan Utama

- **Integrated Direct Downloader**: Program ini tidak hanya dirancang untuk memutar lagu-lagu lokal yang sudah Anda simpan, tetapi juga mampu mengunduh lagu secara langsung melalui sebuah tautan (*link*). File akan otomatis terunduh dan langsung siap diputar di dalam program tanpa perlu repot mengonversi tautan tersebut melalui aplikasi atau situs web pihak ketiga.
- **Dukungan Format Luas (.mp3, .wav, .m4a, .aac, .aiff, .mov)**: Selain mendukung format audio standar seperti `.mp3` dan `.wav`, instrumen JavaFX yang tertanam secara penuh mampu memutar format *Advanced Audio Coding* (`.m4a`, `.aac`), format *lossless* kualitas tinggi `.aiff`.
- **Auto-Installer Independen**: Aplikasi ini memiliki mekanisme pengunduhan dependensi secara mandiri. Apabila *library* yang dibutuhkan tidak ditemukan pada sistem, aplikasi akan meminta konfirmasi pengguna untuk **mengunduh dan mengekstraknya secara otomatis** tanpa memerlukan perangkat lunak tambahan.
- **In-App Debug Terminal**: Tersedia fitur terminal *debug* internal yang dapat diakses melalui tombol **"🛠 Debug"**. Fitur ini berfungsi untuk memantau log sistem dan merekam informasi teknis tanpa mengganggu antarmuka utama.

### Library dan Teknologi yang Digunakan

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
