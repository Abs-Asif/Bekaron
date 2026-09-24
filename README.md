# bekaron (ব্যাকরণ)

**bekaron** is an on-device, offline AI grammar assistant app built with **Kotlin** and **Jetpack Compose** for **Bangla**, **English**, and **Arabic**.

- **Package Name**: `bangla.English.bekaron`
- **App Name**: `bekaron`
- **Version**: Date-based versioning scheme (e.g. `2026.12.31.12.59`)
- **Core ML Engine**: Local ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime-android`) using quantized BERT Transformer models.

---

## 🎯 Locked Features

1. **POS Analyzer**: Analyzes inputted text and assigns Part-of-Speech tags (Noun, Verb, Adjective, Pronoun, Preposition, etc.) to each token with dynamic multi-language detection.
2. **Gap Fill Engine**: Replaces missing sentence gaps (`___`) with appropriate candidate words or predicts high-scoring tokens using local Masked Language Modeling (MLM).
3. **Multi-Language & RTL**: Full support for English, Bangla (LTR), and Arabic (RTL support via `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`).

---

## 🗺️ Roadmap & To-Do List

### Phase 0: Product Setup
- [x] Lock core features: POS Tagging + Gap Fill
- [x] Configure Kotlin project scaffolding with package `bangla.English.bekaron`
- [x] Setup date-based automated versioning (`yyyy.MM.dd.HH.mm`)

### Phase 1: Model Identification & Selection
- [x] Identify English POS model: `vblagoje/bert-english-uncased-finetuned-pos`
- [x] Identify Bangla POS model: `hasan-rag/bangla-bert-pos` / `sagorsarker/bangla-bert-base`
- [x] Identify Arabic POS model: `CAMeL-Lab/bert-base-arabic-camelbert-msa-pos-msa`
- [x] Verify Native MLM `[MASK]` support for Gap Fill across all models

### Phase 2: Model Conversion & Optimization
- [x] Document Python export toolchain using `optimum-cli` and `onnxruntime`
- [x] Set up model output path structure (`app/src/main/assets/models/`)
- [ ] Export Hugging Face models to ONNX
- [ ] Quantize models to INT8 / AVX2 format (reducing size to ~25-30MB)

### Phase 3: Android App Implementation
- [x] Integrate `onnxruntime-android` dependency
- [x] Implement `OfflineGrammarEngine.kt`:
  - [x] `detectLanguage(text)` (Bangla: `[\u0980-\u09FF]`, Arabic: `[\u0600-\u06FF]`, English: default)
  - [x] `loadModel(lang)` with memory management (loads single model, unloads previous)
  - [x] `getPOS(text)` with `Dispatchers.Default` execution
  - [x] `fillGap(sentence, options)` with native MLM fallback & scoring
- [x] Implement Jetpack Compose UI:
  - [x] Screen 1: POS Analyzer (text input, language indicator badge, POS result tags)
  - [x] Screen 2: Gap Fill (sentence input with `___`, option selection, RTL support for Arabic)
  - [x] Navigation bar for switching between features
- [x] Unit testing & Gradle build verification

---

## 🏗️ Technical Architecture

```text
app/
 ├── src/
 │    ├── main/
 │    │    ├── assets/
 │    │    │    └── models/           # Quantized ONNX model files (.onnx)
 │    │    ├── java/bangla/English/bekaron/
 │    │    │    ├── MainActivity.kt
 │    │    │    ├── engine/
 │    │    │    │    └── OfflineGrammarEngine.kt
 │    │    │    ├── ui/
 │    │    │    │    ├── BekaronApp.kt
 │    │    │    │    ├── PosAnalyzerScreen.kt
 │    │    │    │    └── GapFillScreen.kt
 │    │    │    └── theme/
 │    │    └── AndroidManifest.xml
 │    └── test/
 ├── build.gradle.kts
 └── settings.gradle.kts
```

---

## 🛠️ How to Build & Run

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 34)

### Build Commands
```bash
# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

For model conversion instructions, see [`STAGES.md`](STAGES.md).
