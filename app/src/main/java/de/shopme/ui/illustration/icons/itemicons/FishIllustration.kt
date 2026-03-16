package de.shopme.ui.illustration.icons.itemicons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.shopme.R
import de.shopme.ui.theme.ShopMeTheme

@Composable
fun FishIllustration(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.fish_illustration_description)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Lettuce base
        LettuceBed(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .align(Alignment.Center)
                .offset(y = 20.dp)
        )

        // Lemon slice behind fish (top right)
        LemonSlice(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 60.dp)
                .rotate(15f),
            isFull = true
        )

        // Tomatoes (bottom left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 60.dp)
        ) {
            Tomato(modifier = Modifier.size(70.dp).offset(x = 0.dp, y = 20.dp))
            Tomato(modifier = Modifier.size(85.dp).offset(x = 50.dp, y = 35.dp))
        }

        // Lemon slices on lettuce
        LemonSlice(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomCenter)
                .offset(x = 30.dp, y = (-40).dp)
                .rotate(30f)
        )
        LemonSlice(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-60).dp, y = (-80).dp)
                .rotate(-20f)
        )

        // Fish Skeleton (at the bottom center)
        FishSkeleton(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(90.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
                .rotate(-5f)
        )

        // Main Fish
        Fish(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .align(Alignment.Center)
                .offset(y = (-10).dp)
        )

        // Water Droplets (top left)
        WaterDroplets(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopStart)
                .offset(x = 30.dp, y = 30.dp)
        )
    }
}

@Composable
private fun LettuceBed(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.45f
            val points = 18
            for (i in 0 until points) {
                val angle = i * (360f / points)
                val nextAngle = (i + 1) * (360f / points)
                val midAngle = (angle + nextAngle) / 2f
                
                val r1 = radius + (if (i % 2 == 0) 15f else -10f)
                val r2 = radius + (if ((i + 1) % 2 == 0) 15f else -10f)
                val rMid = radius + 40f
                
                val x1 = centerX + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * r1
                val y1 = centerY + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * r1
                val xMid = centerX + Math.cos(Math.toRadians(midAngle.toDouble())).toFloat() * rMid
                val yMid = centerY + Math.sin(Math.toRadians(midAngle.toDouble())).toFloat() * rMid
                val x2 = centerX + Math.cos(Math.toRadians(nextAngle.toDouble())).toFloat() * r2
                val y2 = centerY + Math.sin(Math.toRadians(nextAngle.toDouble())).toFloat() * r2
                
                if (i == 0) moveTo(x1, y1)
                quadraticTo(xMid, yMid, x2, y2)
            }
            close()
        }
        drawPath(path, Color(0xFF8BC34A))
        drawPath(path, Color.Black, style = Stroke(width = 4f))
        
        // Lettuce details
        for (i in 0 until 8) {
            rotate(i * 45f) {
                drawLine(
                    color = Color(0xFF33691E).copy(alpha = 0.3f),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, size.height * 0.15f),
                    strokeWidth = 3f
                )
            }
        }
    }
}

@Composable
private fun Tomato(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F)),
                center = center.minus(Offset(radius * 0.3f, radius * 0.3f)),
                radius = radius * 1.5f
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = center,
            style = Stroke(width = 4f)
        )
        
        // Stem
        val stemPath = Path().apply {
            moveTo(center.x, center.y - radius)
            for (i in 0 until 5) {
                val angle = i * 72f - 90f
                val rOut = radius * 0.45f
                val rIn = radius * 0.15f
                
                val x1 = center.x + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * rOut
                val y1 = (center.y - radius) + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * rOut
                
                val midAngle = angle + 36f
                val x2 = center.x + Math.cos(Math.toRadians(midAngle.toDouble())).toFloat() * rIn
                val y2 = (center.y - radius) + Math.sin(Math.toRadians(midAngle.toDouble())).toFloat() * rIn
                
                lineTo(x1, y1)
                lineTo(x2, y2)
            }
            close()
        }
        drawPath(stemPath, Color(0xFF2E7D32))
        drawPath(stemPath, Color.Black, style = Stroke(width = 3f))
    }
}

