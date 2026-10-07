package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kg.timmitof.keyboard.domain.model.PhotoCrop
import kg.timmitof.keyboard.presentation.preview.drawKeyboardSilhouette
import kg.timmitof.keyboard.presentation.theme.KeyboardColorScheme
import java.io.File
import kotlin.math.max

/** Положение фото в рамке; пересчитывается в долю картинки только по «Готово». */
@Stable
class PhotoCropState {

    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    internal var frame = Size.Zero
        set(value) {
            field = value
            restorePending()
        }
    internal var image = Size.Zero
        set(value) {
            field = value
            restorePending()
        }

    private var pendingCrop: PhotoCrop? = null

    /** Фото всегда закрывает рамку целиком: ни приблизить меньше рамки, ни увести край внутрь. */
    internal fun transform(pan: Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(1f, MaxZoom)
        val limit = overflow()
        offset = Offset(
            (offset.x + pan.x).coerceIn(-limit.width, limit.width),
            (offset.y + pan.y).coerceIn(-limit.height, limit.height),
        )
    }

    /** Сохранённый кадр восстанавливается, как только известны и рамка, и картинка. */
    internal fun restore(crop: PhotoCrop) {
        scale = 1f
        offset = Offset.Zero
        pendingCrop = crop
        restorePending()
    }

    private fun restorePending() {
        val crop = pendingCrop ?: return
        if (frame.isEmpty() || image.isEmpty()) return
        pendingCrop = null

        val cover = coverScale()
        val window = Size((crop.right - crop.left) * image.width, (crop.bottom - crop.top) * image.height)
        if (window.isEmpty()) return

        scale = (max(frame.width / window.width, frame.height / window.height) / cover).coerceIn(1f, MaxZoom)

        val center = Offset(frame.width / 2, frame.height / 2)
        val windowCenter = Offset(
            (frame.width - image.width * cover) / 2 + (crop.left + crop.right) / 2 * image.width * cover,
            (frame.height - image.height * cover) / 2 + (crop.top + crop.bottom) / 2 * image.height * cover,
        )
        offset = (center - windowCenter) * scale
        transform(pan = Offset.Zero, zoom = 1f)
    }

    fun crop(): PhotoCrop? {
        if (frame.isEmpty() || image.isEmpty()) return null

        val cover = coverScale()
        val center = Offset(frame.width / 2, frame.height / 2)
        val drawnLeft = (frame.width - image.width * cover) / 2
        val drawnTop = (frame.height - image.height * cover) / 2

        // Углы рамки — в координатах картинки до слоя масштаба, затем в долях исходного фото.
        fun toImage(point: Offset): Offset {
            val unscaled = center + (point - center - offset) / scale
            return Offset((unscaled.x - drawnLeft) / cover / image.width, (unscaled.y - drawnTop) / cover / image.height)
        }

        val topLeft = toImage(Offset.Zero)
        val bottomRight = toImage(Offset(frame.width, frame.height))
        return PhotoCrop(
            left = topLeft.x.coerceIn(0f, 1f),
            top = topLeft.y.coerceIn(0f, 1f),
            right = bottomRight.x.coerceIn(0f, 1f),
            bottom = bottomRight.y.coerceIn(0f, 1f),
        )
    }

    /**
     * Рисует фото целиком, без предварительной обрезки по рамке, по той же формуле, что и [crop]:
     * что видно в рамке — ровно то, что окажется на клавиатуре.
     */
    internal fun DrawScope.drawPhoto(painter: Painter) {
        val source = painter.intrinsicSize
        if (source.isUnspecified || source.isEmpty()) return

        val zoom = max(size.width / source.width, size.height / source.height) * scale
        val left = size.width / 2 + offset.x - source.width * zoom / 2
        val top = size.height / 2 + offset.y - source.height * zoom / 2

        translate(left = left, top = top) {
            scale(zoom, pivot = Offset.Zero) {
                with(painter) { draw(source) }
            }
        }
    }

    private fun coverScale() = max(frame.width / image.width, frame.height / image.height)

    private fun overflow(): Size {
        if (frame.isEmpty() || image.isEmpty()) return Size.Zero
        val cover = coverScale() * scale
        return Size(
            ((image.width * cover - frame.width) / 2).coerceAtLeast(0f),
            ((image.height * cover - frame.height) / 2).coerceAtLeast(0f),
        )
    }

    private companion object {
        const val MaxZoom = 5f
    }
}

/**
 * Рамка в пропорциях настоящей клавиатуры ([keyboardHeight] при ширине экрана [screenWidth]),
 * поверх фото — полупрозрачные клавиши, чтобы было видно, что окажется под ними.
 */
@Composable
internal fun PhotoCropFrame(
    path: String,
    initialCrop: PhotoCrop,
    state: PhotoCropState,
    keyboardHeight: Dp,
    screenWidth: Dp,
    keyColors: KeyboardColorScheme,
    errorText: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current
    val request = remember(path) { ImageRequest.Builder(context).data(File(path)).size(MaxDecodeSide).build() }
    val painter = rememberAsyncImagePainter(request)
    val painterState by painter.state.collectAsState()
    val ratio = screenWidth / keyboardHeight

    LaunchedEffect(path, initialCrop) { state.restore(initialCrop) }
    // Размер картинки сообщаем состоянию вне композиции — по факту загрузки.
    LaunchedEffect(painter) {
        painter.state.collect { loaded ->
            if (loaded is AsyncImagePainter.State.Success) state.image = painter.intrinsicSize
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(FrameShape)
            .onSizeChanged { size: IntSize -> state.frame = size.toSize() }
            .pointerInput(state) {
                detectTransformGestures { _, pan, zoom, _ -> state.transform(pan, zoom) }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { with(state) { drawPhoto(painter) } },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val unit = size.height / keyboardHeight.toPx()
                    drawKeyboardSilhouette(
                        colors = keyColors,
                        padding = (SilhouettePadding * unit),
                        gap = (SilhouetteGap * unit),
                        radius = (SilhouetteRadius * unit),
                        topInset = (SilhouetteTopBar * unit),
                        alpha = SilhouetteAlpha,
                    )
                },
        )

        when (painterState) {
            is AsyncImagePainter.State.Loading, AsyncImagePainter.State.Empty -> CircularProgressIndicator()
            is AsyncImagePainter.State.Error -> Text(
                text = errorText,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(24.dp),
            )
            else -> Unit
        }
    }
}

private val FrameShape = RoundedCornerShape(14.dp)
private const val MaxDecodeSide = 2048

private val SilhouettePadding = 8.dp
private val SilhouetteGap = 6.dp
private val SilhouetteRadius = 11.dp
private val SilhouetteTopBar = 44.dp
private const val SilhouetteAlpha = 0.72f
