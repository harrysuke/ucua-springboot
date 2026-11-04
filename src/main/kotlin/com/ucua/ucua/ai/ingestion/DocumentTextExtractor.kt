package com.ucua.ucua.ai.ingestion

import com.ucua.ucua.config.AiProperties
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Extracts plain text from the document formats supported by the RAG corpus.
 *
 * - `pdf`  → Apache PDFBox; if the text layer is too thin (scanned document),
 *            falls back to Tesseract OCR of the rendered pages
 * - `docx` → Apache POI XWPF (paragraphs + table cells)
 * - `xlsx` → Apache POI XSSF (every sheet, every non-empty row)
 * - `md` / `markdown` / `txt` / `text` → read as UTF-8
 */
@Component
class DocumentTextExtractor(
    private val props: AiProperties,
    private val ocrService: OcrService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun supports(ext: String): Boolean = ext.lowercase() in SUPPORTED_EXTENSIONS

    fun extract(file: Path, ext: String): String = when (ext.lowercase()) {
        "pdf" -> extractPdf(file)
        "docx" -> extractDocx(file)
        "xlsx" -> extractXlsx(file)
        else -> Files.readString(file, StandardCharsets.UTF_8)
    }

    private fun extractPdf(file: Path): String =
        Loader.loadPDF(file.toFile()).use { doc ->
            val textLayer = PDFTextStripper().apply { sortByPosition = true }.getText(doc)
            val pages = doc.numberOfPages.coerceAtLeast(1)
            val hasUsableText = textLayer.trim().length >= pages * props.ocr.minCharsPerPage
            if (hasUsableText || !ocrService.isEnabled()) {
                textLayer
            } else {
                log.info("PDF '{}' has little/no text layer — running OCR", file.fileName)
                val ocrText = ocrPages(doc)
                if (ocrText.isBlank()) textLayer else "$textLayer\n$ocrText"
            }
        }

    private fun ocrPages(doc: PDDocument): String {
        val renderer = PDFRenderer(doc)
        val out = StringBuilder()
        for (page in 0 until doc.numberOfPages) {
            if (!ocrService.isEnabled()) break
            try {
                val image = renderer.renderImageWithDPI(page, props.ocr.dpi.toFloat())
                val pageText = ocrService.ocr(image)
                if (pageText.isNotBlank()) {
                    out.append("\n<!-- page ").append(page + 1).append(" (OCR) -->\n")
                    out.append(pageText)
                }
            } catch (e: Exception) {
                log.warn("Could not OCR page {} of a PDF: {}", page + 1, e.message)
            }
        }
        return out.toString()
    }

    private fun extractDocx(file: Path): String =
        Files.newInputStream(file).use { input ->
            XWPFDocument(input).use { doc ->
                buildString {
                    doc.paragraphs.forEach { p ->
                        if (p.text.isNotBlank()) appendLine(p.text)
                    }
                    doc.tables.forEach { table ->
                        table.rows.forEach { row ->
                            val cells = row.tableCells.map { it.text.trim() }.filter { it.isNotEmpty() }
                            if (cells.isNotEmpty()) appendLine(cells.joinToString(" | "))
                        }
                    }
                }
            }
        }

    private fun extractXlsx(file: Path): String =
        Files.newInputStream(file).use { input ->
            XSSFWorkbook(input).use { wb ->
                val formatter = DataFormatter()
                buildString {
                    for (sheet in wb) {
                        appendLine("## Sheet: ${sheet.sheetName}")
                        for (row in sheet) {
                            val cells = row.mapNotNull { cell ->
                                formatter.formatCellValue(cell).takeIf { it.isNotBlank() }
                            }
                            if (cells.isNotEmpty()) appendLine(cells.joinToString(" | "))
                        }
                    }
                }
            }
        }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("md", "markdown", "txt", "text", "pdf", "docx", "xlsx")
    }
}
