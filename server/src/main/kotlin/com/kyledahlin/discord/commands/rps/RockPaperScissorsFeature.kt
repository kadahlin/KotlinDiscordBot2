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

package com.kyledahlin.discord.commands.rps

import com.google.firebase.cloud.FirestoreClient
import com.kyledahlin.discord.commands.HonkbotFeature
import com.kyledahlin.discord.server.HonkbotLogger
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updateEphemeralMessage
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.service.RestClient
import java.util.*

class RockPaperScissorsFeature(
    private val storage: RockPaperScissorsStorage = RockPaperScissorsStorage(FirestoreClient.getFirestore())
) : HonkbotFeature() {

    companion object {
        private const val COMMAND_NAME = "RockPaperScissors"
    }

    override val id: String
        get() = "rps"

    override fun getGuildUserCommands(): Set<String> {
        return setOf(COMMAND_NAME)
    }

    private val ongoingGames = mutableMapOf<String, RpsGameState>()

    context(HonkbotLogger)
    suspend fun onInteraction(event: ApplicationCommandInteractionCreateEvent) {
        if (event.interaction.invokedCommandName != COMMAND_NAME || event !is GuildUserCommandInteractionCreateEvent) return
        val response = event.interaction.deferEphemeralResponse()
        val user = event.interaction.user
        val target = event.interaction.target.asMember(event.interaction.guildId)
        val gameId = UUID.randomUUID()
        val guildId = event.interaction.guildId
        ongoingGames[gameId.toString()] =
            RpsGameState(
                guildId = guildId,
                playerOne = user.id,
                playerTwo = target.id,
                channelId = event.interaction.channelId,
                id = gameId
            )
        if (user.id == target.id) {
            response.respond { content = "Can not challenge yourself (you would lose)" }
        }
        else if (event.interaction.getTarget().isBot) {
            response.respond { content = "Can not challenge a bot (you would lose)" }
        } else {
            response.respond {
                content = "you have challenged ${target.effectiveName}"
                actionRow { rpsChoiceButtons(gameId) }
            }
            target.getDmChannel().createMessage {
                content = "You have been challenged to rock paper scissors by ${user.effectiveName}"
                actionRow { rpsChoiceButtons(gameId) }
            }
        }
    }

    private fun ActionRowBuilder.rpsChoiceButtons(gameId: UUID) {
        RpsChoice.entries.map {
            interactionButton(ButtonStyle.Primary, createChoiceButtonId(gameId.toString(), it)) {
                this.label = it.name.lowercase()
            }
        }
    }

    context (HonkbotLogger)
    suspend fun onButtonInteraction(event: ButtonInteractionCreateEvent) {
        if (!event.interaction.componentId.startsWith("rps")) return
        val rpsData = getGameDataFromButton(event.interaction)
        if (rpsData == null) {
            debug { "didnt get anything from this rps button event" }
            event.interaction.respondPublic {
                content = "bot error!"
            }
        } else {
            trace { "detected button data of $rpsData" }
            val ongoing = ongoingGames[rpsData.gameId]
            if (ongoing == null) {
                event.interaction.updatePublicMessage {
                    content = "Outdated game!"
                    components = mutableListOf()
                }
            } else {
                event.interaction.updateEphemeralMessage {
                    content = "you chose ${rpsData.choice}!"
                    components = mutableListOf()
                }
                val newState = updateGameState(event.interaction.user.id, ongoing, rpsData.choice)
                broadcastPotentialWinner(newState, event.kord.rest)
            }
        }
    }

    context(HonkbotLogger)
    private suspend fun broadcastPotentialWinner(state: RpsGameState, client: RestClient) {
        if (state.choiceOne != null && state.choiceTwo != null) {
            val winner = when (state.choiceOne.winsAgainst(state.choiceTwo)) {
                true -> state.playerOne
                false -> state.playerTwo
                else -> null
            }
            storage.insertRpsGame(state.guildId, state.playerOne, state.playerTwo, winner)
            val playerOneName = client.guild.getGuildMember(state.guildId, state.playerOne).nick
            val playerTwoName = client.guild.getGuildMember(state.guildId, state.playerTwo).nick
            val endMessage = if (winner == null) {
                "$playerOneName and $playerTwoName had a draw! Both chose ${state.choiceOne}"
            } else {
                val (winnerName, loserName) = if (winner == state.playerOne) playerOneName to playerTwoName else playerTwoName to playerOneName
                "$winnerName won at rock paper scissors against $loserName! Ouch!"
            }
            client.channel.createMessage(state.channelId) {
                content = endMessage
            }
            ongoingGames.remove(state.id.toString())
        }
    }

    context(HonkbotLogger)
    private suspend fun updateGameState(userId: Snowflake, ongoing: RpsGameState, choice: RpsChoice): RpsGameState {
        val newState = if (userId == ongoing.playerOne) {
            ongoing.copy(choiceOne = choice)
        } else {
            ongoing.copy(choiceTwo = choice)
        }
        ongoingGames[ongoing.id.toString()] = newState
        return newState
    }

    private fun getGameDataFromButton(interaction: ButtonInteraction): RpsButtonResponse? {
        val regex = Regex("""rps_([[a-z0-9A-Z]|-]+)_([a-zA-Z]+)""")
        val matchResult = regex.find(interaction.componentId)

        return if (matchResult != null) {
            val gameId = matchResult.groupValues[1]
            val stringChoice = matchResult.groupValues[2]
            RpsButtonResponse(gameId, RpsChoice.valueOf(stringChoice.uppercase()))
        } else null
    }

    private fun createChoiceButtonId(gameId: String, choice: RpsChoice) = "rps_${gameId}_${choice.name}"
}

data class RpsButtonResponse(val gameId: String, val choice: RpsChoice)