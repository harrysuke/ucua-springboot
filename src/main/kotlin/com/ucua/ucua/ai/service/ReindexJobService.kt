package com.ucua.ucua.ai.service

import com.ucua.ucua.ai.dto.ReindexStatus
import com.ucua.ucua.ai.ingestion.CorpusIndexer
import com.ucua.ucua.ai.ingestion.DocumentIndexer
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Runs the full vector-store reindex (offences → reports → documents) on a single
 * background thread so the HTTP request returns immediately. Only one job can run
 * at a time; [start] is idempotent while a job is in flight.
 */
@Service
class ReindexJobService(
    private val corpusIndexer: CorpusIndexer,
    private val documentIndexer: DocumentIndexer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "reindex-job").apply { isDaemon = true }
    }

    private val running = AtomicBoolean(false)
    private val status = AtomicReference(ReindexStatus(state = "IDLE"))

    /** Start a reindex unless one is already running. Returns the current status. */
    fun start(): ReindexStatus {
        if (!running.compareAndSet(false, true)) {
            log.info("Reindex already running; returning current status")
            return status.get()
        }
        status.set(ReindexStatus(state = "RUNNING", startedAt = now()))
        executor.submit { runJob() }
        return status.get()
    }

    fun current(): ReindexStatus = status.get()

    private fun runJob() {
        val startedAt = status.get().startedAt
        var total = 0
        try {
            val offences = corpusIndexer.indexAllOffences()
            val reports = corpusIndexer.indexAllReports()

            val docs = documentIndexer.indexAllDocuments { processed, filesTotal ->
                total = filesTotal
                status.set(
                    status.get().copy(
                        filesProcessed = processed,
                        filesTotal = filesTotal,
                        indexedOffences = offences,
                        indexedReports = reports
                    )
                )
            }

            status.set(
                ReindexStatus(
                    state = "COMPLETED",
                    startedAt = startedAt,
                    finishedAt = now(),
                    filesProcessed = if (total > 0) total else docs.documents,
                    filesTotal = if (total > 0) total else docs.documents,
                    indexedOffences = offences,
                    indexedReports = reports,
                    indexedDocuments = docs.documents,
                    indexedChunks = docs.chunks
                )
            )
            log.info(
                "Reindex completed: offences={}, reports={}, documents={}, chunks={}",
                offences, reports, docs.documents, docs.chunks
            )
        } catch (e: Exception) {
            log.error("Reindex job failed", e)
            status.set(status.get().copy(state = "FAILED", finishedAt = now(), error = e.message))
        } finally {
            running.set(false)
        }
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdownNow()
    }

    private fun now(): String = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
}