@Composable
private fun LemonSlice(
    modifier: Modifier = Modifier,
    isFull: Boolean = false
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        
        if (isFull) {
            drawCircle(Color(0xFFFFEB3B), radius = radius)
            drawCircle(Color.Black, radius = radius, style = Stroke(width = 4f))
            drawCircle(Color.White, radius = radius * 0.9f, style = Stroke(width = 6f))
            
            for (i in 0 until 8) {
                rotate(i * 45f + 22.5f) {
                    val segmentPath = Path().apply {
                        moveTo(center.x, center.y)
                        val r = radius * 0.85f
                        val angle = 18f
                        val x1 = center.x + Math.cos(Math.toRadians((-angle).toDouble())).toFloat() * r
                        val y1 = center.y + Math.sin(Math.toRadians((-angle).toDouble())).toFloat() * r
                        val x2 = center.x + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * r
                        val y2 = center.y + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * r
                        lineTo(x1, y1)
                        quadraticTo(center.x + r + 5f, center.y, x2, y2)
                        close()
                    }
                    drawPath(segmentPath, Color(0xFFFDD835))
                }
            }
        } else {
            drawArc(
                color = Color(0xFFFFEB3B),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            drawArc(
                color = Color.Black,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 4f)
            )
            drawLine(Color.Black, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 4f)
            
            // Segments
            for (i in 1 until 5) {
                val angle = i * 36f
                val x = center.x + Math.cos(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.85f
                val y = center.y + Math.sin(Math.toRadians(angle.toDouble())).toFloat() * radius * 0.85f
                drawLine(Color.White, center, Offset(x, y), strokeWidth = 4f)
            }
        }
    }
}

@Composable
private fun Fish(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bodyWidth = size.width * 0.8f
            val bodyHeight = size.height * 0.6f
            val center = Offset(size.width * 0.45f, size.height * 0.5f)
            
            // Tail
            val tailPath = Path().apply {
                moveTo(center.x + bodyWidth * 0.3f, center.y)
                lineTo(size.width * 0.98f, center.y - bodyHeight * 0.45f)
                quadraticTo(size.width * 0.85f, center.y, size.width * 0.98f, center.y + bodyHeight * 0.45f)
                close()
            }
            drawPath(tailPath, Color(0xFF1976D2))
            drawPath(tailPath, Color.Black, style = Stroke(width = 5f))
            
            // Tail detail
            for (i in 0 until 5) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.2f),
                    start = Offset(center.x + bodyWidth * 0.35f, center.y - 15f + i * 8f),
                    end = Offset(size.width * 0.95f, center.y - 40f + i * 20f),
                    strokeWidth = 2f
                )
            }

            // Top Fin
            val topFin = Path().apply {
                moveTo(center.x - bodyWidth * 0.15f, center.y - bodyHeight * 0.45f)
                quadraticTo(center.x + bodyWidth * 0.1f, center.y - bodyHeight * 0.9f, center.x + bodyWidth * 0.4f, center.y - bodyHeight * 0.35f)
                close()
            }
            drawPath(topFin, Color(0xFF1565C0))
            drawPath(topFin, Color.Black, style = Stroke(width = 5f))

            // Body
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D47A1), Color(0xFF1E88E5), Color.White),
                startY = center.y - bodyHeight / 2,
                endY = center.y + bodyHeight / 2
            )
            drawOval(
                brush = bodyBrush,
                topLeft = Offset(center.x - bodyWidth / 2, center.y - bodyHeight / 2),
                size = Size(bodyWidth, bodyHeight)
            )
            drawOval(
                color = Color.Black,
                topLeft = Offset(center.x - bodyWidth / 2, center.y - bodyHeight / 2),
                size = Size(bodyWidth, bodyHeight),
                style = Stroke(width = 6f)
            )
            
            // Side Fin
            val sideFin = Path().apply {
                moveTo(center.x + bodyWidth * 0.1f, center.y + bodyHeight * 0.1f)
                quadraticTo(center.x + bodyWidth * 0.45f, center.y + bodyHeight * 0.25f, center.x + bodyWidth * 0.25f, center.y + bodyHeight * 0.45f)
                close()
            }
            drawPath(sideFin, Color(0xFF1565C0))
            drawPath(sideFin, Color.Black, style = Stroke(width = 4f))

            // Scales
            for (i in 0..7) {
                for (j in 0..5) {
                    val x = center.x + i * 30f - 60f
                    val y = center.y + j * 25f - 50f
                    if (Rect(center.x - bodyWidth / 2, center.y - bodyHeight / 2, center.x + bodyWidth / 2, center.y + bodyHeight / 2).contains(Offset(x, y))) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.3f),
                            startAngle = 130f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(x, y),
                            size = Size(25f, 25f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        }
        
        FishFace(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .offset(x = (-40).dp, y = 10.dp)
        )
    }
}

