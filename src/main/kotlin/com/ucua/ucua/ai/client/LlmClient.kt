package com.ucua.ucua.ai.client

interface LlmClient {
    fun generate(system: String, user: String): String
}
