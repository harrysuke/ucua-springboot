package com.ucua.ucua.config

import com.ucua.ucua.ai.client.EmbeddingClient
import com.ucua.ucua.ai.client.LlmClient
import com.ucua.ucua.ai.client.OllamaEmbeddingClient
import com.ucua.ucua.ai.client.OllamaLlmClient
import com.ucua.ucua.ai.store.EmbeddingRepository
import com.ucua.ucua.ai.store.JpaVectorStore
import com.ucua.ucua.ai.store.VectorStore
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.client.RestClient

@Configuration
@EnableAsync
@EnableConfigurationProperties(AiProperties::class)
class AiConfig {

    @Bean
    fun embeddingClient(builder: RestClient.Builder, props: AiProperties): EmbeddingClient =
        when (props.provider.lowercase()) {
            "ollama" -> OllamaEmbeddingClient(builder, props.ollama.baseUrl, props.ollama.embeddingModel)
            else -> throw IllegalStateException(
                "ai.provider '${props.provider}' is not supported by the spike (use 'ollama')"
            )
        }

    @Bean
    fun llmClient(builder: RestClient.Builder, props: AiProperties): LlmClient =
        when (props.provider.lowercase()) {
            "ollama" -> OllamaLlmClient(builder, props.ollama.baseUrl, props.ollama.chatModel)
            else -> throw IllegalStateException(
                "ai.provider '${props.provider}' is not supported by the spike (use 'ollama')"
            )
        }

    @Bean
    fun vectorStore(repo: EmbeddingRepository): VectorStore = JpaVectorStore(repo)
}
