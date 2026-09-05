package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top-screen HUD component for "We Are Warriors!".
 * Displays:
 * 1. Current Age & Battle Timeline progress
 * 2. Meat count, generation rate, and capacity meter
 * 3. Quick-action Meat Production Upgrade button
 * 4. Coin & Gem resource counters and quick settings
 */
@Composable
fun TopScreenHud(
    ageName: String,
    timelineBattle: Int,
    food: Float,
    maxFood: Float,
    foodRate: Float,
    foodRateLevel: Int,
    foodUpgradeCost: Long,
    canAffordFoodUpgrade: Boolean,
    coins: Long,
    gems: Int,
    gameSpeed: Float,
    isMuted: Boolean,
    onUpgradeMeatProduction: () -> Unit,
    onToggleSpeed: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_screen_hud"),
        color = Color(0xFF1B1B2A),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // ─── TIER 1: AGE, CURRENCIES & CONTROLS ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Current Age & Timeline Pill
                Column(modifier = Modifier.testTag("hud_current_age_section")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getAgeIcon(ageName),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ageName.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFC107),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Timeline • Battle $timelineBattle / 6",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF90A4AE)
                    )
                }

                // 2. Resource Counters (Coins & Gems)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Coins Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2C2216))
                            .border(1.dp, Color(0xFF5D4037), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("hud_coins_counter")
                    ) {
                        Text(text = "🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$coins",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    // Gems Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF13281E))
                            .border(1.dp, Color(0xFF1B5E20), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("hud_gems_counter")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = Color(0xFF69F0AE),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$gems",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF69F0AE)
                        )
                    }
                }

                // 3. Quick Controls (Speed & Mute)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2D2D40))
                            .clickable(onClick = onToggleSpeed)
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("speed_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${gameSpeed}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (gameSpeed > 1f) Color(0xFFFFD54F) else Color.White
                        )
                    }

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("sound_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Sound",
                            tint = if (isMuted) Color.Gray else Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ─── TIER 2: MEAT COUNT & PRODUCTION UPGRADE BAR ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF222234))
                    .border(1.dp, Color(0xFF33334D), RoundedCornerShape(10.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meat Count & Generation Rate
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("hud_meat_count_display")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🍗", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Meat: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCFD8DC)
                            )
                            Text(
                                text = "${food.toInt()} / ${maxFood.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // Rate tag
                        Text(
                            text = "+${(foodRate * 10).toInt() / 10f}/s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Live Meat fill progress
                    LinearProgressIndicator(
                        progress = { (food / maxFood).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.5.dp)),
                        color = Color(0xFFFF7043),
                        trackColor = Color(0xFF37474F)
                    )
                }

                // Meat Production Upgrade Button
                val buttonBg = if (canAffordFoodUpgrade) {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF323246), Color(0xFF2B2B3D))
                    )
                }
                val borderColor = if (canAffordFoodUpgrade) Color(0xFFFFD54F) else Color(0xFF45455E)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(buttonBg)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = canAffordFoodUpgrade, onClick = onUpgradeMeatProduction)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("upgrade_meat_production_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = if (canAffordFoodUpgrade) Color.White else Color(0xFF90A4AE),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "MEAT PROD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (canAffordFoodUpgrade) Color.White else Color(0xFF90A4AE)
                                )
                            }
                            Text(
                                text = "Lvl $foodRateLevel",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAffordFoodUpgrade) Color(0xFFFFF9C4) else Color(0xFF78909C)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Cost Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x55000000))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$foodUpgradeCost",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canAffordFoodUpgrade) Color(0xFFFFD54F) else Color(0xFFB0BEC5)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getAgeIcon(ageName: String): String {
    return when {
        ageName.contains("Stone", ignoreCase = true) -> "🪨"
        ageName.contains("Spartan", ignoreCase = true) -> "🏛️"
        ageName.contains("Medieval", ignoreCase = true) -> "⚔️"
        ageName.contains("Renaissance", ignoreCase = true) -> "🏰"
        ageName.contains("Modern", ignoreCase = true) -> "🎖️"
        ageName.contains("Cyber", ignoreCase = true) -> "🤖"
        else -> "🛡️"
    }
}
