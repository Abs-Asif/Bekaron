# Bekaron Implementation & Workflow Guide

`bekaron` (Package: `bangla.English.bekaron`) is an offline AI-powered grammar tool supporting **Bangla**, **English**, and **Arabic**. It features **Part-of-Speech (POS) Tagging** and **Gap Filling** powered by quantized ONNX BERT models running locally on-device.

---

## Phase 0: Product Setup

- **App Name**: `bekaron`
- **Package Name**: `bangla.English.bekaron`
- **Language & Tech Stack**: Kotlin, Jetpack Compose, ONNX Runtime Android (`com.microsoft.onnxruntime:onnxruntime-android`)
- **Version Scheme**: Date-based (`yyyy.MM.dd.HH.mm`)
- **Locked Features**:
  1. **POS Tagging**: Identifies token-level grammatical components (Noun, Verb, Adjective, etc.) for English, Bangla, and Arabic.
  2. **Gap Fill**: Predicts missing words (`___` or `[MASK]`) in sentences given candidate options or top-ranked vocabulary tokens.

---

## Phase 1: Model Acquisition

No model training is required. Download pre-trained Hugging Face BERT models:

1. **English POS**: [`vblagoje/bert-english-uncased-finetuned-pos`](https://huggingface.co/vblagoje/bert-english-uncased-finetuned-pos)
   - Fine-tuned for token classification (POS tagging) with ~97% accuracy.
2. **Bangla POS**: [`hasan-rag/bangla-bert-pos`](https://huggingface.co/hasan-rag/bangla-bert-pos) or [`sagorsarker/bangla-bert-base`](https://huggingface.co/sagorsarker/bangla-bert-base)
   - Fine-tuned on Bangla POS datasets.
3. **Arabic POS**: [`CAMeL-Lab/bert-base-arabic-camelbert-msa-pos-msa`](https://huggingface.co/CAMeL-Lab/bert-base-arabic-camelbert-msa-pos-msa)
   - Built specifically for Modern Standard Arabic (MSA) POS tagging.

### Gap Fill Capability
Because all three models are BERT-based architectures, they natively support Masked Language Modeling (MLM) using `[MASK]` tokens. A single model file per language handles both POS tagging and Gap Fill natively.

---

## Phase 2: Model Conversion & Quantization (Python Script)

Convert Hugging Face PyTorch models to ONNX and apply INT8 quantization to reduce file size from ~400MB to ~25-30MB per model.

### 2.1 Installation
```bash
pip install optimum onnxruntime transformers torch
```

### 2.2 Conversion Script (`convert_models.py`)
```python
import os
import subprocess

MODELS = {
    "en": "vblagoje/bert-english-uncased-finetuned-pos",
    "bn": "hasan-rag/bangla-bert-pos",
    "ar": "CAMeL-Lab/bert-base-arabic-camelbert-msa-pos-msa"
}

OUTPUT_DIR = "app/src/main/assets/models"
os.makedirs(OUTPUT_DIR, exist_ok=True)

for lang, model_id in MODELS.items():
    print(f"--- Exporting and Quantizing {lang} ({model_id}) ---")
    raw_onnx_dir = f"onnx_tmp_{lang}"

    # 1. Export model to ONNX format
    subprocess.run([
        "optimum-cli", "export", "onnx",
        "--model", model_id,
        "--task", "token-classification",
        raw_onnx_dir
    ], check=True)

    # 2. Quantize ONNX model to INT8
    subprocess.run([
        "optimum-cli", "onnxruntime", "quantize",
        "--onnx_model", raw_onnx_dir,
        "--avx2",
        "-o", f"{raw_onnx_dir}/quantized"
    ], check=True)

    # 3. Copy quantized model and tokenizer config to Android assets folder
    quantized_model_path = os.path.join(raw_onnx_dir, "quantized", "model_quantized.onnx")
    target_model_path = os.path.join(OUTPUT_DIR, f"{lang}_pos.onnx")

    if os.path.exists(quantized_model_path):
        os.rename(quantized_model_path, target_model_path)
        print(f"Saved: {target_model_path}")
```

### 2.3 Deliverables
- `en_pos.onnx` (~25MB) + `en_tokenizer.json`
- `bn_pos.onnx` (~25MB) + `bn_tokenizer.json`
- `ar_pos.onnx` (~25MB) + `ar_tokenizer.json`
Place all deliverables inside `app/src/main/assets/models/`.

---

## Phase 3: Android App Implementation Details

### 3.1 Project Setup
- **Dependencies**:
  - `com.microsoft.onnxruntime:onnxruntime-android:1.17.0`
  - Jetpack Compose UI, Material3, Navigation
  - Kotlin Coroutines (`kotlinx-coroutines-android`)

### 3.2 Core Module: `OfflineGrammarEngine.kt`
- **Language Detection**:
  - Bengali range: `[\u0980-\u09FF]`
  - Arabic range: `[\u0600-\u06FF]`
  - Default: English
- **Memory Management**:
  - Load model into `OrtSession` dynamically based on detected language.
  - Close/unload previous session before loading a new language model to minimize memory footprint.
- **Inference Methods**:
  - `getPOS(text: String)`: Tokenizes text, passes tensor to ONNX model, maps logits to POS tag labels.
  - `fillGap(sentence: String, options: List<String>)`: Replaces `___` with `[MASK]`, evaluates candidate options via MLM prediction scores, returns highest probability option.
- **Threading**:
  - Run all heavy ONNX inference calls on `Dispatchers.Default` coroutine context.

### 3.3 User Interface (Jetpack Compose)
- **POS Analyzer Screen**:
  - Real-time or submit-on-click text input.
  - Dynamic language badge (Bangla / English / Arabic).
  - Categorized POS tags displayed as styled chips/cards.
- **Gap Fill Screen**:
  - Input field for sentence containing `___`.
  - Input fields or chips for candidate choices.
  - RTL Layout handling for Arabic using:
    ```kotlin
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // Arabic UI components
    }
    ```
