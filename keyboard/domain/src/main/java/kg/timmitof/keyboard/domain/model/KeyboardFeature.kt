package kg.timmitof.keyboard.domain.model

/**
 * Отдельная возможность клавиатуры: пользователь включает её выключателем [toggle],
 * а доступна она по уровню [tier]. Новая возможность — одна запись здесь.
 */
enum class KeyboardFeature(
    val toggle: KeyboardToggle,
    val tier: FeatureTier,
) {
    TRANSLATOR(KeyboardToggle.TRANSLATOR, FeatureTier.FREE),
}

/** Уровень доступа. Платное появится вместе с покупками — тогда фичу достаточно перевести в [PREMIUM]. */
enum class FeatureTier { FREE, PREMIUM }

/** Что открыто пользователю: бесплатное — всегда, платное — после покупки. */
data class FeatureAccess(
    val unlockedTiers: Set<FeatureTier> = setOf(FeatureTier.FREE),
) {
    fun isUnlocked(feature: KeyboardFeature): Boolean = feature.tier in unlockedTiers
}
