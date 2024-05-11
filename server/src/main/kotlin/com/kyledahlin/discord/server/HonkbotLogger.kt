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

import org.slf4j.LoggerFactory

class HonkbotLogger {
    private val logger = LoggerFactory.getLogger("com.kyledahlin.discord")

    fun trace(message: () -> String) {
        logger.trace(message())
    }

    fun debug(message: () -> String) {
        logger.debug(message())
    }

    fun info(message: () -> String) {
        logger.info(message())
    }

    fun warn(t: Throwable? = null, message: () -> String) {
        if (t == null) {
            logger.warn(message())
        } else {
            logger.warn(message(), t)
        }
    }

    fun error(t: Throwable? = null, message: () -> String) {
        if (t == null) {
            logger.error(message())
        } else {
            logger.error(message(), t)
        }
    }
}