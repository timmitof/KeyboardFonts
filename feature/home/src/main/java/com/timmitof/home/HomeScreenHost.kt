package com.timmitof.home

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timmitof.common.toolbar.ToolbarMode
import com.timmitof.common.toolbar.ToolbarState

@Composable
fun HomeScreenHost(
    navController: NavController = rememberNavController(),
    toolbarState: MutableState<ToolbarState> = remember { mutableStateOf(ToolbarState()) },
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    var showKeyboardTutorialDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var keyboardIsEnabled by remember { mutableStateOf(false) }
    var keyboardIsUsing by remember { mutableStateOf(false) }

    HomeScreen(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = contentPadding
    )

    LaunchedEffect(Unit) {
        toolbarState.value = ToolbarState(
            mode = ToolbarMode.Main(
                onKeyboardTutorial = { showKeyboardTutorialDialog = !showKeyboardTutorialDialog },
                onSettings = { showSettingsDialog = !showSettingsDialog }
            )
        )
    }

    LaunchedEffect(showKeyboardTutorialDialog) {
        keyboardIsEnabled = isKeyboardEnabled(context)
        keyboardIsUsing = isUsingKeyboard(context)
    }
}

private fun isKeyboardEnabled(context: Context): Boolean {
    // TODO: Check enable keyboard
    return false
}

private fun isUsingKeyboard(context: Context): Boolean {
    // TODO: Check is using keyboard
    return false
}