package kg.timmitof.core.ui.components.brand

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.components.settings.SettingsBadge
import kg.timmitof.core.ui.components.settings.SettingsCheckList
import kg.timmitof.core.ui.locale.AppLanguage
import kg.timmitof.core.ui.locale.AppLocale
import kg.timmitof.core.ui.theme.appColors

/** Пилюля с кодом языка интерфейса в шапке; нажатие открывает шторку со всеми языками. */
@Composable
fun AppLanguageButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val current = remember(configuration) { AppLocale.current(context) }
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    val tones = MaterialTheme.appColors.brand
    val description = stringResource(R.string.app_language_change)

    Row(
        modifier = modifier
            .height(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.appColors.cardMuted)
            .semantics { contentDescription = description }
            .clickable { isSheetOpen = true }
            .padding(start = 8.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_globe),
            contentDescription = null,
            tint = tones.solid,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = current.shortName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
    }

    if (isSheetOpen) {
        AppLanguageSheet(
            current = current,
            onSelect = { language ->
                isSheetOpen = false
                context.findActivity()?.let { AppLocale.select(it, language) }
            },
            onDismiss = { isSheetOpen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppLanguageSheet(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.app_language_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SettingsCheckList(
                items = AppLocale.languages,
                key = AppLanguage::tag,
                onClick = onSelect,
            ) { language ->
                SettingsBadge(text = language.shortName)
                Text(
                    text = language.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                if (language == current) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.appColors.brand.solid,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
