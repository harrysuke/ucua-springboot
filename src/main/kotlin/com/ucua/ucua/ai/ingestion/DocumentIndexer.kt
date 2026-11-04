package com.ucua.ucua.ai.ingestion

import com.ucua.ucua.ai.client.EmbeddingClient
import com.ucua.ucua.ai.store.VectorStore
import com.ucua.ucua.config.AiProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * File-based RAG ingestion.
 *
 * Scans [AiProperties.Corpus.dir] (default `./rag-docs`), splits every supported
 * text document into overlapping chunks, embeds each chunk and upserts it into the
 * vector store under `sourceType = "DOC"`.
 *
 * A chunk's `sourceRef` is `<relative-path>#<chunkIndex>` (e.g.
 * `policies/working-at-height.md#2`) so RAG citations point at the exact document
 * and chunk. The whole `DOC` corpus is rebuilt on every run, so deleting or
 * renaming a file never leaves stale vectors behind.
 */
@Component
class DocumentIndexer(
    private val embeddingClient: EmbeddingClient,
    private val vectorStore: VectorStore,
    private val textExtractor: DocumentTextExtractor,
    private val props: AiProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    data class Result(val documents: Int, val chunks: Int)

    /**
     * Rebuild the `DOC` portion of the vector store from the corpus directory.
     * [onProgress] is called as (filesProcessed, filesTotal) after each file.
     */
    fun indexAllDocuments(onProgress: ((processed: Int, total: Int) -> Unit)? = null): Result {
        val dir = Path.of(props.corpus.dir).toAbsolutePath().normalize()
        if (!Files.isDirectory(dir)) {
            log.warn("RAG corpus directory '{}' does not exist — skipping document indexing", dir)
            return Result(0, 0)
        }

        val files: List<Path> = try {
            val stream = Files.walk(dir)
            try {
                stream.filter { Files.isRegularFile(it) }
                    .filter { isSupported(it) && isWithinSizeLimit(it) }
                    .sorted()
                    .collect(Collectors.toList())
            } finally {
                stream.close()
            }
        } catch (e: Exception) {
            log.warn("Could not scan corpus directory '{}': {}", dir, e.message)
            return Result(0, 0)
        }

        // Rebuild from scratch so removed/renamed/shrunk files leave no stale chunks.
        vectorStore.deleteByType(SOURCE_TYPE)

        var documents = 0
        var chunks = 0
        var processed = 0
        onProgress?.invoke(0, files.size)
        for (file in files) {
            try {
                val text = textExtractor.extract(file, extOf(file))
                val pieces = chunk(text, props.corpus.chunkSize, props.corpus.chunkOverlap)
                if (pieces.isEmpty()) {
                    log.warn("No extractable text in '{}' — skipped", file.fileName)
                } else {
                    val vectors = embeddingClient.embedBatch(pieces)
                    val ref = dir.relativize(file).toString().replace('\\', '/')
                    var fileChunks = 0
                    pieces.forEachIndexed { index, piece ->
                        val vector = vectors.getOrNull(index)
                        if (vector != null && vector.isNotEmpty()) {
                            vectorStore.upsert(
                                SOURCE_TYPE, 0, "$ref#$index", piece, vector,
                                props.ollama.embeddingModel
                            )
                            fileChunks++
                            chunks++
                        }
                    }
                    documents++
                    log.info("Indexed {} chunk(s) from '{}'", fileChunks, ref)
                }
            } catch (e: Exception) {
                log.warn("Skipping document '{}': {}", file, e.message)
            } finally {
                processed++
                onProgress?.invoke(processed, files.size)
            }
        }

        log.info("Document indexing complete: {} file(s), {} chunk(s) from {}", documents, chunks, dir)
        return Result(documents, chunks)
    }

    private fun isSupported(file: Path): Boolean {
        val name = file.fileName?.toString() ?: return false
        if (name.startsWith(".")) return false
        return textExtractor.supports(extOf(file))
    }

    private fun extOf(file: Path): String =
        file.fileName?.toString()?.substringAfterLast('.', "")?.lowercase() ?: ""

    private fun isWithinSizeLimit(file: Path): Boolean = try {
        Files.size(file) <= props.corpus.maxFileSizeBytes
    } catch (_: Exception) {
        false
    }

    companion object {
        const val SOURCE_TYPE = "DOC"

        /**
         * Split [text] into chunks of at most [size] characters, carrying [overlap]
         * characters between neighbours and preferring paragraph, sentence, then
         * word boundaries so chunks stay readable.
         */
        fun chunk(text: String, size: Int, overlap: Int): List<String> {
            val clean = text.replace("\r\n", "\n").replace('\r', '\n').trim()
            if (clean.isEmpty()) return emptyList()

            val max = size.coerceAtLeast(200)
            val ov = overlap.coerceIn(0, max / 2)
            if (clean.length <= max) return listOf(clean)

            val out = ArrayList<String>()
            var start = 0
            while (start < clean.length) {
                var end = minOf(start + max, clean.length)
                if (end < clean.length) {
                    val window = clean.substring(start, end)
                    val para = window.lastIndexOf("\n\n")
                    val sent = window.lastIndexOf(". ")
                    val word = window.lastIndexOf(' ')
                    end = when {
                        para > max / 2 -> start + para + 2
                        sent > max / 2 -> start + sent + 2
                        word > max / 2 -> start + word + 1
                        else -> end
                    }
                }
                val piece = clean.substring(start, end).trim()
                if (piece.isNotEmpty()) out.add(piece)
                if (end >= clean.length) break
                start = maxOf(end - ov, start + 1)
            }
            return out
        }
    }
}
