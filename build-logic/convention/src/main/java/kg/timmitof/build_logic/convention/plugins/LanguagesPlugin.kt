package kg.timmitof.build_logic.convention.plugins

import kg.timmitof.build_logic.convention.languages.KeyboardLanguagesExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/** Заводит каталог языков `keyboardLanguages { … }`; дочерние модули читают его своими плагинами. */
class LanguagesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create<KeyboardLanguagesExtension>(KeyboardLanguagesExtension.NAME)
    }
}
