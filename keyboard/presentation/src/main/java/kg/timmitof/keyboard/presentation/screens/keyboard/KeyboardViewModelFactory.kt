package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository

class KeyboardViewModelFactory(
    private val keyboardLayoutRepository: KeyboardLayoutRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            return KeyboardViewModel(keyboardLayoutRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}