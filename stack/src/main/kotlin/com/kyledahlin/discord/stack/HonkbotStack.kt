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

import com.kyledahlin.discord.server.EnvironmentKeys.honkbotLogLevelEnvKey
import com.kyledahlin.discord.server.EnvironmentKeys.honkbotTokenEnvKey
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.elasticbeanstalk.*
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplicationVersion.SourceBundleProperty
import software.amazon.awscdk.services.elasticbeanstalk.CfnEnvironment.OptionSettingProperty
import software.amazon.awscdk.services.s3.assets.Asset
import software.amazon.awscdk.services.s3.assets.AssetProps
import software.constructs.Construct

class HonkbotStack(
    scope: Construct,
    id: String,
    props: StackProps,
) : Stack(scope, id, props) {
    init {
        val application = CfnApplication(
            this, "HonkBot", CfnApplicationProps.builder()
                .applicationName("Honkbot")
                .build()
        )
        val instanceOption = CfnEnvironment.OptionSettingProperty.builder()
            .namespace("aws:elasticbeanstalk:environment")
            .optionName("EnvironmentType")
            .value("SingleInstance")
            .build()
        val ec2Option = CfnEnvironment.OptionSettingProperty.builder()
            .namespace("aws:ec2:instances")
            .optionName("InstanceTypes")
            .value("t3.micro")
            .build()
        val bundle = Asset(
            this, "HonkbotFatJar", AssetProps.builder()
                .path("../server/build/libs/server-all.jar")
                .build()
        )
        val version = CfnApplicationVersion(
            this, "HonkbotVersion", CfnApplicationVersionProps
                .builder()
                .applicationName("Honkbot")
                .sourceBundle(
                    SourceBundleProperty.builder()
                        .s3Bucket(bundle.s3BucketName)
                        .s3Key(bundle.s3ObjectKey)
                        .build()
                )
                .build()
        )
        version.addDependency(application)
        CfnEnvironment(
            this, "HonkbotEnv", CfnEnvironmentProps
                .builder()
                .applicationName(application.applicationName)
                .environmentName("HonkbotEnv")
                .solutionStackName("64bit Amazon Linux 2023 v4.2.3 running Corretto 17")
                .versionLabel(version.ref)
                .optionSettings(
                    mutableListOf(
                        instanceOption,
                        ec2Option,
                        beanstalkVariable(honkbotLogLevelEnvKey, "ERROR"),
                        beanstalkVariable(honkbotTokenEnvKey, System.getenv(honkbotTokenEnvKey)!!),
                        OptionSettingProperty.builder()
                            .namespace("aws:autoscaling:launchconfiguration")
                            .optionName("IamInstanceProfile")
                            .value("ecsInstanceRole")
                            .build()
                    )
                )
                .build()
        )
    }

    private fun beanstalkVariable(name: String, value: String) = OptionSettingProperty.builder()
        .namespace("aws:elasticbeanstalk:application:environment")
        .optionName(name)
        .value(value)
        .build()
}