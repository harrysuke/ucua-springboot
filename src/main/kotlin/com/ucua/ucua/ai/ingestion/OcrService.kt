package com.ucua.ucua.ai.ingestion

import com.ucua.ucua.config.AiProperties
import net.sourceforge.tess4j.Tesseract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

/**
 * Thin wrapper around Tesseract (via Tess4J) for OCR'ing rendered PDF pages.
 *
 * Initialised lazily on first use. If the native library or the `tessdata`
 * language files are missing, OCR is disabled for the rest of the process and a
 * single clear error is logged — reindexing then simply falls back to the PDF
 * text layer instead of failing.
 */
@Component
class OcrService(private val props: AiProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var tesseract: Tesseract? = null

    @Volatile
    private var unavailable = false

    fun isEnabled(): Boolean = props.ocr.enabled && !unavailable

    /** OCR a single page image. Returns "" if OCR is unavailable or the page fails. */
    fun ocr(image: BufferedImage): String {
        val engine = engine() ?: return ""
        return try {
            // Tesseract instances are not thread-safe.
            synchronized(this) { engine.doOCR(image) }.orEmpty()
        } catch (e: Exception) {
            log.warn("OCR failed on a page: {}", e.message)
            ""
        }
    }

    private fun engine(): Tesseract? {
        if (!props.ocr.enabled || unavailable) return null
        tesseract?.let { return it }

        synchronized(this) {
            tesseract?.let { return it }
            if (unavailable) return null

            val dataDir = Path.of(props.ocr.tessdataPath).toAbsolutePath().normalize()
            if (!Files.isDirectory(dataDir)) {
                unavailable = true
                log.error(
                    "OCR disabled: tessdata directory '{}' not found. " +
                        "Place e.g. eng.traineddata there or set ai.ocr.tessdata-path.",
                    dataDir
                )
                return null
            }

            return try {
                val engine = Tesseract()
                engine.setDatapath(dataDir.toString())
                engine.setLanguage(props.ocr.language)
                tesseract = engine
                log.info(
                    "OCR enabled (Tesseract, language='{}', dpi={}, tessdata='{}')",
                    props.ocr.language, props.ocr.dpi, dataDir
                )
                engine
            } catch (e: LinkageError) {
                unavailable = true
                log.error("OCR disabled: Tesseract native library could not be loaded: {}", e.message)
                null
            } catch (e: Exception) {
                unavailable = true
                log.error(
                    "OCR disabled: could not initialise Tesseract (tessdata='{}', language='{}'): {}",
                    dataDir, props.ocr.language, e.message
                )
                null
            }
        }
    }
}
