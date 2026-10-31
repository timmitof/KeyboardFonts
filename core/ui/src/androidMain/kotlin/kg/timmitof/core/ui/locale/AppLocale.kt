package kg.timmitof.core.ui.locale

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Язык интерфейса приложения и клавиатуры — те же языки, что в каталоге клавиатуры.
 *
 * Пока язык не выбран, интерфейс на русском, а не на языке системы.
 *
 * С Android 13 выбранный язык хранит система (`LocaleManager`): она сама пересоздаёт экраны и показывает
 * выбор в настройках приложения. На старых версиях выбор лежит в своих настройках. Активити и сервис
 * клавиатуры подменяют конфигурацию в `attachBaseContext` через [wrap] — выбранным языком до Android 13
 * и русским по умолчанию на любой версии.
 */
object AppLocale {

    /** Язык, на котором сейчас показан [context]: выбранный или русский по умолчанию. */
    fun current(context: Context): AppLanguage = resolve(context.resources.configuration.locales[0])

    /** Сменить язык; экран пересоздаётся с новыми строками. */
    fun select(activity: Activity, language: AppLanguage) {
        if (language == current(activity)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(language.tag)
        } else {
            preferences(activity).edit().putString(KEY_TAG, language.tag).commit()
            activity.recreate()
        }
    }

    /** Контекст с выбранным языком, а без выбора — с русским. Выбор с Android 13 подставляет сама система. */
    fun wrap(base: Context): Context {
        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!base.getSystemService(LocaleManager::class.java).applicationLocales.isEmpty) return base
            DEFAULT_TAG
        } else {
            preferences(base).getString(KEY_TAG, null) ?: DEFAULT_TAG
        }

        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(tag))
        return base.createConfigurationContext(configuration)
    }

    private fun resolve(locale: Locale?): AppLanguage = resolveAppLanguage(locale?.language, locale?.script)

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private const val PREFERENCES = "app_locale"
    private const val KEY_TAG = "tag"
    private const val DEFAULT_TAG = "ru"
}
