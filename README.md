# Kelime Ezberleme

Kelime Ezberleme, Android için geliştirilmiş bir İngilizce kelime öğrenme uygulamasıdır. Uygulama; kelime ezberleme, tekrar etme, quiz çözme, Wordle benzeri oyun oynama ve analiz ekranları ile öğrenme sürecini destekler.

## Öne Çıkan Özellikler

- Kullanıcı kaydı ve giriş sistemi
- Şifre sıfırlama
- Kelime listesi ve kelime ekleme
- Quiz modu
- Wordle benzeri kelime oyunu
- Analiz ekranı
- Hesap ve ayar sayfaları
- Yapay zeka destekli yardımcı ekran

## Proje Yapısı

- `LoginActivity` ve `RegisterActivity`: giriş ve kayıt ekranları
- `MainActivity`: ana ekran
- `WordsListActivity`: kelime listesi
- `AddWordActivity`: yeni kelime ekleme
- `QuizActivity`: test modülü
- `WordleActivity`: Wordle benzeri oyun
- `AnalysisActivity`: kullanım ve başarı analizi
- `AccountActivity`: profil ve hesap işlemleri
- `SettingsActivity`: uygulama ayarları

## Kullanılan Teknolojiler

- Java 17
- Android Gradle Plugin 8.2.x
- `minSdk 24`
- `compileSdk 34`
- AndroidX
- Material Design
- SQLite
- GitHub Actions
- SonarCloud

## Kurulum

1. Android Studio ile projeyi aç.
2. Gradle senkronizasyonunun tamamlanmasını bekle.
3. Emülatör veya fiziksel cihaz seç.
4. Uygulamayı çalıştır.

## Derleme

Proje kök dizininde:

```bash
./gradlew assembleDebug
```

Windows için:

```powershell
gradlew.bat assembleDebug
```

## Notlar

- Bu proje eğitim amaçlı kelime öğrenme ve tekrar pratiği üzerine odaklanır.
- Otomatik build ve kalite kontrolleri için GitHub Actions ve SonarCloud kullanılır.
- Proje kaynak kodu `proje/` klasörü içindedir.

## Geliştirme Hakkında

Uygulama, kullanıcıların kelime öğrenme sürecini düzenli tekrar, quiz ve oyunlaştırma ile desteklemek için tasarlanmıştır. Yerel veriler SQLite veritabanında saklanır.

## Lisans / Ders Notu

Bu proje, yazılım geliştirme süreci kapsamında hazırlanmıştır.
