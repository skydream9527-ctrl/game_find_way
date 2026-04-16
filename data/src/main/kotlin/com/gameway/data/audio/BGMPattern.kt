package com.gameway.data.audio

enum class BGMPattern(
    val notes: List<Note>,
    val bpm: Int
) {
    EASY(listOf(
        Note(261.63f, 500), Note(293.66f, 500), Note(329.63f, 500), Note(349.23f, 500),
        Note(392.00f, 500), Note(349.23f, 500), Note(329.63f, 500), Note(293.66f, 500)
    ), 60),

    MEDIUM(listOf(
        Note(293.66f, 333), Note(329.63f, 333), Note(349.23f, 333), Note(392.00f, 333),
        Note(440.00f, 333), Note(392.00f, 333), Note(349.23f, 333), Note(329.63f, 333),
        Note(293.66f, 333), Note(261.63f, 333), Note(293.66f, 333), Note(329.63f, 333)
    ), 90),

    HARD(listOf(
        Note(220.00f, 250), Note(261.63f, 250), Note(293.66f, 250), Note(329.63f, 250),
        Note(349.23f, 250), Note(392.00f, 250), Note(440.00f, 250), Note(493.88f, 250),
        Note(523.25f, 250), Note(493.88f, 250), Note(440.00f, 250), Note(392.00f, 250)
    ), 120),

    EXPERT(listOf(
        Note(164.81f, 214), Note(196.00f, 214), Note(220.00f, 214), Note(246.94f, 214),
        Note(261.63f, 214), Note(293.66f, 214), Note(329.63f, 214), Note(349.23f, 214),
        Note(392.00f, 214), Note(440.00f, 214), Note(493.88f, 214), Note(523.25f, 214),
        Note(587.33f, 214), Note(659.25f, 214)
    ), 140),

    BOSS(listOf(
        Note(110.00f, 200), Note(130.81f, 200), Note(146.83f, 200), Note(164.81f, 200),
        Note(174.61f, 200), Note(196.00f, 200), Note(220.00f, 200), Note(246.94f, 200),
        Note(261.63f, 200), Note(293.66f, 200), Note(329.63f, 200), Note(349.23f, 200)
    ), 150)
}