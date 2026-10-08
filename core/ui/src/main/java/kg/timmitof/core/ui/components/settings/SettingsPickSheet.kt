package kg.timmitof.core.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.components.field.ProbeTextField
import kg.timmitof.core.ui.theme.appColors

/**
 * Раздел шторки выбора: заголовок и строки. [onClick] = `null` — строки раздела только показываются
 * (например, уже подключённые).
 */
@Immutable
data class SettingsPickSection<T : Any>(
    val title: String,
    val items: List<T>,
    val onClick: ((T) -> Unit)? = null,
)

/**
 * Шторка выбора из каталога с поиском сверху (добавить язык и т. п.).
 * Выбор не закрывает шторку: строка переезжает между разделами, и можно выбрать ещё.
 * Пустые после фильтрации разделы не показываются.
 *
 * @param matches подходит ли строка под запрос; запрос уже обрезан, не пустой.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SettingsPickSheet(
    title: String,
    searchPlaceholder: String,
    sections: List<SettingsPickSection<T>>,
    key: (T) -> Any,
    matches: (item: T, query: String) -> Boolean,
    onDismiss: () -> Unit,
    rowHeight: Dp = SettingsListTallRowHeight,
    row: @Composable RowScope.(T) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val query = remember { mutableStateOf("") }
    val currentSections by rememberUpdatedState(sections)
    val currentMatches by rememberUpdatedState(matches)

    // Фильтр пересчитывается только при смене запроса или самих разделов, а не на каждую рекомпозицию шторки.
    val visibleSections by remember {
        derivedStateOf {
            val text = query.value.trim()
            currentSections
                .map { section ->
                    if (text.isEmpty()) section else section.copy(items = section.items.filter { currentMatches(it, text) })
                }
                .filter { it.items.isNotEmpty() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            ProbeTextField(
                placeholder = searchPlaceholder,
                containerColor = MaterialTheme.appColors.cardMuted,
                cursorColor = MaterialTheme.colorScheme.primary,
                textStyle = TextStyle(fontSize = 14.sp),
                placeholderColor = MaterialTheme.colorScheme.outline,
                shape = SearchShape,
                contentPadding = PaddingValues(horizontal = 16.dp),
                singleLine = true,
                modifier = Modifier.height(46.dp),
                leading = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp),
                    )
                },
                onTextChange = { query.value = it },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                visibleSections.forEach { section ->
                    key(section.title) {
                        Column {
                            SettingsSectionHeader(title = section.title)
                            SettingsCheckList(
                                items = section.items,
                                key = key,
                                rowHeight = rowHeight,
                                onClick = section.onClick,
                                row = row,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val SearchShape = RoundedCornerShape(23.dp)
