package kg.timmitof.feature_home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.components.field.ProbeTextField
import kg.timmitof.core.ui.theme.appColors

@Composable
internal fun TryFontsField(
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.appColors

    ProbeTextField(
        placeholder = placeholder,
        containerColor = colors.card,
        cursorColor = colors.brand.solid,
        textStyle = TextStyle(fontSize = 19.sp),
        placeholderColor = MaterialTheme.colorScheme.outline,
        shape = FieldShape,
        borderWidth = 2.dp,
        borderColor = colors.brand.solid,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        autoFocus = true,
        modifier = modifier.heightIn(min = 54.dp),
    )
}

@Composable
internal fun CoachMark(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier.padding(bottom = ArrowSize / 2)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 32.dp, y = ArrowSize / 2)
                .size(ArrowSize)
                .rotate(45f)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.inverseSurface)
        )
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(CoachShape)
                .background(scheme.inverseSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.hintOnInverse,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = body,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = scheme.inverseOnSurface,
            )
        }
    }
}

private val ArrowSize = 14.dp
private val FieldShape = RoundedCornerShape(16.dp)
private val CoachShape = RoundedCornerShape(14.dp)
