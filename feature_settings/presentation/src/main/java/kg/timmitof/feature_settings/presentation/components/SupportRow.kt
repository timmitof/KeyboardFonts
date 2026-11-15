package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.components.brand.AccentIconTile
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.core.ui.R as CoreR

@Composable
internal fun SupportRow(
    isSupporter: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(RowShape)
            .background(MaterialTheme.appColors.card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentIconTile(
            icon = painterResource(R.drawable.ic_tip_jar),
            role = if (isSupporter) AccentRole.APPEARANCE else AccentRole.HINT,
            iconSize = 20.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.support_row_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier.padding(top = 1.dp),
                text = stringResource(if (isSupporter) R.string.support_row_subtitle_done else R.string.support_row_subtitle),
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Icon(
            painter = painterResource(CoreR.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp),
        )
    }
}

private val RowShape = RoundedCornerShape(20.dp)
