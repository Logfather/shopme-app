package de.shopme.ui.illustration.icons.shopicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun MuellerLogo(modifier: Modifier = Modifier) {
    val description = stringResource(id = R.string.mueller_logo_description)
    val muellerOrange = Color(0xFFF3691E)
    val muellerBlack = Color(0xFF000000)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height

        drawMuellerText(width, height, muellerOrange)
        drawFlowerFrame(width, height, muellerOrange)
        drawCenterM(width, height, muellerBlack)
    }
}

private fun DrawScope.drawMuellerText(w: Float, h: Float, color: Color) {
    val textY = h * 0.12f
    val startX = w * 0.11f

    // "Müller" text - simplified but representative shapes
    // M
    val mPath = Path().apply {
        moveTo(startX, textY + h * 0.12f)
        lineTo(startX, textY)
        cubicTo(startX + w * 0.05f, textY - h * 0.01f, startX + w * 0.1f, textY + h * 0.05f, startX + w * 0.12f, textY + h * 0.08f)
        cubicTo(startX + w * 0.14f, textY + h * 0.05f, startX + w * 0.19f, textY - h * 0.01f, startX + w * 0.24f, textY)
        lineTo(startX + w * 0.24f, textY + h * 0.12f)
        lineTo(startX + w * 0.18f, textY + h * 0.12f)
        lineTo(startX + w * 0.18f, textY + h * 0.04f)
        lineTo(startX + w * 0.12f, textY + h * 0.12f)
        lineTo(startX + w * 0.06f, textY + h * 0.04f)
        lineTo(startX + w * 0.06f, textY + h * 0.12f)
        close()
    }
    drawPath(mPath, color)

    // ü
    val uBaseX = startX + w * 0.27f
    val uPath = Path().apply {
        moveTo(uBaseX, textY + h * 0.04f)
        lineTo(uBaseX, textY + h * 0.1f)
        cubicTo(uBaseX, textY + h * 0.13f, uBaseX + w * 0.08f, textY + h * 0.13f, uBaseX + w * 0.08f, textY + h * 0.1f)
        lineTo(uBaseX + w * 0.08f, textY + h * 0.04f)
        lineTo(uBaseX + w * 0.12f, textY + h * 0.12f)
        lineTo(uBaseX + w * 0.08f, textY + h * 0.12f)
        lineTo(uBaseX + w * 0.08f, textY + h * 0.11f)
        cubicTo(uBaseX + w * 0.06f, textY + h * 0.13f, uBaseX, textY + h * 0.13f, uBaseX - w * 0.04f, textY + h * 0.1f)
        lineTo(uBaseX - w * 0.04f, textY + h * 0.04f)
        close()
        // dots
        addOval(Rect(Offset(uBaseX, textY), Size(w * 0.03f, w * 0.03f)))
        addOval(Rect(Offset(uBaseX + w * 0.06f, textY), Size(w * 0.03f, w * 0.03f)))
    }
    drawPath(uPath, color)

    // l l
    val l1X = startX + w * 0.44f
    drawRect(color, Offset(l1X, textY), Size(w * 0.05f, h * 0.12f))
    val l2X = startX + w * 0.52f
    drawRect(color, Offset(l2X, textY), Size(w * 0.05f, h * 0.12f))

    // e
    val eX = startX + w * 0.61f
    val ePath = Path().apply {
        addOval(Rect(eX, textY + h * 0.04f, eX + w * 0.09f, textY + h * 0.12f))
    }
    drawPath(ePath, color)
    drawRect(Color.White, Offset(eX + w * 0.02f, textY + h * 0.07f), Size(w * 0.05f, h * 0.015f))

    // r
    val rX = startX + w * 0.73f
    val rPath = Path().apply {
        moveTo(rX, textY + h * 0.04f)
        lineTo(rX, textY + h * 0.12f)
        lineTo(rX + w * 0.05f, textY + h * 0.12f)
        lineTo(rX + w * 0.05f, textY + h * 0.07f)
        cubicTo(rX + w * 0.05f, textY + h * 0.04f, rX + w * 0.12f, textY + h * 0.04f, rX + w * 0.12f, textY + h * 0.06f)
        lineTo(rX + w * 0.12f, textY + h * 0.04f)
        cubicTo(rX + w * 0.12f, textY + h * 0.01f, rX, textY + h * 0.01f, rX, textY + h * 0.04f)
        close()
    }
    drawPath(rPath, color)
}

private fun DrawScope.drawFlowerFrame(w: Float, h: Float, color: Color) {
    val cx = w * 0.5f
    val cy = h * 0.62f
    val size = w * 0.75f
    val half = size / 2f
    
    val path = Path().apply {
        moveTo(cx, cy - half)
        cubicTo(cx + half * 0.8f, cy - half, cx + half, cy - half * 0.8f, cx + half, cy)
        cubicTo(cx + half, cy + half * 0.8f, cx + half * 0.8f, cy + half, cx, cy + half)
        cubicTo(cx - half * 0.8f, cy + half, cx - half, cy + half * 0.8f, cx - half, cy)
        cubicTo(cx - half, cy - half * 0.8f, cx - half * 0.8f, cy - half, cx, cy - half)
        
        val innerSize = size * 0.75f
        val ih = innerSize / 2f
        moveTo(cx, cy - ih)
        cubicTo(cx - ih * 0.5f, cy - ih, cx - ih, cy - ih * 0.5f, cx - ih, cy)
        cubicTo(cx - ih, cy + ih * 0.5f, cx - ih * 0.5f, cy + ih, cx, cy + ih)
        cubicTo(cx + ih * 0.5f, cy + ih, cx + ih, cy + ih * 0.5f, cx + ih, cy)
        cubicTo(cx + ih, cy - ih * 0.5f, cx + ih * 0.5f, cy - ih, cx, cy - ih)
    }
    drawPath(path, color)
}

private fun DrawScope.drawCenterM(w: Float, h: Float, color: Color) {
    val cx = w * 0.5f
    val cy = h * 0.62f
    val mw = w * 0.35f
    val mh = h * 0.3f
    
    val path = Path().apply {
        moveTo(cx - mw * 0.5f, cy + mh * 0.5f)
        lineTo(cx - mw * 0.5f, cy - mh * 0.5f)
        lineTo(cx - mw * 0.2f, cy - mh * 0.5f)
        lineTo(cx, cy - mh * 0.1f)
        lineTo(cx + mw * 0.2f, cy - mh * 0.5f)
        lineTo(cx + mw * 0.5f, cy - mh * 0.5f)
        lineTo(cx + mw * 0.5f, cy + mh * 0.5f)
        lineTo(cx + mw * 0.25f, cy + mh * 0.5f)
        lineTo(cx + mw * 0.25f, cy - mh * 0.1f)
        lineTo(cx, cy + mh * 0.2f)
        lineTo(cx - mw * 0.25f, cy - mh * 0.1f)
        lineTo(cx - mw * 0.25f, cy + mh * 0.5f)
        close()
    }
    drawPath(path, color)
}

@Preview(showBackground = true)
@Composable
private fun MuellerLogoPreview() {
    ShopMeTheme {
        MuellerLogo(
            modifier = Modifier
                .padding(16.dp)
                .size(300.dp)
        )
    }
}
