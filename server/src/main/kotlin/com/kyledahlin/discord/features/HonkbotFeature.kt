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

package com.kyledahlin.discord.features

import com.kyledahlin.discord.server.HonkbotLogger
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent

abstract class HonkbotFeature {
    abstract val id: String

    /**
     * Get all user context command names associated with this feature
     */
    context(HonkbotLogger)
    open fun getGuildUserCommands(): Set<String> = emptySet()

    context(HonkbotLogger)
    open suspend fun createSlashCommands(guildId: Snowflake, kord: Kord) {
    }

    context(HonkbotLogger)
    open suspend fun onButtonInteraction(event: ButtonInteractionCreateEvent) {
    }

    context(HonkbotLogger)
    open suspend fun onGuildChatInteraction(event: GuildChatInputCommandInteractionCreateEvent) {
    }

    context(HonkbotLogger)
    open suspend fun onGuildUserCommand(event: GuildUserCommandInteractionCreateEvent) {
    }

}