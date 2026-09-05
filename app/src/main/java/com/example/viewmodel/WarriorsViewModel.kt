package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundSynthesizer
import com.example.data.GamePreferences
import com.example.model.AgeDefinition
import com.example.model.AgesCatalog
import com.example.model.BaseTower
import com.example.model.CardBonusType
import com.example.model.CardItem
import com.example.model.CombatUnit
import com.example.model.FloatingCombatText
import com.example.model.Particle
import com.example.model.Projectile
import com.example.model.SkillState
import com.example.model.SkillType
import com.example.model.UnitRole
import com.example.model.WarriorBlueprint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

data class WarriorsGameState(
    val age: AgeDefinition = AgesCatalog.ALL_AGES[0],
    val timelineBattle: Int = 1,
    val coins: Long = 50L,
    val gems: Int = 30,
    val battleCoinsEarned: Long = 0L,
    val food: Float = 5.0f,
    val maxFood: Float = 100f,
    val foodRate: Float = 1.0f,
    val foodRateLevel: Int = 1,
    val baseHpLevel: Int = 1,
    val unitLevelMelee: Int = 1,
    val unitLevelRanged: Int = 1,
    val unitLevelHeavy: Int = 1,
    val playerBase: BaseTower = BaseTower(currentHp = 300f, maxHp = 300f, isPlayer = true),
    val enemyBase: BaseTower = BaseTower(currentHp = 300f, maxHp = 300f, isPlayer = false),
    val units: List<CombatUnit> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val floatingTexts: List<FloatingCombatText> = emptyList(),
    val particles: List<Particle> = emptyList(),
    val skills: List<SkillState> = emptyList(),
    val cards: List<CardItem> = emptyList(),
    val gameSpeed: Float = 1.0f,
    val isBattleActive: Boolean = true,
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false,
    val isMuted: Boolean = false,
    val meteorEffectProgress: Float = 0f
)

class WarriorsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = GamePreferences(application)
    val sound = SoundSynthesizer()

    private val _state = MutableStateFlow(WarriorsGameState())
    val state: StateFlow<WarriorsGameState> = _state.asStateFlow()

    private var gameLoopJob: Job? = null
    private var enemySpawnTimer = 0f
    private var currentMeteorTimer = 0f

    init {
        sound.isMuted = prefs.isMuted
        loadInitialState()
        startGameLoop()
    }

    private fun loadInitialState() {
        val ageIndex = prefs.currentAgeIndex.coerceIn(0, AgesCatalog.ALL_AGES.size - 1)
        val age = AgesCatalog.ALL_AGES[ageIndex]
        val baseMaxHp = 250f + age.baseHpBonus + (prefs.baseHpLevel * 60f)
        val enemyMaxHp = 250f + age.baseHpBonus + (prefs.timelineBattle * 120f)
        val foodRate = 1.0f + (prefs.foodRateLevel * 0.15f)

        val cards = listOf(
            CardItem("card_dmg", "Berserk Might", "+Damage for all units", "sword", prefs.getCardLevel("card_dmg"), 10, CardBonusType.DAMAGE_BOOST, 0.10f),
            CardItem("card_hp", "Iron Vitality", "+Max HP for all units", "shield", prefs.getCardLevel("card_hp"), 10, CardBonusType.HEALTH_BOOST, 0.12f),
            CardItem("card_spd", "Wind Boots", "+Movement speed", "boots", prefs.getCardLevel("card_spd"), 10, CardBonusType.SPEED_BOOST, 0.08f),
            CardItem("card_gold", "Golden Goblet", "+Battle coin rewards", "coin", prefs.getCardLevel("card_gold"), 10, CardBonusType.GOLD_BOOST, 0.15f)
        )

        val skills = listOf(
            SkillState(SkillType.METEOR_STRIKE),
            SkillState(SkillType.MORALE_BOOST),
            SkillState(SkillType.FREEZE)
        )

        _state.value = _state.value.copy(
            age = age,
            timelineBattle = prefs.timelineBattle,
            coins = prefs.coins,
            gems = prefs.gems,
            battleCoinsEarned = 0L,
            food = 5.0f,
            foodRate = foodRate,
            foodRateLevel = prefs.foodRateLevel,
            baseHpLevel = prefs.baseHpLevel,
            unitLevelMelee = prefs.unitLevelMelee,
            unitLevelRanged = prefs.unitLevelRanged,
            unitLevelHeavy = prefs.unitLevelHeavy,
            playerBase = BaseTower(currentHp = baseMaxHp, maxHp = baseMaxHp, isPlayer = true),
            enemyBase = BaseTower(currentHp = enemyMaxHp, maxHp = enemyMaxHp, isPlayer = false),
            units = emptyList(),
            projectiles = emptyList(),
            floatingTexts = emptyList(),
            particles = emptyList(),
            skills = skills,
            cards = cards,
            isBattleActive = true,
            isVictory = false,
            isDefeat = false,
            isMuted = prefs.isMuted
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val dt = 0.016f * _state.value.gameSpeed
                if (_state.value.isBattleActive && !_state.value.isVictory && !_state.value.isDefeat) {
                    updateGame(dt)
                }
                delay(16)
            }
        }
    }

    private fun updateGame(dt: Float) {
        val s = _state.value
        val isFrozen = s.skills.find { it.type == SkillType.FREEZE }?.isActive == true
        val isMorale = s.skills.find { it.type == SkillType.MORALE_BOOST }?.isActive == true

        // 1. Food Regeneration
        val newFood = (s.food + s.foodRate * dt).coerceAtMost(s.maxFood)

        // 2. Skill timers
        val updatedSkills = s.skills.map { skill ->
            val newCooldown = (skill.currentCooldown - dt).coerceAtLeast(0f)
            val newActive = (skill.activeDurationRemaining - dt).coerceAtLeast(0f)
            skill.copy(currentCooldown = newCooldown, activeDurationRemaining = newActive)
        }

        // Meteor visual progress
        if (currentMeteorTimer > 0f) {
            currentMeteorTimer = (currentMeteorTimer - dt).coerceAtLeast(0f)
        }

        // 3. Enemy Spawner AI
        enemySpawnTimer += dt
        val nextUnits = s.units.toMutableList()
        val nextParticles = s.particles.toMutableList()
        val nextFloats = s.floatingTexts.toMutableList()
        val nextProj = s.projectiles.toMutableList()

        val spawnInterval = max(2.0f, 4.2f - (s.timelineBattle * 0.35f))
        if (enemySpawnTimer >= spawnInterval && !isFrozen) {
            enemySpawnTimer = 0f
            spawnEnemyUnit(nextUnits)
        }

        // 4. Update Units
        val playerBasePos = 0.12f
        val enemyBasePos = 0.88f
        var coinsGained = 0L
        var playerBaseHp = s.playerBase.currentHp
        var enemyBaseHp = s.enemyBase.currentHp

        val survivingUnits = mutableListOf<CombatUnit>()
        for (unit in nextUnits) {
            if (unit.isDead) {
                unit.deathTimer += dt * 2.5f
                if (unit.deathTimer < 1f) {
                    survivingUnits.add(unit)
                }
                continue
            }

            // Status checks
            val frozen = !unit.isPlayer && isFrozen
            val speedBonus = if (unit.isPlayer && isMorale) 1.6f else 1.0f
            val attackSpeedBonus = if (unit.isPlayer && isMorale) 1.6f else 1.0f

            unit.attackTimer -= dt * attackSpeedBonus
            unit.walkAnimCycle += dt * 4f * speedBonus

            // Find closest opponent (ahead or slightly overlapping to prevent ghosting)
            val opponentAhead = if (unit.isPlayer) {
                nextUnits.filter { !it.isPlayer && !it.isDead && it.x >= unit.x - 0.04f }
                    .minByOrNull { abs(it.x - unit.x) }
            } else {
                nextUnits.filter { it.isPlayer && !it.isDead && it.x <= unit.x + 0.04f }
                    .minByOrNull { abs(unit.x - it.x) }
            }

            // Friendly unit ahead for formation spacing
            val friendlyAhead = if (unit.isPlayer) {
                nextUnits.filter { it.isPlayer && !it.isDead && it.id != unit.id && it.x > unit.x }
                    .minByOrNull { it.x - unit.x }
            } else {
                nextUnits.filter { !it.isPlayer && !it.isDead && it.id != unit.id && it.x < unit.x }
                    .minByOrNull { unit.x - it.x }
            }
            val minFriendlySpacing = if (unit.blueprint.role == UnitRole.HEAVY) 0.05f else 0.035f
            val blockedByFriendly = friendlyAhead?.let { abs(it.x - unit.x) < minFriendlySpacing } ?: false

            val distanceToOpponent = opponentAhead?.let { abs(it.x - unit.x) } ?: 999f
            val distanceToBase = if (unit.isPlayer) {
                (enemyBasePos - unit.x)
            } else {
                (unit.x - playerBasePos)
            }

            val inCombatRange = distanceToOpponent <= unit.blueprint.attackRange ||
                    distanceToBase <= unit.blueprint.attackRange

            if (inCombatRange && !frozen) {
                unit.isAttacking = true
                if (unit.attackTimer <= 0f) {
                    unit.attackTimer = unit.blueprint.attackCooldownSec
                    unit.attackAnimProgress = 1f

                    // Execute Attack
                    if (unit.blueprint.projectileType != null) {
                        // Launch projectile
                        val targetX = if (opponentAhead != null) opponentAhead.x else (if (unit.isPlayer) enemyBasePos else playerBasePos)
                        nextProj.add(
                            Projectile(
                                id = UUID.randomUUID().toString(),
                                isPlayer = unit.isPlayer,
                                currentX = unit.x,
                                targetX = targetX,
                                damage = unit.damage,
                                speed = 0.75f,
                                type = unit.blueprint.projectileType,
                                startX = unit.x
                            )
                        )
                        sound.playArrowWhoosh()
                    } else {
                        // Direct melee strike
                        sound.playSwordClash()
                        if (opponentAhead != null && distanceToOpponent <= unit.blueprint.attackRange) {
                            applyDamageToUnit(opponentAhead, unit.damage, unit.isPlayer, nextFloats, nextParticles)
                            if (opponentAhead.currentHp <= 0f) {
                                coinsGained += calculateCoinReward(opponentAhead.blueprint)
                            }
                        } else {
                            // Hit enemy base or player base
                            if (unit.isPlayer) {
                                enemyBaseHp = max(0f, enemyBaseHp - unit.damage)
                                nextFloats.add(
                                    FloatingCombatText(
                                        id = UUID.randomUUID().toString(),
                                        x = enemyBasePos,
                                        yOffset = 10f,
                                        text = "-${unit.damage.toInt()}",
                                        isCrit = false,
                                        isPlayerDamage = true
                                    )
                                )
                            } else {
                                playerBaseHp = max(0f, playerBaseHp - unit.damage)
                                nextFloats.add(
                                    FloatingCombatText(
                                        id = UUID.randomUUID().toString(),
                                        x = playerBasePos,
                                        yOffset = 10f,
                                        text = "-${unit.damage.toInt()}",
                                        isCrit = false,
                                        isPlayerDamage = false
                                    )
                                )
                            }
                        }
                    }
                } else {
                    unit.attackAnimProgress = max(0f, unit.attackAnimProgress - dt * 3f)
                }
            } else {
                unit.isAttacking = false
                unit.attackAnimProgress = 0f
                if (!frozen && !blockedByFriendly) {
                    // Walk forward towards enemy base
                    val moveDist = unit.blueprint.moveSpeed * dt * speedBonus
                    if (unit.isPlayer) {
                        unit.x = (unit.x + moveDist).coerceAtMost(enemyBasePos)
                    } else {
                        unit.x = (unit.x - moveDist).coerceAtLeast(playerBasePos)
                    }
                }
            }

            survivingUnits.add(unit)
        }

        // 5. Update Projectiles
        val remainingProj = mutableListOf<Projectile>()
        for (proj in nextProj) {
            val step = proj.speed * dt
            if (proj.isPlayer) {
                proj.currentX += step
                if (proj.currentX >= proj.targetX) {
                    // Detonate projectile at target
                    handleProjectileImpact(proj, nextUnits, nextFloats, nextParticles) { isPlayerBase, dmg ->
                        if (isPlayerBase) {
                            playerBaseHp = max(0f, playerBaseHp - dmg)
                        } else {
                            enemyBaseHp = max(0f, enemyBaseHp - dmg)
                        }
                    }
                    sound.playExplosion()
                } else {
                    remainingProj.add(proj)
                }
            } else {
                proj.currentX -= step
                if (proj.currentX <= proj.targetX) {
                    handleProjectileImpact(proj, nextUnits, nextFloats, nextParticles) { isPlayerBase, dmg ->
                        if (isPlayerBase) {
                            playerBaseHp = max(0f, playerBaseHp - dmg)
                        } else {
                            enemyBaseHp = max(0f, enemyBaseHp - dmg)
                        }
                    }
                    sound.playExplosion()
                } else {
                    remainingProj.add(proj)
                }
            }
        }

        // 6. Update Floating Texts & Particles
        val remainingFloats = nextFloats.filter {
            it.lifetime -= dt
            it.yOffset += dt * 45f
            it.alpha = (it.lifetime / 0.8f).coerceIn(0f, 1f)
            it.lifetime > 0f
        }

        val remainingParticles = nextParticles.filter {
            it.x += it.vx * dt
            it.y += it.vy * dt
            it.life -= dt
            it.alpha = (it.life / it.maxLife).coerceIn(0f, 1f)
            it.life > 0f
        }

        // 7. Check Victory / Defeat
        var isVictory = false
        var isDefeat = false
        val currentCoins = s.coins + coinsGained
        val totalBattleCoins = s.battleCoinsEarned + coinsGained

        if (enemyBaseHp <= 0f && !s.isVictory) {
            isVictory = true
            sound.playVictory()
            val victoryCoins = 150L * (s.age.index + 1) * s.timelineBattle
            val bonusGems = 10 + s.age.index * 5
            prefs.coins = currentCoins + victoryCoins
            prefs.gems = s.gems + bonusGems
            _state.value = _state.value.copy(
                playerBase = s.playerBase.copy(currentHp = playerBaseHp),
                enemyBase = s.enemyBase.copy(currentHp = 0f),
                coins = prefs.coins,
                gems = prefs.gems,
                battleCoinsEarned = totalBattleCoins + victoryCoins,
                isVictory = true,
                isBattleActive = false
            )
            return
        } else if (playerBaseHp <= 0f && !s.isDefeat) {
            isDefeat = true
            sound.playDefeat()
            prefs.coins = currentCoins
            _state.value = _state.value.copy(
                playerBase = s.playerBase.copy(currentHp = 0f),
                enemyBase = s.enemyBase.copy(currentHp = enemyBaseHp),
                coins = prefs.coins,
                battleCoinsEarned = totalBattleCoins,
                isDefeat = true,
                isBattleActive = false
            )
            return
        }

        if (coinsGained > 0) {
            prefs.coins = currentCoins
            sound.playCoin()
        }

        _state.value = s.copy(
            food = newFood,
            coins = currentCoins,
            battleCoinsEarned = totalBattleCoins,
            playerBase = s.playerBase.copy(currentHp = playerBaseHp),
            enemyBase = s.enemyBase.copy(currentHp = enemyBaseHp),
            units = survivingUnits,
            projectiles = remainingProj,
            floatingTexts = remainingFloats,
            particles = remainingParticles,
            skills = updatedSkills,
            meteorEffectProgress = currentMeteorTimer / 2.0f
        )
    }

    private fun handleProjectileImpact(
        proj: Projectile,
        units: List<CombatUnit>,
        floatingTexts: MutableList<FloatingCombatText>,
        particles: MutableList<Particle>,
        onDamageBase: (Boolean, Float) -> Unit
    ) {
        // Area of effect damage near targetX
        val splashRange = 0.08f
        val targets = if (proj.isPlayer) {
            units.filter { !it.isPlayer && !it.isDead && abs(it.x - proj.targetX) <= splashRange }
        } else {
            units.filter { it.isPlayer && !it.isDead && abs(it.x - proj.targetX) <= splashRange }
        }

        if (targets.isNotEmpty()) {
            for (t in targets) {
                applyDamageToUnit(t, proj.damage, proj.isPlayer, floatingTexts, particles)
            }
        } else {
            // Hit base if within range
            if (proj.isPlayer && proj.targetX >= 0.82f) {
                onDamageBase(false, proj.damage)
                floatingTexts.add(
                    FloatingCombatText(
                        id = UUID.randomUUID().toString(),
                        x = 0.88f,
                        yOffset = 15f,
                        text = "-${proj.damage.toInt()}",
                        isCrit = false,
                        isPlayerDamage = true
                    )
                )
            } else if (!proj.isPlayer && proj.targetX <= 0.18f) {
                onDamageBase(true, proj.damage)
                floatingTexts.add(
                    FloatingCombatText(
                        id = UUID.randomUUID().toString(),
                        x = 0.12f,
                        yOffset = 15f,
                        text = "-${proj.damage.toInt()}",
                        isCrit = false,
                        isPlayerDamage = false
                    )
                )
            }
        }

        // Particle explosion
        for (i in 0..8) {
            particles.add(
                Particle(
                    x = proj.targetX,
                    y = -20f + (Random.nextFloat() * 20f - 10f),
                    vx = (Random.nextFloat() - 0.5f) * 0.15f,
                    vy = -(Random.nextFloat() * 40f + 20f),
                    color = 0xFFFF7043,
                    size = 4f,
                    alpha = 1f,
                    life = 0.4f,
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun applyDamageToUnit(
        unit: CombatUnit,
        damage: Float,
        isPlayerAttacking: Boolean,
        floatingTexts: MutableList<FloatingCombatText>,
        particles: MutableList<Particle>
    ) {
        val isCrit = Random.nextFloat() < 0.15f
        val actualDmg = if (isCrit) damage * 1.5f else damage
        unit.currentHp = max(0f, unit.currentHp - actualDmg)

        floatingTexts.add(
            FloatingCombatText(
                id = UUID.randomUUID().toString(),
                x = unit.x,
                yOffset = 0f,
                text = if (isCrit) "CRIT! -${actualDmg.toInt()}" else "-${actualDmg.toInt()}",
                isCrit = isCrit,
                isPlayerDamage = isPlayerAttacking
            )
        )

        // Slash impact sparks
        for (i in 0..4) {
            particles.add(
                Particle(
                    x = unit.x,
                    y = -25f,
                    vx = (Random.nextFloat() - 0.5f) * 0.12f,
                    vy = -(Random.nextFloat() * 30f + 10f),
                    color = if (isCrit) 0xFFFFD700 else 0xFFFFFFFF,
                    size = 3.5f,
                    alpha = 1f,
                    life = 0.35f,
                    maxLife = 0.35f
                )
            )
        }

        if (unit.currentHp <= 0f) {
            unit.isDead = true
            unit.deathTimer = 0f
        }
    }

    private fun spawnEnemyUnit(unitList: MutableList<CombatUnit>) {
        val age = _state.value.age
        val roll = Random.nextFloat()
        val blueprint = when {
            roll < 0.55f -> age.units[0] // Melee
            roll < 0.85f -> age.units[1] // Ranged
            else -> age.units[2]         // Heavy
        }

        val enemyHpScale = 1.0f + (_state.value.timelineBattle - 1) * 0.25f
        val enemyDmgScale = 1.0f + (_state.value.timelineBattle - 1) * 0.20f

        val combatUnit = CombatUnit(
            id = UUID.randomUUID().toString(),
            blueprint = blueprint,
            isPlayer = false,
            x = 0.88f,
            currentHp = blueprint.baseHp * enemyHpScale,
            maxHp = blueprint.baseHp * enemyHpScale,
            damage = blueprint.baseDamage * enemyDmgScale
        )
        unitList.add(combatUnit)
    }

    private fun calculateCoinReward(blueprint: WarriorBlueprint): Long {
        val baseCoin = (blueprint.foodCost * 2.5f * (_state.value.age.index + 1)).toLong()
        val goldBonusCard = _state.value.cards.find { it.bonusType == CardBonusType.GOLD_BOOST }
        val cardMultiplier = 1.0f + (goldBonusCard?.level ?: 0) * (goldBonusCard?.bonusPerLevel ?: 0f)
        return max(1L, (baseCoin * cardMultiplier).toLong())
    }

    fun spawnUnit(blueprint: WarriorBlueprint) {
        val s = _state.value
        if (!s.isBattleActive || s.food < blueprint.foodCost) return

        // Card and upgrade multipliers
        val roleLevel = when (blueprint.role) {
            UnitRole.MELEE -> s.unitLevelMelee
            UnitRole.RANGED -> s.unitLevelRanged
            UnitRole.HEAVY -> s.unitLevelHeavy
        }

        val dmgCard = s.cards.find { it.bonusType == CardBonusType.DAMAGE_BOOST }
        val hpCard = s.cards.find { it.bonusType == CardBonusType.HEALTH_BOOST }

        val upgradeDmgMult = 1.0f + (roleLevel - 1) * 0.15f + ((dmgCard?.level ?: 0) * (dmgCard?.bonusPerLevel ?: 0f))
        val upgradeHpMult = 1.0f + (roleLevel - 1) * 0.15f + ((hpCard?.level ?: 0) * (hpCard?.bonusPerLevel ?: 0f))

        val unitHp = blueprint.baseHp * upgradeHpMult
        val unitDmg = blueprint.baseDamage * upgradeDmgMult

        val combatUnit = CombatUnit(
            id = UUID.randomUUID().toString(),
            blueprint = blueprint,
            isPlayer = true,
            x = 0.12f,
            currentHp = unitHp,
            maxHp = unitHp,
            damage = unitDmg
        )

        _state.value = s.copy(
            food = s.food - blueprint.foodCost,
            units = s.units + combatUnit
        )
        sound.playSpawn()
    }

    fun triggerSkill(type: SkillType) {
        val s = _state.value
        val skillIndex = s.skills.indexOfFirst { it.type == type }
        if (skillIndex == -1) return
        val skill = s.skills[skillIndex]
        if (!skill.isReady) return

        sound.playSkillTrigger()

        var currentUnits = s.units
        var bonusCoins = 0L
        val nextFloats = s.floatingTexts.toMutableList()

        when (type) {
            SkillType.METEOR_STRIKE -> {
                currentMeteorTimer = 2.0f
                val meteorDmg = 140f * (s.age.index + 1)
                currentUnits = s.units.map { u ->
                    if (!u.isPlayer && !u.isDead) {
                        val newHp = max(0f, u.currentHp - meteorDmg)
                        nextFloats.add(
                            FloatingCombatText(
                                id = UUID.randomUUID().toString(),
                                x = u.x,
                                yOffset = 25f,
                                text = "METEOR! -${meteorDmg.toInt()}",
                                isCrit = true,
                                isPlayerDamage = true
                            )
                        )
                        if (newHp <= 0f) {
                            bonusCoins += calculateCoinReward(u.blueprint)
                            u.copy(currentHp = 0f, isDead = true, deathTimer = 0f)
                        } else {
                            u.copy(currentHp = newHp)
                        }
                    } else u
                }
                sound.playExplosion()
            }
            SkillType.MORALE_BOOST -> {
                // Active for 6 seconds
            }
            SkillType.FREEZE -> {
                // Freezes enemies for 4 seconds
            }
        }

        if (bonusCoins > 0L) {
            prefs.coins = s.coins + bonusCoins
        }

        val updatedSkills = s.skills.toMutableList()
        updatedSkills[skillIndex] = skill.copy(
            currentCooldown = type.cooldownSec,
            activeDurationRemaining = when (type) {
                SkillType.MORALE_BOOST -> 6.0f
                SkillType.FREEZE -> 4.0f
                SkillType.METEOR_STRIKE -> 2.0f
            }
        )
        _state.value = s.copy(
            coins = s.coins + bonusCoins,
            units = currentUnits,
            floatingTexts = nextFloats,
            skills = updatedSkills
        )
    }

    fun toggleGameSpeed() {
        val current = _state.value.gameSpeed
        val next = when (current) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        _state.value = _state.value.copy(gameSpeed = next)
        sound.playUpgrade()
    }

    fun toggleMute() {
        val next = !sound.isMuted
        sound.isMuted = next
        prefs.isMuted = next
        _state.value = _state.value.copy(isMuted = next)
    }

    fun upgradeFoodProduction() {
        val s = _state.value
        val cost = (15L * s.foodRateLevel * (s.age.index + 1))
        if (s.coins < cost) return

        prefs.coins = s.coins - cost
        prefs.foodRateLevel = s.foodRateLevel + 1
        val newRate = 1.0f + (prefs.foodRateLevel * 0.15f)

        _state.value = s.copy(
            coins = prefs.coins,
            foodRateLevel = prefs.foodRateLevel,
            foodRate = newRate
        )
        sound.playUpgrade()
    }

    fun upgradeBaseHp() {
        val s = _state.value
        val cost = (20L * s.baseHpLevel * (s.age.index + 1))
        if (s.coins < cost) return

        prefs.coins = s.coins - cost
        prefs.baseHpLevel = s.baseHpLevel + 1
        val newMax = 250f + s.age.baseHpBonus + (prefs.baseHpLevel * 60f)
        val delta = 60f

        _state.value = s.copy(
            coins = prefs.coins,
            baseHpLevel = prefs.baseHpLevel,
            playerBase = s.playerBase.copy(
                currentHp = s.playerBase.currentHp + delta,
                maxHp = newMax
            )
        )
        sound.playUpgrade()
    }

    fun upgradeUnitRole(role: UnitRole) {
        val s = _state.value
        val currentLevel = when (role) {
            UnitRole.MELEE -> s.unitLevelMelee
            UnitRole.RANGED -> s.unitLevelRanged
            UnitRole.HEAVY -> s.unitLevelHeavy
        }
        val cost = (25L * currentLevel * (s.age.index + 1))
        if (s.coins < cost) return

        prefs.coins = s.coins - cost
        when (role) {
            UnitRole.MELEE -> {
                prefs.unitLevelMelee = currentLevel + 1
                _state.value = s.copy(coins = prefs.coins, unitLevelMelee = currentLevel + 1)
            }
            UnitRole.RANGED -> {
                prefs.unitLevelRanged = currentLevel + 1
                _state.value = s.copy(coins = prefs.coins, unitLevelRanged = currentLevel + 1)
            }
            UnitRole.HEAVY -> {
                prefs.unitLevelHeavy = currentLevel + 1
                _state.value = s.copy(coins = prefs.coins, unitLevelHeavy = currentLevel + 1)
            }
        }
        sound.playUpgrade()
    }

    fun evolveAge() {
        val s = _state.value
        if (s.age.index >= AgesCatalog.ALL_AGES.size - 1) return
        val cost = s.age.evolveCostCoins
        if (s.coins < cost) return

        val nextAgeIndex = s.age.index + 1
        prefs.coins = s.coins - cost
        prefs.currentAgeIndex = nextAgeIndex
        prefs.timelineBattle = 1 // Reset to battle 1 in new era!
        prefs.gems = s.gems + 50 // Milestone bonus

        sound.playEvolve()
        loadInitialState()
    }

    fun nextBattle() {
        val s = _state.value
        val nextBattleNum = if (s.timelineBattle < 6) s.timelineBattle + 1 else 1
        prefs.timelineBattle = nextBattleNum
        loadInitialState()
    }

    fun restartBattle() {
        loadInitialState()
    }

    fun drawGachaCard() {
        val s = _state.value
        val cost = 20
        if (s.gems < cost) return

        val randomCard = s.cards.random()
        val newLevel = (randomCard.level + 1).coerceAtMost(randomCard.maxLevel)
        prefs.gems = s.gems - cost
        prefs.setCardLevel(randomCard.id, newLevel)

        val updatedCards = s.cards.map {
            if (it.id == randomCard.id) it.copy(level = newLevel) else it
        }

        _state.value = s.copy(gems = prefs.gems, cards = updatedCards)
        sound.playUpgrade()
    }
}
