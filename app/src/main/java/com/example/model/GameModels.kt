package com.example.model

enum class UnitRole {
    MELEE,
    RANGED,
    HEAVY
}

enum class ProjectileType {
    ROCK,
    ARROW,
    CROSSBOW_BOLT,
    CANNONBALL,
    ROCKET,
    LASER
}

data class WarriorBlueprint(
    val id: String,
    val name: String,
    val role: UnitRole,
    val foodCost: Int,
    val baseHp: Float,
    val baseDamage: Float,
    val attackRange: Float, // Fraction of screen (0.05 to 0.40)
    val attackCooldownSec: Float,
    val moveSpeed: Float, // Screen units per second
    val projectileType: ProjectileType? = null
)

data class AgeDefinition(
    val index: Int,
    val name: String,
    val subtitle: String,
    val evolveCostCoins: Long,
    val baseHpBonus: Float,
    val skyColorTop: Long,
    val skyColorBottom: Long,
    val groundColor: Long,
    val baseBuildingName: String,
    val units: List<WarriorBlueprint>
)

data class CombatUnit(
    val id: String,
    val blueprint: WarriorBlueprint,
    val isPlayer: Boolean,
    var x: Float, // 0.0 (player base) to 1.0 (enemy base)
    var currentHp: Float,
    val maxHp: Float,
    val damage: Float,
    var attackTimer: Float = 0f,
    var isAttacking: Boolean = false,
    var attackAnimProgress: Float = 0f,
    var walkAnimCycle: Float = 0f,
    var isDead: Boolean = false,
    var deathTimer: Float = 0f
)

data class Projectile(
    val id: String,
    val isPlayer: Boolean,
    var currentX: Float,
    val targetX: Float,
    val damage: Float,
    val speed: Float,
    val type: ProjectileType,
    val startX: Float
)

data class FloatingCombatText(
    val id: String,
    val x: Float,
    var yOffset: Float,
    val text: String,
    val isCrit: Boolean,
    val isPlayerDamage: Boolean,
    var alpha: Float = 1f,
    var lifetime: Float = 0.8f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    var size: Float,
    var alpha: Float,
    var life: Float,
    val maxLife: Float
)

data class CardItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    var level: Int,
    val maxLevel: Int,
    val bonusType: CardBonusType,
    val bonusPerLevel: Float
)

enum class CardBonusType {
    DAMAGE_BOOST,
    HEALTH_BOOST,
    SPEED_BOOST,
    GOLD_BOOST,
    FOOD_SPEED_BOOST
}

enum class SkillType(
    val title: String,
    val cooldownSec: Float,
    val description: String
) {
    METEOR_STRIKE("Meteor Strike", 25f, "Devastating artillery barrage on enemy lines"),
    MORALE_BOOST("Morale Surge", 20f, "Warriors gain +60% move & attack speed for 6s"),
    FREEZE("Glacial Freeze", 30f, "Freezes all enemy units in place for 4s")
}

data class SkillState(
    val type: SkillType,
    var currentCooldown: Float = 0f,
    var activeDurationRemaining: Float = 0f
) {
    val isReady: Boolean get() = currentCooldown <= 0f
    val isActive: Boolean get() = activeDurationRemaining > 0f
    val progress: Float get() = if (type.cooldownSec > 0) (currentCooldown / type.cooldownSec).coerceIn(0f, 1f) else 0f
}

data class BaseTower(
    var currentHp: Float,
    val maxHp: Float,
    val isPlayer: Boolean
)

