package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.AgeDefinition
import com.example.model.BaseTower
import com.example.model.CombatUnit
import com.example.model.FloatingCombatText
import com.example.model.Particle
import com.example.model.Projectile
import kotlin.math.sin

@Composable
fun BattlefieldCanvas(
    modifier: Modifier = Modifier,
    age: AgeDefinition,
    playerBase: BaseTower,
    enemyBase: BaseTower,
    units: List<CombatUnit>,
    projectiles: List<Projectile>,
    floatingTexts: List<FloatingCombatText>,
    particles: List<Particle>,
    isFrozen: Boolean,
    isMoraleBoosted: Boolean,
    meteorEffectProgress: Float
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val groundY = height * 0.76f

        // 1. Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(age.skyColorTop), Color(age.skyColorBottom)),
                startY = 0f,
                endY = groundY
            ),
            size = Size(width, groundY)
        )

        // 2. Distant Hills / Parallax Backdrop
        val hillPath = Path().apply {
            moveTo(0f, groundY - 60f)
            cubicTo(
                width * 0.25f, groundY - 110f,
                width * 0.50f, groundY - 40f,
                width * 0.75f, groundY - 100f
            )
            lineTo(width, groundY - 50f)
            lineTo(width, groundY)
            lineTo(0f, groundY)
            close()
        }
        drawPath(
            path = hillPath,
            color = Color(age.groundColor).copy(alpha = 0.35f)
        )

        // 3. Ground / Battlefield Lane
        drawRect(
            color = Color(age.groundColor),
            topLeft = Offset(0f, groundY),
            size = Size(width, height - groundY)
        )
        // Lane Top Border / Pathway
        drawLine(
            color = Color(0x33000000),
            start = Offset(0f, groundY),
            end = Offset(width, groundY),
            strokeWidth = 3f
        )
        // Road striping / pebbles
        for (i in 0..12) {
            val px = (i * width / 12f) + 15f
            drawOval(
                color = Color.Black.copy(alpha = 0.12f),
                topLeft = Offset(px, groundY + 14f),
                size = Size(14f, 5f)
            )
        }

        // 4. Bases
        UnitDrawers.drawBaseBuilding(
            drawScope = this,
            isPlayer = true,
            ageIndex = age.index,
            canvasWidth = width,
            groundY = groundY,
            currentHp = playerBase.currentHp,
            maxHp = playerBase.maxHp
        )
        UnitDrawers.drawBaseBuilding(
            drawScope = this,
            isPlayer = false,
            ageIndex = age.index,
            canvasWidth = width,
            groundY = groundY,
            currentHp = enemyBase.currentHp,
            maxHp = enemyBase.maxHp
        )

        // Base HP text overlays
        drawContext.canvas.nativeCanvas.apply {
            val baseTextPaint = android.graphics.Paint().apply {
                textSize = 24f
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
            }
            // Player Base Text
            baseTextPaint.color = android.graphics.Color.parseColor("#90CAF9")
            drawText("${playerBase.currentHp.toInt()} / ${playerBase.maxHp.toInt()} HP", 55f, groundY - 145f, baseTextPaint)

            // Enemy Base Text
            baseTextPaint.color = android.graphics.Color.parseColor("#EF9A9A")
            drawText("${enemyBase.currentHp.toInt()} / ${enemyBase.maxHp.toInt()} HP", width - 55f, groundY - 145f, baseTextPaint)
        }

        // 5. Units (sorted by Y/role so larger units stay behind or front cleanly)
        val sortedUnits = units.sortedBy { it.blueprint.role.ordinal }
        for (unit in sortedUnits) {
            UnitDrawers.drawUnit(
                drawScope = this,
                unit = unit,
                canvasWidth = width,
                groundY = groundY,
                ageIndex = age.index
            )
        }

        // 6. Projectiles
        for (proj in projectiles) {
            UnitDrawers.drawProjectile(
                drawScope = this,
                projectile = proj,
                canvasWidth = width,
                groundY = groundY
            )
        }

        // 7. Particles (dust, explosions, sparks)
        for (p in particles) {
            drawCircle(
                color = Color(p.color).copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x * width, groundY + p.y)
            )
        }

        // 8. Skill Visual FX
        if (meteorEffectProgress > 0f) {
            // Falling meteors across enemy lane
            for (m in 0..4) {
                val mx = width * (0.55f + m * 0.08f)
                val my = (1f - meteorEffectProgress) * groundY + m * 15f
                drawLine(
                    color = Color(0xFFFF5722),
                    start = Offset(mx - 40f, my - 60f),
                    end = Offset(mx, my),
                    strokeWidth = 6f
                )
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = 12f * meteorEffectProgress,
                    center = Offset(mx, my)
                )
            }
        }

        if (isFrozen) {
            // Icy blue frost tint across enemy side
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color(0x6600E5FF)),
                    startX = width * 0.35f,
                    endX = width
                ),
                topLeft = Offset(0f, 0f),
                size = Size(width, groundY + 40f)
            )
        }

        if (isMoraleBoosted) {
            // Golden speed rays across player units
            for (ray in 0..6) {
                val rx = width * (0.05f + ray * 0.08f)
                drawLine(
                    color = Color(0x66FFD700),
                    start = Offset(rx, groundY - 45f),
                    end = Offset(rx + 25f, groundY - 10f),
                    strokeWidth = 3f
                )
            }
        }

        // 9. Floating Combat Text
        for (txt in floatingTexts) {
            val textX = txt.x * width
            val textY = groundY - 72f - txt.yOffset

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = if (txt.isPlayerDamage) {
                        if (txt.isCrit) android.graphics.Color.parseColor("#FFD600")
                        else android.graphics.Color.parseColor("#FF5252")
                    } else {
                        android.graphics.Color.WHITE
                    }
                    textSize = if (txt.isCrit) 34f else 26f
                    isFakeBoldText = true
                    alpha = (txt.alpha * 255).toInt().coerceIn(0, 255)
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                }
                drawText(txt.text, textX, textY, paint)
            }
        }
    }
}
