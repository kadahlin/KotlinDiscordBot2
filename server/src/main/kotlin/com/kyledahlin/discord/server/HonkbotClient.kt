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

package com.kyledahlin.discord.server

import com.kyledahlin.discord.features.HonkbotFeature
import com.kyledahlin.discord.server.EnvironmentKeys.honkbotTokenEnvKey
import dev.kord.core.Kord
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.core.on
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class HonkbotClient @Inject constructor(
    @Named(honkbotTokenEnvKey) private val token: String,
    private val logger: HonkbotLogger,
    private val features: Set<@JvmSuppressWildcards HonkbotFeature>
) {
    private lateinit var client: Kord

    suspend fun start() = with(logger) {
        client = Kord(token)

        with(client) {
            on<ReadyEvent> {
                debug { "ready event for [${guilds.size}] guilds as [${this.self.effectiveName}]" }
            }
            on<GuildUserCommandInteractionCreateEvent> {
                features.forEach { it.onGuildUserCommand(this) }
            }
            on<GuildCreateEvent> {
                deleteLegacyCommands()
                features.flatMap { it.getGuildUserCommands() }.forEach { name ->
                    client.createGuildUserCommand(guild.id, name)
                }
            }
            on<ButtonInteractionCreateEvent> {
                trace { "button event with id [${interaction.componentId}]" }
                features.forEach { it.onButtonInteraction(this) }
            }
            on<GuildChatInputCommandInteractionCreateEvent> {
                trace { "guild chat input in [${interaction.guildId}] with id [${interaction.invokedCommandName}]" }
                features.forEach { it.onGuildChatInteraction(this) }
            }
            login {}
        }
    }

    /**
     * Remove all commands from the previous iteration of the bot
     */
    context(HonkbotLogger)
    private suspend fun GuildCreateEvent.deleteLegacyCommands() {
        client.getGuildApplicationCommands(guild.id).collect { command ->
            if (command.name in legacyCommands) {
                info { "deleting legacy [${command.name}] from [${guild.name}]" }
                client.getGuildApplicationCommand(guild.id, command.id).delete()
            }
        }
    }

    companion object {
        // Command names present in the old bot that still live on some of my personal servers
        private val legacyCommands = setOf(
            "pung",
            "echo",
            "jojo",
            "corona",
            "rockpaperscissors",
            "rps_user",
            "Challenge RPS",
            "wellness-register",
            "todays-mods"
        )
    }
}