object AgesCatalog {
    val ALL_AGES: List<AgeDefinition> = listOf(
        AgeDefinition(
            index = 0,
            name = "Stone Age",
            subtitle = "Prehistoric Dawn",
            evolveCostCoins = 120L,
            baseHpBonus = 0f,
            skyColorTop = 0xFF87CEEB,
            skyColorBottom = 0xFFE0F7FA,
            groundColor = 0xFF8D6E63,
            baseBuildingName = "Cave Cavern",
            units = listOf(
                WarriorBlueprint(
                    id = "stone_melee",
                    name = "Clubman",
                    role = UnitRole.MELEE,
                    foodCost = 3,
                    baseHp = 80f,
                    baseDamage = 18f,
                    attackRange = 0.055f,
                    attackCooldownSec = 0.9f,
                    moveSpeed = 0.14f
                ),
                WarriorBlueprint(
                    id = "stone_ranged",
                    name = "Rock Slinger",
                    role = UnitRole.RANGED,
                    foodCost = 6,
                    baseHp = 50f,
                    baseDamage = 14f,
                    attackRange = 0.22f,
                    attackCooldownSec = 1.3f,
                    moveSpeed = 0.12f,
                    projectileType = ProjectileType.ROCK
                ),
                WarriorBlueprint(
                    id = "stone_heavy",
                    name = "Dino Rider",
                    role = UnitRole.HEAVY,
                    foodCost = 14,
                    baseHp = 250f,
                    baseDamage = 38f,
                    attackRange = 0.065f,
                    attackCooldownSec = 1.2f,
                    moveSpeed = 0.10f
                )
            )
        ),
        AgeDefinition(
            index = 1,
            name = "Spartan Age",
            subtitle = "Bronze & Glory",
            evolveCostCoins = 500L,
            baseHpBonus = 250f,
            skyColorTop = 0xFFFFB74D,
            skyColorBottom = 0xFFFFF3E0,
            groundColor = 0xFFBCAAA4,
            baseBuildingName = "Acropolis Fort",
            units = listOf(
                WarriorBlueprint(
                    id = "spartan_melee",
                    name = "Hoplite",
                    role = UnitRole.MELEE,
                    foodCost = 4,
                    baseHp = 130f,
                    baseDamage = 30f,
                    attackRange = 0.06f,
                    attackCooldownSec = 0.85f,
                    moveSpeed = 0.15f
                ),
                WarriorBlueprint(
                    id = "spartan_ranged",
                    name = "Greek Archer",
                    role = UnitRole.RANGED,
                    foodCost = 8,
                    baseHp = 75f,
                    baseDamage = 26f,
                    attackRange = 0.25f,
                    attackCooldownSec = 1.2f,
                    moveSpeed = 0.13f,
                    projectileType = ProjectileType.ARROW
                ),
                WarriorBlueprint(
                    id = "spartan_heavy",
                    name = "War Chariot",
                    role = UnitRole.HEAVY,
                    foodCost = 18,
                    baseHp = 420f,
                    baseDamage = 62f,
                    attackRange = 0.07f,
                    attackCooldownSec = 1.1f,
                    moveSpeed = 0.12f
                )
            )
        ),
        AgeDefinition(
            index = 2,
            name = "Medieval Age",
            subtitle = "Castles & Steel",
            evolveCostCoins = 2200L,
            baseHpBonus = 650f,
            skyColorTop = 0xFF64B5F6,
            skyColorBottom = 0xFFE1F5FE,
            groundColor = 0xFF78909C,
            baseBuildingName = "Stone Castle",
            units = listOf(
                WarriorBlueprint(
                    id = "medieval_melee",
                    name = "Swordsman",
                    role = UnitRole.MELEE,
                    foodCost = 5,
                    baseHp = 200f,
                    baseDamage = 48f,
                    attackRange = 0.06f,
                    attackCooldownSec = 0.8f,
                    moveSpeed = 0.15f
                ),
                WarriorBlueprint(
                    id = "medieval_ranged",
                    name = "Crossbowman",
                    role = UnitRole.RANGED,
                    foodCost = 10,
                    baseHp = 110f,
                    baseDamage = 45f,
                    attackRange = 0.28f,
                    attackCooldownSec = 1.35f,
                    moveSpeed = 0.13f,
                    projectileType = ProjectileType.CROSSBOW_BOLT
                ),
                WarriorBlueprint(
                    id = "medieval_heavy",
                    name = "Armored Knight",
                    role = UnitRole.HEAVY,
                    foodCost = 22,
                    baseHp = 650f,
                    baseDamage = 98f,
                    attackRange = 0.07f,
                    attackCooldownSec = 1.0f,
                    moveSpeed = 0.11f
                )
            )
        ),
        AgeDefinition(
            index = 3,
            name = "Renaissance Age",
            subtitle = "Gunpowder Revolution",
            evolveCostCoins = 8500L,
            baseHpBonus = 1400f,
            skyColorTop = 0xFFFF8A65,
            skyColorBottom = 0xFFFBE9E7,
            groundColor = 0xFF6D4C41,
            baseBuildingName = "Bastion Keep",
            units = listOf(
                WarriorBlueprint(
                    id = "renaissance_melee",
                    name = "Duelist",
                    role = UnitRole.MELEE,
                    foodCost = 6,
                    baseHp = 290f,
                    baseDamage = 75f,
                    attackRange = 0.065f,
                    attackCooldownSec = 0.75f,
                    moveSpeed = 0.16f
                ),
                WarriorBlueprint(
                    id = "renaissance_ranged",
                    name = "Musketeer",
                    role = UnitRole.RANGED,
                    foodCost = 12,
                    baseHp = 160f,
                    baseDamage = 78f,
                    attackRange = 0.30f,
                    attackCooldownSec = 1.4f,
                    moveSpeed = 0.13f,
                    projectileType = ProjectileType.ARROW
                ),
                WarriorBlueprint(
                    id = "renaissance_heavy",
                    name = "Field Cannon",
                    role = UnitRole.HEAVY,
                    foodCost = 28,
                    baseHp = 950f,
                    baseDamage = 160f,
                    attackRange = 0.34f,
                    attackCooldownSec = 2.0f,
                    moveSpeed = 0.09f,
                    projectileType = ProjectileType.CANNONBALL
                )
            )
        ),
        AgeDefinition(
            index = 4,
            name = "Modern Age",
            subtitle = "Steel & Firepower",
            evolveCostCoins = 30000L,
            baseHpBonus = 3000f,
            skyColorTop = 0xFF546E7A,
            skyColorBottom = 0xFFCFD8DC,
            groundColor = 0xFF455A64,
            baseBuildingName = "Iron Bunker",
            units = listOf(
                WarriorBlueprint(
                    id = "modern_melee",
                    name = "Assault Trooper",
                    role = UnitRole.MELEE,
                    foodCost = 8,
                    baseHp = 440f,
                    baseDamage = 115f,
                    attackRange = 0.07f,
                    attackCooldownSec = 0.7f,
                    moveSpeed = 0.16f
                ),
                WarriorBlueprint(
                    id = "modern_ranged",
                    name = "Rocket Gunner",
                    role = UnitRole.RANGED,
                    foodCost = 16,
                    baseHp = 260f,
                    baseDamage = 135f,
                    attackRange = 0.33f,
                    attackCooldownSec = 1.5f,
                    moveSpeed = 0.13f,
                    projectileType = ProjectileType.ROCKET
                ),
                WarriorBlueprint(
                    id = "modern_heavy",
                    name = "Combat Tank",
                    role = UnitRole.HEAVY,
                    foodCost = 36,
                    baseHp = 1600f,
                    baseDamage = 280f,
                    attackRange = 0.35f,
                    attackCooldownSec = 1.6f,
                    moveSpeed = 0.10f,
                    projectileType = ProjectileType.CANNONBALL
                )
            )
        ),
        AgeDefinition(
            index = 5,
            name = "Cyber Future",
            subtitle = "Plasma Supremacy",
            evolveCostCoins = 100000L,
            baseHpBonus = 6500f,
            skyColorTop = 0xFF1A237E,
            skyColorBottom = 0xFF4A148C,
            groundColor = 0xFF212121,
            baseBuildingName = "Plasma Citadel",
            units = listOf(
                WarriorBlueprint(
                    id = "cyber_melee",
                    name = "Cyber Samurai",
                    role = UnitRole.MELEE,
                    foodCost = 10,
                    baseHp = 700f,
                    baseDamage = 190f,
                    attackRange = 0.08f,
                    attackCooldownSec = 0.65f,
                    moveSpeed = 0.18f
                ),
                WarriorBlueprint(
                    id = "cyber_ranged",
                    name = "Plasma Trooper",
                    role = UnitRole.RANGED,
                    foodCost = 20,
                    baseHp = 420f,
                    baseDamage = 220f,
                    attackRange = 0.36f,
                    attackCooldownSec = 1.1f,
                    moveSpeed = 0.14f,
                    projectileType = ProjectileType.LASER
                ),
                WarriorBlueprint(
                    id = "cyber_heavy",
                    name = "Titan Mech",
                    role = UnitRole.HEAVY,
                    foodCost = 50,
                    baseHp = 2600f,
                    baseDamage = 480f,
                    attackRange = 0.38f,
                    attackCooldownSec = 1.4f,
                    moveSpeed = 0.11f,
                    projectileType = ProjectileType.LASER
                )
            )
        )
    )
}
