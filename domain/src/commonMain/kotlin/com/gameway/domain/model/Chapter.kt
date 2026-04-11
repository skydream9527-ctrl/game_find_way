package com.gameway.domain.model

data class ChapterTheme(
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val platformColor: Long
)

data class Chapter(
    val id: Int,
    val name: String,
    val emoji: String,
    val theme: ChapterTheme,
    val unlocked: Boolean = true,
    val completedLevels: Int = 0,
    val totalLevels: Int = 10
) {
    companion object {
        fun getAllChapters(): List<Chapter> = listOf(
            Chapter(1, "精灵森林", "🧝", ChapterTheme(0xFF4CAF50, 0xFFFFD700, 0xFF1B5E20, 0xFF5D4037)),
            Chapter(2, "蘑菇沼泽", "🍄", ChapterTheme(0xFF9C27B0, 0xFF76FF03, 0xFF4A148C, 0xFF795548)),
            Chapter(3, "矮人矿洞", "⛏️", ChapterTheme(0xFF795548, 0xFFFF9800, 0xFF3E2723, 0xFF6D4C41)),
            Chapter(4, "龙巢火山", "🐉", ChapterTheme(0xFFF44336, 0xFF212121, 0xFFB71C1C, 0xFF424242)),
            Chapter(5, "幽灵古堡", "👻", ChapterTheme(0xFF1A237E, 0xFF757575, 0xFF0D47A1, 0xFF616161)),
            Chapter(6, "魔法高塔", "🔮", ChapterTheme(0xFF7B1FA2, 0xFFB0BEC5, 0xFF4A148C, 0xFF9E9E9E)),
            Chapter(7, "海神之殿", "🌊", ChapterTheme(0xFF2196F3, 0xFF00BCD4, 0xFF01579B, 0xFF455A64)),
            Chapter(8, "暗影迷宫", "🌙", ChapterTheme(0xFF212121, 0xFF7B1FA2, 0xFF000000, 0xFF424242)),
            Chapter(9, "天空之城", "☁️", ChapterTheme(0xFFFFFFFF, 0xFFFFD700, 0xFF87CEEB, 0xFFE0E0E0)),
            Chapter(10, "星空神殿", "⭐", ChapterTheme(0xFF1A237E, 0xFFFFD700, 0xFF0D47A1, 0xFF616161))
        )
    }
}