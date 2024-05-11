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

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.kyledahlin.discord.features.HonkbotFeature
import com.kyledahlin.discord.features.rps.RockPaperScissorsFeature
import com.kyledahlin.discord.server.EnvironmentKeys.honkbotTokenEnvKey
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.inject.Named
import javax.inject.Singleton

@Component(modules = [HonkbotModule::class])
@Singleton
interface HonkbotComponent {
    fun client(): HonkbotClient

    @Component.Builder
    interface Builder {
        fun build(): HonkbotComponent

        @BindsInstance
        fun discordToken(@Named(honkbotTokenEnvKey) token: String): Builder

    }
}

@Module
abstract class HonkbotModule {

    companion object {
        @JvmStatic
        @Provides
        @Singleton
        @ElementsIntoSet
        fun providesRules(
            rps: RockPaperScissorsFeature
        ): Set<HonkbotFeature> {
            return setOf(
                rps
            )
        }

        @JvmStatic
        @Provides
        @Singleton
        fun bindsFirestore(): Firestore = FirestoreClient.getFirestore()
    }
}