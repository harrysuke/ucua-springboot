package com.ucua.ucua.ai.ingestion

import com.ucua.ucua.ai.client.EmbeddingClient
import com.ucua.ucua.ai.store.VectorStore
import com.ucua.ucua.config.AiProperties
import com.ucua.ucua.entity.Offence
import com.ucua.ucua.entity.UcuaForm
import com.ucua.ucua.repository.OffenceRepository
import com.ucua.ucua.repository.UcuaFormRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CorpusIndexer(
    private val offenceRepository: OffenceRepository,
    private val ucuaFormRepository: UcuaFormRepository,
    private val embeddingClient: EmbeddingClient,
    private val vectorStore: VectorStore,
    private val props: AiProperties
) {

    /** Index a single report after it is committed (fired from ReportService). */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onReportSaved(event: ReportIndexedEvent) {
        try {
            val form = ucuaFormRepository.findById(event.reportId).orElse(null) ?: return
            if (form.isDelete != 0) return
            val text = buildReportText(form)
            if (text.length <= 3) return
            vectorStore.upsert(
                "REPORT", 0, form.id.toString(), text,
                embeddingClient.embed(text), props.ollama.embeddingModel
            )
        } catch (_: Exception) {
            // embedding/store failure must not break the originating request
        }
    }

    fun indexAllOffences(): Int {
        var n = 0
        offenceRepository.findByIsDeleteOrderByOffenceCodeAsc(0).forEach { off ->
            val text = buildOffenceText(off)
            vectorStore.upsert(
                "OFFENCE", 0, off.offenceCode, text,
                embeddingClient.embed(text), props.ollama.embeddingModel
            )
            n++
        }
        return n
    }

    fun indexAllReports(): Int {
        var n = 0
        ucuaFormRepository.findAll()
            .filter { it.isDelete == 0 && buildReportText(it).length > 3 }
            .forEach { f ->
                val text = buildReportText(f)
                vectorStore.upsert(
                    "REPORT", 0, f.id.toString(), text,
                    embeddingClient.embed(text), props.ollama.embeddingModel
                )
                n++
            }
        return n
    }

    private fun buildOffenceText(off: Offence): String =
        "${off.category} | ${off.offKeyword} | ${off.offence}"

    private fun buildReportText(f: UcuaForm): String = listOf(
        f.formType, f.conditionDetails, f.actionTaken, f.followUp,
        f.observerConditionDetails, f.violatorName, f.violatorCompany, f.violatorDept
    ).filter { !it.isNullOrBlank() }.joinToString(" | ")

    /** On startup, seed the offence catalogue into the vector store if empty. */
    @Bean
    fun startupIndexer(): ApplicationRunner = ApplicationRunner {
        if (!props.enabled) return@ApplicationRunner
        try {
            if (vectorStore.count("OFFENCE") == 0L) indexAllOffences()
        } catch (_: Exception) {
            // Ollama may not be running yet; will index on first /reindex
        }
    }
}
