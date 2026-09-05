package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgesCatalog
import com.example.model.UnitRole
import com.example.viewmodel.WarriorsGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradesBottomSheet(
    state: WarriorsGameState,
    onDismiss: () -> Unit,
    onUpgradeFood: () -> Unit,
    onUpgradeBaseHp: () -> Unit,
    onUpgradeUnit: (UnitRole) -> Unit,
    onEvolveAge: () -> Unit
) {
    val currentAge = state.age
    val hasNextAge = currentAge.index < AgesCatalog.ALL_AGES.size - 1
    val nextAge = if (hasNextAge) AgesCatalog.ALL_AGES[currentAge.index + 1] else null

    val foodUpgradeCost = (15L * state.foodRateLevel * (currentAge.index + 1))
    val baseHpUpgradeCost = (20L * state.baseHpLevel * (currentAge.index + 1))

    val meleeCost = (25L * state.unitLevelMelee * (currentAge.index + 1))
    val rangedCost = (25L * state.unitLevelRanged * (currentAge.index + 1))
    val heavyCost = (25L * state.unitLevelHeavy * (currentAge.index + 1))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E2C)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF9800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upgrade,
                            contentDescription = "Upgrades",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Base & Unit Upgrades",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_upgrades_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Age Evolution Card
            if (hasNextAge && nextAge != null) {
                val canEvolve = state.coins >= currentAge.evolveCostCoins
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = if (canEvolve) Color(0xFFFFD700) else Color(0xFF4A4A68),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canEvolve) Color(0xFF2E2415) else Color(0xFF28283C)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ERA EVOLUTION",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFB300),
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "${currentAge.name}  ➔  ${nextAge.name}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "🪙 ${currentAge.evolveCostCoins}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canEvolve) Color(0xFFFFD700) else Color(0xFFFFAB91)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlocks: ${nextAge.units.joinToString(", ") { it.name }} and increases base strength!",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onEvolveAge,
                            enabled = canEvolve,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("evolve_age_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                disabledContainerColor = Color(0xFF424258)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (canEvolve) "EVOLVE NOW!" else "NEED ${currentAge.evolveCostCoins - state.coins} MORE COINS",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (canEvolve) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Base Production Upgrades
            Text(
                text = "PRODUCTION & FORTIFICATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90A4AE),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            UpgradeRowItem(
                title = "Meat Production Rate",
                subtitle = "Current: ${(state.foodRate * 10).toInt() / 10f} /sec  (+0.15/s)",
                level = state.foodRateLevel,
                cost = foodUpgradeCost,
                canAfford = state.coins >= foodUpgradeCost,
                icon = Icons.Default.Fastfood,
                iconColor = Color(0xFFFF7043),
                tag = "upgrade_food_btn",
                onUpgrade = onUpgradeFood
            )

            Spacer(modifier = Modifier.height(10.dp))

            UpgradeRowItem(
                title = "Base Fortress Health",
                subtitle = "Current: ${state.playerBase.maxHp.toInt()} HP  (+60 HP)",
                level = state.baseHpLevel,
                cost = baseHpUpgradeCost,
                canAfford = state.coins >= baseHpUpgradeCost,
                icon = Icons.Default.Security,
                iconColor = Color(0xFF42A5F5),
                tag = "upgrade_base_hp_btn",
                onUpgrade = onUpgradeBaseHp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Unit Stat Upgrades
            Text(
                text = "WARRIOR UNITS TRAINING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90A4AE),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            UpgradeRowItem(
                title = currentAge.units[0].name,
                subtitle = "Melee • +15% Damage & HP",
                level = state.unitLevelMelee,
                cost = meleeCost,
                canAfford = state.coins >= meleeCost,
                icon = Icons.Default.Upgrade,
                iconColor = Color(0xFF66BB6A),
                tag = "upgrade_unit_melee_btn",
                onUpgrade = { onUpgradeUnit(UnitRole.MELEE) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            UpgradeRowItem(
                title = currentAge.units[1].name,
                subtitle = "Ranged • +15% Damage & HP",
                level = state.unitLevelRanged,
                cost = rangedCost,
                canAfford = state.coins >= rangedCost,
                icon = Icons.Default.Upgrade,
                iconColor = Color(0xFFAB47BC),
                tag = "upgrade_unit_ranged_btn",
                onUpgrade = { onUpgradeUnit(UnitRole.RANGED) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            UpgradeRowItem(
                title = currentAge.units[2].name,
                subtitle = "Heavy Tank • +15% Damage & HP",
                level = state.unitLevelHeavy,
                cost = heavyCost,
                canAfford = state.coins >= heavyCost,
                icon = Icons.Default.Upgrade,
                iconColor = Color(0xFFFFCA28),
                tag = "upgrade_unit_heavy_btn",
                onUpgrade = { onUpgradeUnit(UnitRole.HEAVY) }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun UpgradeRowItem(
    title: String,
    subtitle: String,
    level: Int,
    cost: Long,
    canAfford: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    tag: String,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF272738)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lv.$level",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                modifier = Modifier
                    .height(38.dp)
                    .testTag(tag),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFF37474F)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🪙 $cost",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
