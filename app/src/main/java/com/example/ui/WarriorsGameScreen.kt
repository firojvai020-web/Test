package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SkillType
import com.example.model.UnitRole
import com.example.model.WarriorBlueprint
import com.example.viewmodel.WarriorsViewModel

@Composable
fun WarriorsGameScreen(viewModel: WarriorsViewModel) {
    val state by viewModel.state.collectAsState()

    var showUpgradesSheet by remember { mutableStateOf(false) }
    var showCardsDialog by remember { mutableStateOf(false) }

    val isFrozen = state.skills.find { it.type == SkillType.FREEZE }?.isActive == true
    val isMorale = state.skills.find { it.type == SkillType.MORALE_BOOST }?.isActive == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14141E))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. TOP-SCREEN HUD (Age, Meat Count, Upgrade Meat Production Button, Coins, Gems, Speed)
            val foodUpgradeCost = (15L * state.foodRateLevel * (state.age.index + 1))
            val canAffordFoodUpgrade = state.coins >= foodUpgradeCost

            TopScreenHud(
                ageName = state.age.name,
                timelineBattle = state.timelineBattle,
                food = state.food,
                maxFood = state.maxFood,
                foodRate = state.foodRate,
                foodRateLevel = state.foodRateLevel,
                foodUpgradeCost = foodUpgradeCost,
                canAffordFoodUpgrade = canAffordFoodUpgrade,
                coins = state.coins,
                gems = state.gems,
                gameSpeed = state.gameSpeed,
                isMuted = state.isMuted,
                onUpgradeMeatProduction = { viewModel.upgradeFoodProduction() },
                onToggleSpeed = { viewModel.toggleGameSpeed() },
                onToggleMute = { viewModel.toggleMute() }
            )

            // 2. BATTLEFIELD VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BattlefieldCanvas(
                    age = state.age,
                    playerBase = state.playerBase,
                    enemyBase = state.enemyBase,
                    units = state.units,
                    projectiles = state.projectiles,
                    floatingTexts = state.floatingTexts,
                    particles = state.particles,
                    isFrozen = isFrozen,
                    isMoraleBoosted = isMorale,
                    meteorEffectProgress = state.meteorEffectProgress
                )

                // Skills Bar floating on the right side of battlefield
                SkillsBar(
                    skills = state.skills,
                    onTriggerSkill = { viewModel.triggerSkill(it) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }

            // 3. BOTTOM COMMAND CENTER
            BottomControlTray(
                food = state.food,
                maxFood = state.maxFood,
                foodRate = state.foodRate,
                units = state.age.units,
                unitLevelMelee = state.unitLevelMelee,
                unitLevelRanged = state.unitLevelRanged,
                unitLevelHeavy = state.unitLevelHeavy,
                canEvolve = state.coins >= state.age.evolveCostCoins,
                onSpawnUnit = { viewModel.spawnUnit(it) },
                onOpenUpgrades = { showUpgradesSheet = true },
                onOpenCards = { showCardsDialog = true }
            )
        }

        // Upgrades Bottom Sheet
        if (showUpgradesSheet) {
            UpgradesBottomSheet(
                state = state,
                onDismiss = { showUpgradesSheet = false },
                onUpgradeFood = { viewModel.upgradeFoodProduction() },
                onUpgradeBaseHp = { viewModel.upgradeBaseHp() },
                onUpgradeUnit = { viewModel.upgradeUnitRole(it) },
                onEvolveAge = {
                    viewModel.evolveAge()
                    showUpgradesSheet = false
                }
            )
        }

        // Cards / Relics Dialog
        if (showCardsDialog) {
            CardsDialog(
                gems = state.gems,
                cards = state.cards,
                onDismiss = { showCardsDialog = false },
                onDrawCard = { viewModel.drawGachaCard() }
            )
        }

        // Victory Dialog
        if (state.isVictory) {
            BattleOutcomeDialog(
                isVictory = true,
                coinsEarned = state.battleCoinsEarned,
                gemsEarned = 10 + state.age.index * 5,
                battleNumber = state.timelineBattle,
                onConfirm = { viewModel.nextBattle() },
                onOpenUpgrades = { showUpgradesSheet = true }
            )
        }

        // Defeat Dialog
        if (state.isDefeat) {
            BattleOutcomeDialog(
                isVictory = false,
                coinsEarned = state.battleCoinsEarned,
                gemsEarned = 0,
                battleNumber = state.timelineBattle,
                onConfirm = { viewModel.restartBattle() },
                onOpenUpgrades = { showUpgradesSheet = true }
            )
        }
    }
}

