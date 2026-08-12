package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout

interface KeyboardLayoutRepository {

    suspend fun getLayout(language: String): KeyboardLayout

    /**
     * Нижний ряд под тип поля ввода (`email`, `password`, `search`, `message`).
     *
     * null — варианта нет, раскладка остаётся со своим рядом.
     */
    suspend fun getBottomRow(variant: String): List<KeyboardKey>?
}
