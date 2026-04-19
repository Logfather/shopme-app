package de.shopme.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.unit.dp
import de.shopme.ui.theme.BrandBlack

val CheckFlagIcon: ImageVector
    get() {
        if (_checkFlagIcon != null) {
            return _checkFlagIcon!!
        }
        _checkFlagIcon = Builder(
            name = "CheckFlagIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {

            path(
                fill = SolidColor(BrandBlack),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 55f)

                curveTo(25f, 70f, 35f, 80f, 45f, 75f)
                curveTo(55f, 70f, 70f, 40f, 85f, 25f)
                curveTo(75f, 20f, 60f, 35f, 45f, 60f)
                curveTo(35f, 75f, 30f, 65f, 20f, 55f)

                close()
            }

        }.build()

        return _checkFlagIcon!!
    }

private var _checkFlagIcon: ImageVector? = null