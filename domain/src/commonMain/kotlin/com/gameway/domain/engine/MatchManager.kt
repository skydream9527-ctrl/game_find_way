package com.gameway.domain.engine

import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchResult
import com.gameway.domain.model.MatchStatus
import com.gameway.domain.repository.MatchRepository
import java.util.UUID

class MatchManager(
    private val matchRepository: MatchRepository
) {
    suspend fun createMatchRequest(
        challengerId: String,
        challengerName: String,
        challengedId: String,
        challengedName: String,
        chapter: Int,
        level: Int
    ): Match {
        val match = Match(
            id = UUID.randomUUID().toString(),
            challengerId = challengerId,
            challengerName = challengerName,
            challengedId = challengedId,
            challengedName = challengedName,
            chapter = chapter,
            level = level,
            status = MatchStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        matchRepository.createMatch(match)
        return match
    }

    suspend fun acceptMatch(matchId: String): Match {
        val match = matchRepository.getMatch(matchId)!!
        val updated = match.copy(status = MatchStatus.ACCEPTED)
        matchRepository.updateMatch(updated)
        return updated
    }

    suspend fun rejectMatch(matchId: String) {
        val match = matchRepository.getMatch(matchId)!!
        val updated = match.copy(status = MatchStatus.REJECTED)
        matchRepository.updateMatch(updated)
    }

    suspend fun submitScore(matchId: String, playerId: String, score: Int): MatchResult {
        val match = matchRepository.getMatch(matchId)!!

        val updated = if (playerId == match.challengerId) {
            match.copy(challengerScore = score)
        } else {
            match.copy(challengedScore = score)
        }

        val finalMatch = matchRepository.updateMatch(updated)

        if (finalMatch.challengerScore > 0 && finalMatch.challengedScore > 0) {
            return calculateResult(finalMatch)
        }

        return MatchResult(
            matchId = matchId,
            winnerId = null,
            winnerName = null,
            isDraw = false,
            challengerScore = finalMatch.challengerScore,
            challengedScore = finalMatch.challengedScore
        )
    }

    private suspend fun calculateResult(match: Match): MatchResult {
        val winnerId: String?
        val winnerName: String?
        val isDraw = match.challengerScore == match.challengedScore

        if (isDraw) {
            winnerId = null
            winnerName = null
        } else if (match.challengerScore > match.challengedScore) {
            winnerId = match.challengerId
            winnerName = match.challengerName
        } else {
            winnerId = match.challengedId
            winnerName = match.challengedName
        }

        val completed = match.copy(
            status = MatchStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )
        matchRepository.updateMatch(completed)

        return MatchResult(
            matchId = match.id,
            winnerId = winnerId,
            winnerName = winnerName,
            isDraw = isDraw,
            challengerScore = match.challengerScore,
            challengedScore = match.challengedScore
        )
    }

    suspend fun getPendingMatches(playerId: String): List<Match> {
        return matchRepository.getPendingMatchesForPlayer(playerId)
    }

    suspend fun getActiveMatches(): List<Match> {
        return matchRepository.getActiveMatches()
    }
}