@Composable
fun TopBar(
    ageName: String,
    timelineBattle: Int,
    coins: Long,
    gems: Int,
    gameSpeed: Float,
    isMuted: Boolean,
    onToggleSpeed: () -> Unit,
    onToggleMute: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1E2C),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Age & Battle Pill
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ageName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFB300)
                    )
                }
                Text(
                    text = "Battle $timelineBattle / 6",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF90A4AE)
                )
            }

            // Coin & Gem Counters
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B2215))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Gems
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF152A20))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = Color(0xFF69F0AE),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$gems",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69F0AE)
                    )
                }
            }

            // Quick Controls (Speed, Sound)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Speed Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E2E42))
                        .clickable(onClick = onToggleSpeed)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("speed_toggle_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${gameSpeed}x",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (gameSpeed > 1f) Color(0xFFFFD54F) else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Mute Button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("sound_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Sound",
                        tint = if (isMuted) Color.Gray else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SkillsBar(
    skills: List<com.example.model.SkillState>,
    onTriggerSkill: (SkillType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (skill in skills) {
            val icon = when (skill.type) {
                SkillType.METEOR_STRIKE -> Icons.Default.Bolt
                SkillType.MORALE_BOOST -> Icons.Default.FlashOn
                SkillType.FREEZE -> Icons.Default.AcUnit
            }
            val skillColor = when (skill.type) {
                SkillType.METEOR_STRIKE -> Color(0xFFFF5722)
                SkillType.MORALE_BOOST -> Color(0xFFFFD700)
                SkillType.FREEZE -> Color(0xFF00E5FF)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC1A1A28))
                    .border(
                        width = 2.dp,
                        color = if (skill.isReady) skillColor else Color(0xFF424255),
                        shape = CircleShape
                    )
                    .clickable(enabled = skill.isReady) { onTriggerSkill(skill.type) }
                    .testTag("skill_${skill.type.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = skill.type.title,
                    tint = if (skill.isReady) skillColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                // Cooldown shadow overlay
                if (!skill.isReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xAA000000))
                    )
                    Text(
                        text = "${skill.currentCooldown.toInt()}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BottomControlTray(
    food: Float,
    maxFood: Float,
    foodRate: Float,
    units: List<WarriorBlueprint>,
    unitLevelMelee: Int,
    unitLevelRanged: Int,
    unitLevelHeavy: Int,
    canEvolve: Boolean,
    onSpawnUnit: (WarriorBlueprint) -> Unit,
    onOpenUpgrades: () -> Unit,
    onOpenCards: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1B1B2A),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // 1. Meat / Food Production Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🍗", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Meat: ${food.toInt()} / ${maxFood.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "+${(foodRate * 10).toInt() / 10f}/sec",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF69F0AE)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (food / maxFood).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFFF7043),
                trackColor = Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Troop Spawn Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in units.indices) {
                    val unit = units[i]
                    val canAfford = food >= unit.foodCost
                    val unitLevel = when (unit.role) {
                        UnitRole.MELEE -> unitLevelMelee
                        UnitRole.RANGED -> unitLevelRanged
                        UnitRole.HEAVY -> unitLevelHeavy
                    }

                    UnitSpawnCard(
                        blueprint = unit,
                        level = unitLevel,
                        canAfford = canAfford,
                        tag = "spawn_unit_$i",
                        onSpawn = { onSpawnUnit(unit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Navigation / Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenUpgrades,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("open_upgrades_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canEvolve) Color(0xFFFF9800) else Color(0xFF2C2C40)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Upgrade,
                            contentDescription = null,
                            tint = if (canEvolve) Color.White else Color(0xFFFFD54F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (canEvolve) "EVOLVE AGE!" else "UPGRADES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = onOpenCards,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("open_cards_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38264A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = Color(0xFFE040FB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RELIC CARDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitSpawnCard(
    blueprint: WarriorBlueprint,
    level: Int,
    canAfford: Boolean,
    tag: String,
    onSpawn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(92.dp)
            .border(
                width = 1.5.dp,
                color = if (canAfford) Color(0xFFFF9800) else Color(0xFF323246),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = canAfford, onClick = onSpawn)
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) Color(0xFF2E241B) else Color(0xFF222232)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Level badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (blueprint.role) {
                        UnitRole.MELEE -> "⚔️"
                        UnitRole.RANGED -> "🏹"
                        UnitRole.HEAVY -> "🛡️"
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = "Lv.$level",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
            }

            // Unit Name
            Text(
                text = blueprint.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (canAfford) Color.White else Color.Gray,
                maxLines = 1
            )

            // Meat Cost Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (canAfford) Color(0xFFFF7043) else Color(0xFF424254))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🍗 ${blueprint.foodCost}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun BattleOutcomeDialog(
    isVictory: Boolean,
    coinsEarned: Long,
    gemsEarned: Int,
    battleNumber: Int,
    onConfirm: () -> Unit,
    onOpenUpgrades: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            color = Color(0xFF1E1E2E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isVictory) "🏆 VICTORY!" else "💀 DEFEAT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isVictory) Color(0xFFFFD700) else Color(0xFFE53935)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isVictory) {
                        "Enemy Fortress Cleared on Battle $battleNumber!"
                    } else {
                        "Your Fortress fell, but your loot was preserved!"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFFCFD8DC),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rewards Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF28283C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Battle Coins Farmed", fontSize = 13.sp, color = Color.Gray)
                            Text(
                                text = "+🪙 $coinsEarned",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }

                        if (gemsEarned > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Victory Gems Bonus", fontSize = 13.sp, color = Color.Gray)
                                Text(
                                    text = "+💎 $gemsEarned",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF69F0AE)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenUpgrades,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("outcome_upgrades_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "UPGRADE", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("outcome_continue_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVictory) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isVictory) "NEXT BATTLE" else "RETRY BATTLE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
