package kg.timmitof.core.ui

import android.content.Context
import android.widget.Toast

/**
 * Функция расширяющая [Context], для показа тоста
 */
fun Context.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()