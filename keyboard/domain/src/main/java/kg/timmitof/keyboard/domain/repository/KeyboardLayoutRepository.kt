package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardLayout

interface KeyboardLayoutRepository {

    suspend fun getLayout(language: String): KeyboardLayout
}