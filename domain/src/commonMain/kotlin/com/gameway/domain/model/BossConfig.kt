package com.gameway.domain.model

data class BossConfig(
    val type: BossType,
    val name: String,
    val emoji: String,
    val attackPatterns: List<AttackPattern>,
    val moveSpeed: Float,
    val isFlying: Boolean,
    val health: Int = 1
) {
    companion object {
        val SLIME = BossConfig(BossType.SLIME, "史莱姆", "🟢", listOf(AttackPattern.PROJECTILE), 2f, false)
        val BAT = BossConfig(BossType.BAT, "蝙蝠", "🦇", listOf(AttackPattern.DASH), 5f, true)
        val DRAGON = BossConfig(BossType.DRAGON, "龙", "🐉", listOf(AttackPattern.LASER), 3f, true)
        val GOLEM = BossConfig(BossType.GOLEM, "石头人", "🗿", listOf(AttackPattern.PROJECTILE, AttackPattern.DASH), 2f, false)
        val DEMON = BossConfig(BossType.DEMON, "恶魔", "👹", AttackPattern.entries, 4f, true)

        fun getForChapter(chapter: Int): BossConfig {
            return when {
                chapter <= 1 -> SLIME
                chapter <= 2 -> BAT
                chapter <= 3 -> DRAGON
                chapter <= 4 -> GOLEM
                else -> DEMON
            }
        }
    }
}
