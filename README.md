# BERKAH ASMR — Rain ASMR Studio Mobile (Android)

*by Aziez Zembut*

Aplikasi Android untuk konten ASMR hujan:
- 🎧 **Audio:** deteksi suara kaget → buang otomatis → stereo lebar → EQ hangat → loudness (LUFS) → limiter → **master loop mulus** (WAV)
- 🖼️ **Thumbnail:** 3 variasi 1280×720 gaya *Pure Blessings Rain* / *Rainy Chum* / *Gelap Netral*
- File tersimpan di **Download/BerkahASMR**

APK dibuat otomatis oleh **GitHub Actions**. Anda tidak perlu menginstal Android Studio.

---

## Cara membuat APK lewat GitHub (sekali saja, ±10 menit)

1. Login ke **github.com** (buat akun gratis kalau belum punya).
2. Klik **+** (kanan atas) → **New repository**
   - Repository name: `berkah-asmr`
   - Pilih **Public** (GitHub Actions gratis tanpa batas untuk repo publik)
   - Klik **Create repository**
3. Di halaman repo baru, klik link **"uploading an existing file"**.
4. Ekstrak ZIP ini di PC, buka folder **BerkahASMRApp**, lalu **seret SEMUA isinya**
   (termasuk folder `.github`, `app`, dan file `build.gradle`, dll.) ke halaman upload GitHub.
   > Penting: yang diseret adalah **isi** folder, bukan folder BerkahASMRApp-nya.
   > Pastikan folder **`.github`** ikut terupload. Tanpa folder ini, APK tidak akan dibuat.
5. Klik **Commit changes**.
6. Buka tab **Actions**. Proses **"Build APK Berkah ASMR"** akan berjalan sekitar 5–8 menit.
   Tunggu sampai muncul tanda ✅ hijau.
7. Kembali ke halaman utama repo → bagian **Releases** (kolom kanan) → klik versi terbaru
   → download **BerkahASMR.apk**. Langkah ini paling mudah dilakukan langsung dari HP.

## Menginstal di HP

1. Buka file **BerkahASMR.apk** yang sudah didownload.
2. Kalau muncul "Instal dari sumber tidak dikenal", izinkan untuk browser/File Manager.
3. Kalau Play Protect memberi peringatan (karena aplikasi tidak berasal dari Play Store),
   pilih **Detail lainnya → Tetap instal**.

## Kalau ada masalah

| Masalah | Solusi |
|---|---|
| Tab Actions kosong / tidak ada proses | Folder `.github/workflows` belum terupload. Upload ulang folder `.github`. |
| Tanda ❌ merah di langkah "Terbitkan di Releases" | Repo → **Settings → Actions → General → Workflow permissions** → pilih **Read and write permissions** → Save. Lalu di tab Actions klik **Re-run all jobs**. APK juga tetap bisa didownload dari bagian **Artifacts** di halaman proses tersebut (berbentuk ZIP, ekstrak dulu). |
| ❌ merah di langkah "Build APK" | Klik langkah itu, screenshot pesan error-nya, lalu kirim ke Claude. |
| Ingin build ulang | Tab **Actions** → *Build APK Berkah ASMR* → **Run workflow**. |

## Batasan versi HP

- Rekaman diproses **maksimal 10 menit** (memori HP terbatas). Rekaman yang lebih panjang otomatis dipotong.
- Render video 8–10 jam **tetap di PC** (Rain ASMR Studio untuk Windows), karena terlalu berat untuk HP.

Alur kerja yang disarankan: **HP** untuk merekam, membersihkan, membuat master loop, dan thumbnail → **PC** untuk render video panjang → upload ke YouTube.
