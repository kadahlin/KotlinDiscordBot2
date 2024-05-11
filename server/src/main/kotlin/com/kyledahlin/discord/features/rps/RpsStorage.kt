/*
 *
 * Copyright 2024 Kyle Dahlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * /
 */

package com.kyledahlin.discord.features.rps

import com.google.cloud.firestore.Firestore
import com.kyledahlin.discord.server.HonkbotLogger
import dev.kord.common.entity.Snowflake
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import com.kyledahlin.discord.features.rps.RpsStorageKeys as keys

@Singleton
class RockPaperScissorsStorage @Inject constructor(
    firestore: Firestore,
) {

    private val _collection = firestore.collection("rps")

    context(HonkbotLogger)
    suspend fun insertRpsGame(
        guildId: Snowflake,
        playerOne: Snowflake,
        playerTwo: Snowflake,
        winner: Snowflake?,
    ) {
        val updateTime = _collection
            .add(RockPaperScissorsStoredGameData(playerOne, playerTwo, winner).toMap(guildId))
            .get()
        debug { "update time for rps was $updateTime" }
    }

    suspend fun getAllRpsGamesForPlayer(snowflake: Snowflake): Collection<RockPaperScissorsStoredGameData> {
        val oneDocs = _collection
            .whereEqualTo(keys.playerOne, snowflake.toString())
            .get()
            .get()
            .documents

        val twoDocs = _collection
            .whereEqualTo(keys.playerOne, snowflake.toString())
            .get()
            .get()
            .documents

        val docs = oneDocs + twoDocs
        return docs.mapNotNull {
            RockPaperScissorsStoredGameData.fromMap(it.data)
        }
    }
}

data class RockPaperScissorsStoredGameData(
    val playerOne: Snowflake,
    val playerTwo: Snowflake,
    val winner: Snowflake?,
) {
    companion object {
        fun fromMap(map: Map<String, Any>): RockPaperScissorsStoredGameData? {
            return try {
                RockPaperScissorsStoredGameData(
                    map[keys.playerOne].sf(),
                    map[keys.playerTwo].sf(),
                    map[keys.winner]?.sf(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(guildId: Snowflake): Map<String, Any> {
        return mapOf(
            keys.guildId to guildId.toString(),
            keys.playerOne to playerOne.toString(),
            keys.playerTwo to playerTwo.toString(),
            keys.winner to winner.toString(),
        )
    }
}

enum class RpsChoice {
    ROCK, PAPER, SCISSORS;

    infix fun winsAgainst(otherChoice: RpsChoice): Boolean? = when {
        this == ROCK -> when (otherChoice) {
            ROCK -> null
            PAPER -> false
            SCISSORS -> true
        }

        this == PAPER -> when (otherChoice) {
            ROCK -> true
            PAPER -> null
            SCISSORS -> false
        }

        else -> when (otherChoice) {
            ROCK -> false
            PAPER -> true
            SCISSORS -> null
        }
    }

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

data class RpsGameState(
    val id: UUID,
    val playerOne: Snowflake,
    val playerTwo: Snowflake,
    val channelId: Snowflake,
    val guildId: Snowflake,
    val choiceOne: RpsChoice? = null,
    val choiceTwo: RpsChoice? = null
) {


    override fun equals(other: Any?): Boolean {
        return other is RpsGameState && other.id == id
    }
}

private fun Any?.sf() = Snowflake(this as String)