@Composable
private fun FishFace(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FishEye(eyeSize = 55.dp)
            FishEye(eyeSize = 55.dp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        FishMouth(modifier = Modifier.size(70.dp))
    }
}

@Composable
private fun FishEye(eyeSize: Dp) {
    Box(
        modifier = Modifier
            .size(eyeSize)
            .background(Color.White, CircleShape)
            .border(5.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pupilRadius = size.minDimension * 0.4f
            drawCircle(
                color = Color.Black,
                radius = pupilRadius,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = pupilRadius * 0.4f,
                center = center.minus(Offset(pupilRadius * 0.3f, pupilRadius * 0.3f))
            )
        }
    }
}

@Composable
private fun FishMouth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.2f)
            quadraticTo(size.width * 0.5f, size.height * 1.2f, size.width * 0.9f, size.height * 0.2f)
            quadraticTo(size.width * 0.5f, size.height * 0.1f, size.width * 0.1f, size.height * 0.2f)
            close()
        }
        drawPath(path, Color(0xFFB71C1C))
        drawPath(path, Color.Black, style = Stroke(width = 5f))
        
        // Tongue
        val tonguePath = Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.7f)
            quadraticTo(size.width * 0.5f, size.height * 1.0f, size.width * 0.65f, size.height * 0.7f)
        }
        drawPath(tonguePath, Color(0xFFFF5252))
    }
}

@Composable
private fun FishSkeleton(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color.Black
        val fillColor = Color(0xFFF5F5F5)
        val strokeWidth = 5f
        
        // Skull
        val skullPath = Path().apply {
            moveTo(70f, size.height / 2)
            cubicTo(50f, -10f, 0f, -10f, 0f, size.height / 2)
            cubicTo(0f, size.height + 10f, 50f, size.height + 10f, 70f, size.height / 2)
            close()
        }
        drawPath(skullPath, fillColor)
        drawPath(skullPath, strokeColor, style = Stroke(width = strokeWidth))
        
        // Skull Eye Hole
        drawCircle(strokeColor, radius = 12f, center = Offset(25f, size.height * 0.4f))
        
        // Spine
        drawLine(
            color = strokeColor,
            start = Offset(70f, size.height / 2),
            end = Offset(size.width * 0.9f, size.height / 2),
            strokeWidth = strokeWidth
        )
        
        // Ribs
        for (i in 0 until 5) {
            val x = 100f + i * 35f
            drawLine(
                color = strokeColor,
                start = Offset(x, size.height * 0.25f),
                end = Offset(x, size.height * 0.75f),
                strokeWidth = strokeWidth
            )
        }
        
        // Tail bone
        val tailBone = Path().apply {
            moveTo(size.width * 0.9f, size.height / 2)
            lineTo(size.width, size.height * 0.2f)
            lineTo(size.width, size.height * 0.8f)
            close()
        }
        drawPath(tailBone, fillColor)
        drawPath(tailBone, strokeColor, style = Stroke(width = strokeWidth))
    }
}

@Composable
private fun WaterDroplets(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val dropletColor = Color(0xFF4FC3F7)
        val strokeColor = Color.Black
        
        fun drawDroplet(offset: Offset, scale: Float, rotation: Float) {
            rotate(rotation, offset) {
                val path = Path().apply {
                    moveTo(offset.x, offset.y - 20f * scale)
                    cubicTo(offset.x + 15f * scale, offset.y + 10f * scale, offset.x + 10f * scale, offset.y + 25f * scale, offset.x, offset.y + 25f * scale)
                    cubicTo(offset.x - 10f * scale, offset.y + 25f * scale, offset.x - 15f * scale, offset.y + 10f * scale, offset.x, offset.y - 20f * scale)
                    close()
                }
                drawPath(path, dropletColor)
                drawPath(path, strokeColor, style = Stroke(width = 4f))
                
                // Highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 4f * scale,
                    center = Offset(offset.x - 5f * scale, offset.y + 5f * scale)
                )
            }
        }
        
        drawDroplet(Offset(size.width * 0.2f, size.height * 0.8f), 1.2f, -30f)
        drawDroplet(Offset(size.width * 0.5f, size.height * 0.4f), 1.6f, 10f)
        drawDroplet(Offset(size.width * 0.8f, size.height * 0.7f), 1.1f, 40f)
    }
}

@Preview(showBackground = true)
@Composable
private fun FishIllustrationPreview() {
    ShopMeTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            FishIllustration()
        }
    }
}
