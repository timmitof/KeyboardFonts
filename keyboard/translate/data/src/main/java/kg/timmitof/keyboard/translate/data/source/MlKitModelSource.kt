package kg.timmitof.keyboard.translate.data.source

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Модели языков ML Kit на телефоне. Английский сюда не передаём: он встроен, его не качают и не удаляют. */
@Singleton
class MlKitModelSource @Inject constructor() {

    private val manager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    suspend fun downloadedCodes(): Set<String> =
        manager.getDownloadedModels(TranslateRemoteModel::class.java).await()
            .mapTo(mutableSetOf()) { it.language }

    suspend fun download(code: String) {
        manager.download(model(code), DownloadConditions.Builder().build()).await()
    }

    suspend fun delete(code: String) {
        manager.deleteDownloadedModel(model(code)).await()
    }

    private fun model(code: String): TranslateRemoteModel = TranslateRemoteModel.Builder(code).build()
}
