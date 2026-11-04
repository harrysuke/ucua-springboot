Tesseract language data for OCR fallback
========================================

This folder is the `ai.ocr.tessdata-path` (default `./tessdata`).
Tess4J bundles the native Tesseract library, but NOT the language files.

Required for OCR:
  - eng.traineddata  (English)

Optional (multi-language, e.g. Malay):
  - msa.traineddata  (then set ai.ocr.language: eng+msa)

Download from the official Tesseract tessdata repo, e.g.:

  https://github.com/tesseract-ocr/tessdata/raw/main/eng.traineddata
  https://github.com/tesseract-ocr/tessdata/raw/main/msa.traineddata

(tessdata_fast is smaller/faster; tessdata_best is more accurate but larger.)

If these files are missing, OCR is skipped and PDFs fall back to their text
layer only — reindexing still works, it just cannot read scanned pages.

Windows note: Tess4J's native binaries require the Microsoft Visual C++ v14
Redistributable to be installed.
