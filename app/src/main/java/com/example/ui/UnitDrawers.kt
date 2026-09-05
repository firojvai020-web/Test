package com.example.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.CombatUnit
import com.example.model.Projectile
import com.example.model.ProjectileType
import com.example.model.UnitRole
import kotlin.math.PI
import kotlin.math.sin

object UnitDrawers {

    fun drawUnit(
        drawScope: DrawScope,
        unit: CombatUnit,
        canvasWidth: Float,
        groundY: Float,
        ageIndex: Int
    ) {
        val posX = unit.x * canvasWidth
        val direction = if (unit.isPlayer) 1f else -1f

        // Death animation: fade out and rise/tilt
        val alpha = if (unit.isDead) (1f - unit.deathTimer).coerceIn(0f, 1f) else 1f
        if (alpha <= 0f) return

        val unitScale = if (unit.blueprint.role == UnitRole.HEAVY) 2.8f else 2.35f
        val feetY = 18f
        val bobbing = if (unit.isDead) 0f else sin(unit.walkAnimCycle * 2 * PI.toFloat()) * 4f
        val posY = groundY - feetY + bobbing - (if (unit.isDead) unit.deathTimer * 30f else 0f)

        drawScope.withTransform({
            translate(left = posX, top = posY)
            scale(scaleX = direction * unitScale, scaleY = unitScale, pivot = Offset(0f, feetY))
            if (unit.isDead) {
                rotate(degrees = 75f * unit.deathTimer, pivot = Offset(0f, feetY))
            }
        }) {
            // Shadow under feet
            drawOval(
                color = Color.Black.copy(alpha = 0.32f * alpha),
                topLeft = Offset(-15f, 15f),
                size = Size(30f, 8f)
            )

            // Draw unit according to Role and Age
            when (unit.blueprint.role) {
                UnitRole.MELEE -> drawMeleeUnit(this, unit, ageIndex, alpha)
                UnitRole.RANGED -> drawRangedUnit(this, unit, ageIndex, alpha)
                UnitRole.HEAVY -> drawHeavyUnit(this, unit, ageIndex, alpha)
            }
        }

        // Health Bar above unit
        if (!unit.isDead && unit.currentHp < unit.maxHp) {
            val barWidth = 52f
            val barHeight = 6f
            val barX = posX - barWidth / 2
            val barY = groundY - (if (unit.blueprint.role == UnitRole.HEAVY) 115f else 95f) + bobbing

            drawScope.drawRoundRect(
                color = Color(0xDD000000),
                topLeft = Offset(barX - 1.5f, barY - 1.5f),
                size = Size(barWidth + 3f, barHeight + 3f),
                cornerRadius = CornerRadius(3f, 3f)
            )

            val hpRatio = (unit.currentHp / unit.maxHp).coerceIn(0f, 1f)
            val hpColor = when {
                hpRatio > 0.5f -> Color(0xFF4CAF50)
                hpRatio > 0.25f -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }

            drawScope.drawRoundRect(
                color = hpColor,
                topLeft = Offset(barX, barY),
                size = Size(barWidth * hpRatio, barHeight),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
        }
    }

    private fun drawMeleeUnit(
        drawScope: DrawScope,
        unit: CombatUnit,
        ageIndex: Int,
        alpha: Float
    ) {
        val swing = if (unit.isAttacking) sin(unit.attackAnimProgress * PI.toFloat()) * 40f else 0f
        val teamColor = if (unit.isPlayer) Color(0xFF2196F3) else Color(0xFFE53935)

        // Body / Armor
        val armorColor = when (ageIndex) {
            0 -> Color(0xFF8D6E63) // Stone Age fur
            1 -> Color(0xFFFFB300) // Spartan bronze
            2 -> Color(0xFFB0BEC5) // Medieval chainmail
            3 -> Color(0xFF3F51B5) // Renaissance coat
            4 -> Color(0xFF37474F) // Modern camo
            else -> Color(0xFF00E5FF) // Cyber neon
        }

        // Legs
        val legWalk = sin(unit.walkAnimCycle * 2 * PI.toFloat()) * 5f
        drawScope.drawLine(
            color = Color(0xFF3E2723).copy(alpha = alpha),
            start = Offset(-4f, 8f),
            end = Offset(-4f - legWalk, 18f),
            strokeWidth = 3.5f
        )
        drawScope.drawLine(
            color = Color(0xFF3E2723).copy(alpha = alpha),
            start = Offset(4f, 8f),
            end = Offset(4f + legWalk, 18f),
            strokeWidth = 3.5f
        )

        // Torso
        drawScope.drawRoundRect(
            color = armorColor.copy(alpha = alpha),
            topLeft = Offset(-8f, -4f),
            size = Size(16f, 14f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Belt / Emblem
        drawScope.drawRect(
            color = teamColor.copy(alpha = alpha),
            topLeft = Offset(-8f, 5f),
            size = Size(16f, 3f)
        )

        // Head
        drawScope.drawCircle(
            color = Color(0xFFFFCC80).copy(alpha = alpha),
            radius = 7.5f,
            center = Offset(0f, -12f)
        )

        // Helmet / Hair
        when (ageIndex) {
            0 -> { // Caveman hair band & bone
                drawScope.drawArc(
                    color = Color(0xFF4E342E).copy(alpha = alpha),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-7.5f, -19.5f),
                    size = Size(15f, 15f)
                )
            }
            1 -> { // Spartan Helmet with red plume
                drawScope.drawArc(
                    color = Color(0xFFFFB300).copy(alpha = alpha),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-8f, -20f),
                    size = Size(16f, 16f)
                )
                drawScope.drawRect(
                    color = Color(0xFFD50000).copy(alpha = alpha),
                    topLeft = Offset(-4f, -25f),
                    size = Size(8f, 6f)
                )
            }
            2 -> { // Medieval Iron Greathelm
                drawScope.drawRoundRect(
                    color = Color(0xFFCFD8DC).copy(alpha = alpha),
                    topLeft = Offset(-8f, -20f),
                    size = Size(16f, 14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Visor slit
                drawScope.drawLine(
                    color = Color.Black.copy(alpha = alpha),
                    start = Offset(-5f, -13f),
                    end = Offset(5f, -13f),
                    strokeWidth = 2f
                )
            }
            3 -> { // Renaissance feathered cavalier hat
                drawScope.drawOval(
                    color = Color(0xFF1A237E).copy(alpha = alpha),
                    topLeft = Offset(-11f, -18f),
                    size = Size(22f, 6f)
                )
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 3f,
                    center = Offset(-6f, -20f)
                )
            }
            4 -> { // Modern Military Helmet & Goggles
                drawScope.drawArc(
                    color = Color(0xFF2E7D32).copy(alpha = alpha),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(-8.5f, -20f),
                    size = Size(17f, 16f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFFFEB3B).copy(alpha = alpha),
                    topLeft = Offset(-5f, -14f),
                    size = Size(10f, 4f),
                    cornerRadius = CornerRadius(1f, 1f)
                )
            }
            else -> { // Cyber visor
                drawScope.drawCircle(
                    color = Color(0xFF263238).copy(alpha = alpha),
                    radius = 7.5f,
                    center = Offset(0f, -12f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF00E5FF).copy(alpha = alpha),
                    topLeft = Offset(-6f, -14f),
                    size = Size(12f, 4f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }

        // Weapon Hand & Weapon (with attack swing rotation)
        drawScope.withTransform({
            translate(left = 6f, top = 2f)
            rotate(degrees = swing, pivot = Offset(0f, 0f))
        }) {
            when (ageIndex) {
                0 -> { // Heavy Stone Club
                    drawLine(
                        color = Color(0xFF5D4037).copy(alpha = alpha),
                        start = Offset(0f, 0f),
                        end = Offset(14f, -10f),
                        strokeWidth = 4f
                    )
                    drawCircle(
                        color = Color(0xFF757575).copy(alpha = alpha),
                        radius = 5.5f,
                        center = Offset(15f, -11f)
                    )
                }
                1 -> { // Spartan Spear
                    drawLine(
                        color = Color(0xFF8D6E63).copy(alpha = alpha),
                        start = Offset(-4f, 6f),
                        end = Offset(20f, -12f),
                        strokeWidth = 3f
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(20f, -12f)
                            lineTo(26f, -16f)
                            lineTo(21f, -8f)
                            close()
                        },
                        color = Color(0xFFFFB300).copy(alpha = alpha)
                    )
                }
                2 -> { // Steel Longsword
                    drawLine(
                        color = Color(0xFFECEFF1).copy(alpha = alpha),
                        start = Offset(0f, 0f),
                        end = Offset(18f, -14f),
                        strokeWidth = 3.5f
                    )
                    // Crossguard
                    drawLine(
                        color = Color(0xFFFFD54F).copy(alpha = alpha),
                        start = Offset(2f, 3f),
                        end = Offset(-2f, -3f),
                        strokeWidth = 2.5f
                    )
                }
                3 -> { // Rapier
                    drawLine(
                        color = Color(0xFFECEFF1).copy(alpha = alpha),
                        start = Offset(0f, 0f),
                        end = Offset(20f, -8f),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = alpha),
                        radius = 4f,
                        center = Offset(2f, 0f),
                        style = Stroke(width = 2f)
                    )
                }
                4 -> { // Tactical Combat Knife / Baton
                    drawLine(
                        color = Color(0xFF212121).copy(alpha = alpha),
                        start = Offset(0f, 0f),
                        end = Offset(14f, -6f),
                        strokeWidth = 3.5f
                    )
                }
                else -> { // Neon Plasma Katana
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = alpha),
                        start = Offset(0f, 0f),
                        end = Offset(20f, -12f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = Offset(2f, -1f),
                        end = Offset(18f, -11f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }

    private fun drawRangedUnit(
        drawScope: DrawScope,
        unit: CombatUnit,
        ageIndex: Int,
        alpha: Float
    ) {
        val teamColor = if (unit.isPlayer) Color(0xFF2196F3) else Color(0xFFE53935)
        val legWalk = sin(unit.walkAnimCycle * 2 * PI.toFloat()) * 4f

        // Legs
        drawScope.drawLine(
            color = Color(0xFF424242).copy(alpha = alpha),
            start = Offset(-4f, 6f),
            end = Offset(-4f - legWalk, 16f),
            strokeWidth = 3f
        )
        drawScope.drawLine(
            color = Color(0xFF424242).copy(alpha = alpha),
            start = Offset(4f, 6f),
            end = Offset(4f + legWalk, 16f),
            strokeWidth = 3f
        )

        // Torso / Tunic
        drawScope.drawRoundRect(
            color = teamColor.copy(alpha = alpha),
            topLeft = Offset(-7f, -4f),
            size = Size(14f, 12f),
            cornerRadius = CornerRadius(2.5f, 2.5f)
        )

        // Head
        drawScope.drawCircle(
            color = Color(0xFFFFCC80).copy(alpha = alpha),
            radius = 6.5f,
            center = Offset(0f, -11f)
        )

        // Cap / Headgear
        val hatColor = when (ageIndex) {
            0 -> Color(0xFF8D6E63)
            1 -> Color(0xFF5D4037)
            2 -> Color(0xFF2E7D32)
            3 -> Color(0xFF1565C0)
            4 -> Color(0xFF37474F)
            else -> Color(0xFF311B92)
        }
        drawScope.drawArc(
            color = hatColor.copy(alpha = alpha),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(-7f, -18f),
            size = Size(14f, 14f)
        )

        // Weapon
        val recoil = if (unit.isAttacking) -3f else 0f
        when (ageIndex) {
            0 -> { // Slingshot
                drawScope.drawLine(
                    color = Color(0xFF6D4C41).copy(alpha = alpha),
                    start = Offset(2f, 0f),
                    end = Offset(10f + recoil, -4f),
                    strokeWidth = 2.5f
                )
                drawScope.drawLine(
                    color = Color(0xFF6D4C41).copy(alpha = alpha),
                    start = Offset(10f + recoil, -4f),
                    end = Offset(14f + recoil, -8f),
                    strokeWidth = 2f
                )
                drawScope.drawLine(
                    color = Color(0xFF6D4C41).copy(alpha = alpha),
                    start = Offset(10f + recoil, -4f),
                    end = Offset(14f + recoil, 0f),
                    strokeWidth = 2f
                )
            }
            1 -> { // Wooden Bow
                drawScope.drawArc(
                    color = Color(0xFF8D6E63).copy(alpha = alpha),
                    startAngle = 270f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(4f + recoil, -14f),
                    size = Size(12f, 24f),
                    style = Stroke(width = 2.5f)
                )
                // Bowstring
                drawScope.drawLine(
                    color = Color.LightGray.copy(alpha = alpha),
                    start = Offset(10f + recoil, -14f),
                    end = Offset(10f + recoil, 10f),
                    strokeWidth = 1f
                )
            }
            2 -> { // Crossbow
                drawScope.drawLine(
                    color = Color(0xFF5D4037).copy(alpha = alpha),
                    start = Offset(0f, 0f),
                    end = Offset(14f + recoil, -1f),
                    strokeWidth = 3f
                )
                drawScope.drawLine(
                    color = Color(0xFFCFD8DC).copy(alpha = alpha),
                    start = Offset(11f + recoil, -7f),
                    end = Offset(11f + recoil, 5f),
                    strokeWidth = 2.5f
                )
            }
            3 -> { // Musket with bayonet
                drawScope.drawLine(
                    color = Color(0xFF4E342E).copy(alpha = alpha),
                    start = Offset(-2f, 2f),
                    end = Offset(18f + recoil, -3f),
                    strokeWidth = 3f
                )
                // Barrel
                drawScope.drawLine(
                    color = Color(0xFF9E9E9E).copy(alpha = alpha),
                    start = Offset(8f + recoil, -1f),
                    end = Offset(24f + recoil, -4f),
                    strokeWidth = 2f
                )
            }
            4 -> { // Rocket Launcher (RPG) on shoulder
                drawScope.drawRoundRect(
                    color = Color(0xFF263238).copy(alpha = alpha),
                    topLeft = Offset(-4f + recoil, -10f),
                    size = Size(24f, 6f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Rocket warhead
                drawScope.drawPath(
                    path = Path().apply {
                        moveTo(20f + recoil, -10f)
                        lineTo(25f + recoil, -7f)
                        lineTo(20f + recoil, -4f)
                        close()
                    },
                    color = Color(0xFFC62828).copy(alpha = alpha)
                )
            }
            else -> { // Plasma Rifle
                drawScope.drawRoundRect(
                    color = Color(0xFF212121).copy(alpha = alpha),
                    topLeft = Offset(0f + recoil, -4f),
                    size = Size(18f, 5f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Glowing plasma chamber
                drawScope.drawRect(
                    color = Color(0xFF00E5FF).copy(alpha = alpha),
                    topLeft = Offset(4f + recoil, -3f),
                    size = Size(8f, 3f)
                )
            }
        }
    }

    private fun drawHeavyUnit(
        drawScope: DrawScope,
        unit: CombatUnit,
        ageIndex: Int,
        alpha: Float
    ) {
        val teamColor = if (unit.isPlayer) Color(0xFF2196F3) else Color(0xFFE53935)
        val legWalk = sin(unit.walkAnimCycle * 2 * PI.toFloat())

        when (ageIndex) {
            0 -> { // Dino Rider!
                // Dino Body
                drawScope.drawOval(
                    color = Color(0xFF43A047).copy(alpha = alpha),
                    topLeft = Offset(-24f, -10f),
                    size = Size(38f, 22f)
                )
                // Dino Head & Jaw
                drawScope.drawOval(
                    color = Color(0xFF388E3C).copy(alpha = alpha),
                    topLeft = Offset(10f, -22f),
                    size = Size(18f, 14f)
                )
                // Dino Eye
                drawScope.drawCircle(
                    color = Color.Yellow.copy(alpha = alpha),
                    radius = 2.5f,
                    center = Offset(20f, -16f)
                )
                // Dino Legs
                val dinoLegF = legWalk * 6f
                drawScope.drawLine(
                    color = Color(0xFF2E7D32).copy(alpha = alpha),
                    start = Offset(-12f, 8f),
                    end = Offset(-12f - dinoLegF, 18f),
                    strokeWidth = 6f
                )
                drawScope.drawLine(
                    color = Color(0xFF2E7D32).copy(alpha = alpha),
                    start = Offset(4f, 8f),
                    end = Offset(4f + dinoLegF, 18f),
                    strokeWidth = 6f
                )
                // Caveman rider on back
                drawScope.drawCircle(
                    color = Color(0xFFFFCC80).copy(alpha = alpha),
                    radius = 5.5f,
                    center = Offset(-6f, -22f)
                )
                drawScope.drawRect(
                    color = teamColor.copy(alpha = alpha),
                    topLeft = Offset(-10f, -16f),
                    size = Size(10f, 8f)
                )
            }
            1 -> { // Spartan War Chariot
                // Chariot Body
                drawScope.drawRoundRect(
                    color = Color(0xFF8D6E63).copy(alpha = alpha),
                    topLeft = Offset(-26f, -12f),
                    size = Size(28f, 20f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Bronze side-shield
                drawScope.drawCircle(
                    color = Color(0xFFFFB300).copy(alpha = alpha),
                    radius = 8f,
                    center = Offset(-12f, -2f)
                )
                // Big rolling wheel
                val wheelAngle = unit.walkAnimCycle * 360f
                drawScope.withTransform({
                    rotate(degrees = wheelAngle, pivot = Offset(-12f, 12f))
                }) {
                    drawCircle(
                        color = Color(0xFF4E342E).copy(alpha = alpha),
                        radius = 8f,
                        center = Offset(-12f, 12f),
                        style = Stroke(width = 3.5f)
                    )
                    drawLine(
                        color = Color(0xFFBCAAA4).copy(alpha = alpha),
                        start = Offset(-20f, 12f),
                        end = Offset(-4f, 12f),
                        strokeWidth = 2f
                    )
                }
                // War horse in front
                drawScope.drawOval(
                    color = Color(0xFF5D4037).copy(alpha = alpha),
                    topLeft = Offset(4f, -14f),
                    size = Size(26f, 18f)
                )
                // Horse head
                drawScope.drawOval(
                    color = Color(0xFF4E342E).copy(alpha = alpha),
                    topLeft = Offset(22f, -22f),
                    size = Size(12f, 14f)
                )
                // Warrior standing in chariot
                drawScope.drawCircle(
                    color = Color(0xFFFFCC80).copy(alpha = alpha),
                    radius = 6f,
                    center = Offset(-16f, -20f)
                )
                drawScope.drawRect(
                    color = teamColor.copy(alpha = alpha),
                    topLeft = Offset(-20f, -14f),
                    size = Size(10f, 8f)
                )
            }
            2 -> { // Armored Mounted Knight on Warhorse
                // Armored Horse
                drawScope.drawOval(
                    color = Color(0xFFB0BEC5).copy(alpha = alpha),
                    topLeft = Offset(-18f, -10f),
                    size = Size(36f, 20f)
                )
                // Horse Head & Armor
                drawScope.drawOval(
                    color = Color(0xFF90A4AE).copy(alpha = alpha),
                    topLeft = Offset(12f, -22f),
                    size = Size(14f, 16f)
                )
                // Knight with heavy shield & glowing lance
                drawScope.drawRoundRect(
                    color = Color(0xFFECEFF1).copy(alpha = alpha),
                    topLeft = Offset(-10f, -24f),
                    size = Size(16f, 18f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Plume
                drawScope.drawRect(
                    color = teamColor.copy(alpha = alpha),
                    topLeft = Offset(-6f, -30f),
                    size = Size(8f, 6f)
                )
                // Giant War Lance
                drawScope.drawLine(
                    color = Color(0xFFFFD54F).copy(alpha = alpha),
                    start = Offset(-4f, -14f),
                    end = Offset(34f, -18f),
                    strokeWidth = 4.5f
                )
            }
            3 -> { // Heavy Field Cannon
                // Wooden Carriage
                drawScope.drawRoundRect(
                    color = Color(0xFF5D4037).copy(alpha = alpha),
                    topLeft = Offset(-20f, -4f),
                    size = Size(30f, 14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Big Iron Barrel
                drawScope.drawRoundRect(
                    color = Color(0xFF263238).copy(alpha = alpha),
                    topLeft = Offset(-12f, -16f),
                    size = Size(34f, 10f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Wheels
                drawScope.drawCircle(
                    color = Color(0xFF8D6E63).copy(alpha = alpha),
                    radius = 9f,
                    center = Offset(-4f, 8f),
                    style = Stroke(width = 4f)
                )
            }
            4 -> { // Modern Heavy Combat Tank!
                // Tread System
                drawScope.drawRoundRect(
                    color = Color(0xFF212121).copy(alpha = alpha),
                    topLeft = Offset(-26f, 6f),
                    size = Size(48f, 12f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Wheels inside tread
                for (w in 0..3) {
                    drawScope.drawCircle(
                        color = Color(0xFF757575).copy(alpha = alpha),
                        radius = 4f,
                        center = Offset(-18f + w * 12f, 12f)
                    )
                }
                // Armor Hull
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F).copy(alpha = alpha),
                    topLeft = Offset(-24f, -8f),
                    size = Size(44f, 16f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                // Rotating Turret
                drawScope.drawRoundRect(
                    color = Color(0xFF263238).copy(alpha = alpha),
                    topLeft = Offset(-10f, -18f),
                    size = Size(22f, 11f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Team flag
                drawScope.drawRect(
                    color = teamColor.copy(alpha = alpha),
                    topLeft = Offset(-8f, -14f),
                    size = Size(6f, 4f)
                )
                // Main Heavy Cannon Barrel
                val recoil = if (unit.isAttacking) -6f else 0f
                drawScope.drawRoundRect(
                    color = Color(0xFF455A64).copy(alpha = alpha),
                    topLeft = Offset(10f + recoil, -15f),
                    size = Size(26f, 5f),
                    cornerRadius = CornerRadius(1.5f, 1.5f)
                )
            }
            else -> { // Cyber Titan Mech Robot!
                // Heavy legs
                val legShift = legWalk * 5f
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F).copy(alpha = alpha),
                    topLeft = Offset(-14f, 2f),
                    size = Size(8f, 16f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F).copy(alpha = alpha),
                    topLeft = Offset(4f, 2f),
                    size = Size(8f, 16f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Massive Mech Torso
                drawScope.drawRoundRect(
                    color = Color(0xFF212121).copy(alpha = alpha),
                    topLeft = Offset(-22f, -22f),
                    size = Size(38f, 26f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                // Glowing Plasma Core
                drawScope.drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = alpha),
                    radius = 6f,
                    center = Offset(-3f, -10f)
                )
                // Shoulder Cannons
                drawScope.drawRoundRect(
                    color = Color(0xFF455A64).copy(alpha = alpha),
                    topLeft = Offset(14f, -24f),
                    size = Size(18f, 8f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawScope.drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = alpha),
                    start = Offset(20f, -20f),
                    end = Offset(32f, -20f),
                    strokeWidth = 3f
                )
            }
        }
    }

    fun drawProjectile(
        drawScope: DrawScope,
        projectile: Projectile,
        canvasWidth: Float,
        groundY: Float
    ) {
        val posX = projectile.currentX * canvasWidth
        // Parabolic arc for rocks/arrows, straight for rockets/lasers
        val progress = if (projectile.startX != projectile.targetX) {
            ((projectile.currentX - projectile.startX) / (projectile.targetX - projectile.startX)).coerceIn(0f, 1f)
        } else 0.5f

        val arcHeight = when (projectile.type) {
            ProjectileType.ROCK -> 45f
            ProjectileType.ARROW -> 30f
            ProjectileType.CROSSBOW_BOLT -> 15f
            ProjectileType.CANNONBALL -> 35f
            ProjectileType.ROCKET -> 5f
            ProjectileType.LASER -> 0f
        }

        val arc = sin(progress * PI.toFloat()) * arcHeight
        val posY = groundY - 20f - arc
        val dir = if (projectile.isPlayer) 1f else -1f

        when (projectile.type) {
            ProjectileType.ROCK -> {
                drawScope.drawCircle(
                    color = Color(0xFF757575),
                    radius = 4.5f,
                    center = Offset(posX, posY)
                )
            }
            ProjectileType.ARROW -> {
                drawScope.withTransform({
                    translate(posX, posY)
                    scale(dir, 1f, Offset.Zero)
                    rotate(degrees = (0.5f - progress) * 40f * dir, pivot = Offset.Zero)
                }) {
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(-8f, 0f),
                        end = Offset(8f, 0f),
                        strokeWidth = 2.5f
                    )
                    // Tip
                    drawCircle(
                        color = Color.LightGray,
                        radius = 2f,
                        center = Offset(8f, 0f)
                    )
                }
            }
            ProjectileType.CROSSBOW_BOLT -> {
                drawScope.drawLine(
                    color = Color(0xFFCFD8DC),
                    start = Offset(posX - 6f * dir, posY),
                    end = Offset(posX + 6f * dir, posY),
                    strokeWidth = 3f
                )
            }
            ProjectileType.CANNONBALL -> {
                drawScope.drawCircle(
                    color = Color(0xFF263238),
                    radius = 6f,
                    center = Offset(posX, posY)
                )
                // Smoke puff trail
                drawScope.drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 3.5f,
                    center = Offset(posX - 8f * dir, posY + 2f)
                )
            }
            ProjectileType.ROCKET -> {
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(posX - 8f, posY - 3f),
                    size = Size(16f, 6f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Flame exhaust
                drawScope.drawCircle(
                    color = Color(0xFFFF5722),
                    radius = 4f,
                    center = Offset(posX - 9f * dir, posY)
                )
            }
            ProjectileType.LASER -> {
                drawScope.drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(posX - 14f * dir, posY),
                    end = Offset(posX + 14f * dir, posY),
                    strokeWidth = 4.5f
                )
                drawScope.drawLine(
                    color = Color.White,
                    start = Offset(posX - 10f * dir, posY),
                    end = Offset(posX + 10f * dir, posY),
                    strokeWidth = 2f
                )
            }
        }
    }

    fun drawBaseBuilding(
        drawScope: DrawScope,
        isPlayer: Boolean,
        ageIndex: Int,
        canvasWidth: Float,
        groundY: Float,
        currentHp: Float,
        maxHp: Float
    ) {
        val baseWidth = 95f
        val baseHeight = 125f
        val x = if (isPlayer) 8f else canvasWidth - baseWidth - 8f
        val y = groundY - baseHeight + 10f

        val teamColor = if (isPlayer) Color(0xFF1976D2) else Color(0xFFD32F2F)

        when (ageIndex) {
            0 -> { // Cave / Stone Age Lair
                drawScope.drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(x, y + 15f),
                    size = Size(baseWidth, baseHeight - 15f),
                    cornerRadius = CornerRadius(20f, 20f)
                )
                // Cave entrance hole
                drawScope.drawOval(
                    color = Color(0xFF212121),
                    topLeft = Offset(if (isPlayer) x + baseWidth - 30f else x + 5f, groundY - 45f),
                    size = Size(25f, 45f)
                )
                // Bone or torch outside
                drawScope.drawCircle(
                    color = Color(0xFFFF9800),
                    radius = 4f,
                    center = Offset(if (isPlayer) x + baseWidth - 35f else x + 35f, groundY - 35f)
                )
            }
            1 -> { // Spartan / Greek Temple
                // Stone base & columns
                drawScope.drawRect(
                    color = Color(0xFFE0E0E0),
                    topLeft = Offset(x, y + 25f),
                    size = Size(baseWidth, baseHeight - 25f)
                )
                // Pillars
                for (p in 0..3) {
                    drawScope.drawRect(
                        color = Color(0xFFBDBDBD),
                        topLeft = Offset(x + 6f + p * 20f, y + 35f),
                        size = Size(12f, baseHeight - 45f)
                    )
                }
                // Triangular Pediment roof
                val roof = Path().apply {
                    moveTo(x - 5f, y + 25f)
                    lineTo(x + baseWidth / 2, y)
                    lineTo(x + baseWidth + 5f, y + 25f)
                    close()
                }
                drawScope.drawPath(path = roof, color = Color(0xFFFFB300))
            }
            2 -> { // Medieval Stone Castle Keep
                drawScope.drawRect(
                    color = Color(0xFF78909C),
                    topLeft = Offset(x, y + 15f),
                    size = Size(baseWidth, baseHeight - 15f)
                )
                // Battlements / Crenellations
                for (b in 0..3) {
                    drawScope.drawRect(
                        color = Color(0xFF607D8B),
                        topLeft = Offset(x + b * 22f, y),
                        size = Size(14f, 18f)
                    )
                }
                // Arch Gate
                drawScope.drawRoundRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(if (isPlayer) x + baseWidth - 32f else x + 8f, groundY - 48f),
                    size = Size(24f, 48f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Banner
                drawScope.drawRect(
                    color = teamColor,
                    topLeft = Offset(x + 10f, y + 30f),
                    size = Size(20f, 30f)
                )
            }
            3 -> { // Renaissance Bastion Fort
                drawScope.drawRect(
                    color = Color(0xFF8D6E63),
                    topLeft = Offset(x, y + 10f),
                    size = Size(baseWidth, baseHeight - 10f)
                )
                // Brick lines
                for (row in 0..4) {
                    drawScope.drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(x, y + 20f + row * 18f),
                        end = Offset(x + baseWidth, y + 20f + row * 18f),
                        strokeWidth = 2f
                    )
                }
                // Cannon port
                drawScope.drawCircle(
                    color = Color.Black,
                    radius = 7f,
                    center = Offset(if (isPlayer) x + baseWidth - 16f else x + 16f, y + 35f)
                )
            }
            4 -> { // Modern Military Steel Bunker
                drawScope.drawRoundRect(
                    color = Color(0xFF455A64),
                    topLeft = Offset(x, y + 20f),
                    size = Size(baseWidth, baseHeight - 20f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Steel blast door
                drawScope.drawRect(
                    color = Color(0xFF263238),
                    topLeft = Offset(if (isPlayer) x + baseWidth - 30f else x + 6f, groundY - 42f),
                    size = Size(24f, 42f)
                )
                // Sandbags at base
                for (s in 0..3) {
                    drawScope.drawRoundRect(
                        color = Color(0xFFBCAAA4),
                        topLeft = Offset(x + s * 20f, groundY - 12f),
                        size = Size(22f, 12f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                // Radar Dish on roof
                drawScope.drawArc(
                    color = Color(0xFFCFD8DC),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(x + 20f, y),
                    size = Size(26f, 22f),
                    style = Stroke(width = 3f)
                )
            }
            else -> { // Cyber Plasma Citadel
                drawScope.drawRoundRect(
                    color = Color(0xFF1A237E),
                    topLeft = Offset(x, y),
                    size = Size(baseWidth, baseHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Glowing Neon Conduits
                drawScope.drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(x + 10f, y + 10f),
                    end = Offset(x + 10f, groundY),
                    strokeWidth = 3f
                )
                drawScope.drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(x + baseWidth - 10f, y + 10f),
                    end = Offset(x + baseWidth - 10f, groundY),
                    strokeWidth = 3f
                )
                // Holographic core
                drawScope.drawCircle(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.8f),
                    radius = 16f,
                    center = Offset(x + baseWidth / 2, y + 40f)
                )
            }
        }

        // Base HP bar above building
        val barWidth = 85f
        val barHeight = 7f
        val barX = x + (baseWidth - barWidth) / 2
        val barY = y - 18f

        drawScope.drawRoundRect(
            color = Color(0xDD000000),
            topLeft = Offset(barX - 1.5f, barY - 1.5f),
            size = Size(barWidth + 3f, barHeight + 3f),
            cornerRadius = CornerRadius(3.5f, 3.5f)
        )

        val hpRatio = (currentHp / maxHp).coerceIn(0f, 1f)
        val hpColor = if (isPlayer) Color(0xFF2196F3) else Color(0xFFE53935)

        drawScope.drawRoundRect(
            color = hpColor,
            topLeft = Offset(barX, barY),
            size = Size(barWidth * hpRatio, barHeight),
            cornerRadius = CornerRadius(3.5f, 3.5f)
        )
    }
}
