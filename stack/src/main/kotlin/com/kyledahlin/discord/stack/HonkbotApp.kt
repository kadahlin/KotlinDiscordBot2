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

package com.kyledahlin.discord.stack

import com.kyledahlin.discord.server.EnvironmentKeys.honkbotAccountIdEnvKey
import com.kyledahlin.discord.server.EnvironmentKeys.honkbotRegionEnvKey
import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps

fun main() {
    val app = App()

    HonkbotStack(
        app, "honkbot-prod-stack",
        StackProps.builder()
            .env(
                Environment.builder()
                    .account(System.getenv(honkbotAccountIdEnvKey))
                    .region(System.getenv(honkbotRegionEnvKey))
                    .build()
            )
            .build()
    )

    app.synth()
}