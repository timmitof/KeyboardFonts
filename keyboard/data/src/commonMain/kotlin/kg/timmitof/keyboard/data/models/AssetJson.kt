package kg.timmitof.keyboard.data.models

import kotlinx.serialization.json.Json

/** Разбор JSON из ассетов. Незнакомые поля пропускаются, как раньше у Gson. */
internal val AssetJson = Json { ignoreUnknownKeys = true }
