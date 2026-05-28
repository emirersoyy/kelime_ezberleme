# Kelime Ezberleme

Kelime Ezberleme, Android için geliştirilmiş bir İngilizce kelime öğrenme uygulamasıdır.  
Uygulama; kelime ezberleme, tekrar etme, quiz çözme, Wordle benzeri oyun oynama ve analiz ekranları ile öğrenme sürecini destekler.

## Özellikler

- Kullanıcı kaydı ve giriş sistemi
- Şifre sıfırlama
- Kelime listesi ve kelime ekleme
- Quiz modu
- Wordle benzeri kelime oyunu
- Analiz ekranı
- Hesap ve ayar sayfaları
- Yapay zeka destekli yardımcı ekran

## Teknolojiler

- Java 17
- Android Gradle Plugin 8.2.x
- `minSdk 24`
- `compileSdk 34`
- AndroidX
- Material Design
- SQLite

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

## Kurulum

1. Android Studio ile projeyi aç.
2. Gradle senkronizasyonunun tamamlanmasını bekle.
3. Emülatör veya fiziksel cihaz seç.
4. Uygulamayı çalıştır.

## Derleme

Terminalden proje kökünde:

```bash
./gradlew assembleDebug
```

Windows için:

```powershell
gradlew.bat assembleDebug
```

## Not

Bu proje eğitim amaçlı kelime öğrenme ve tekrar pratiği üzerine odaklanır.  
SonarCloud kalite kontrolleri ve GitHub Actions ile otomatik derleme/test akışı da yapılandırılmıştır.
