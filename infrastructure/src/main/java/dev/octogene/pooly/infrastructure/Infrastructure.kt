package dev.octogene.pooly.infrastructure

import com.pulumi.Config
import com.pulumi.Context
import com.pulumi.Pulumi
import com.pulumi.core.Output
import com.pulumi.hcloud.HcloudFunctions.getImage
import com.pulumi.hcloud.HcloudFunctions.getPrimaryIp
import com.pulumi.hcloud.HcloudFunctions.getSshKeys
import com.pulumi.hcloud.PrimaryIp
import com.pulumi.hcloud.PrimaryIpArgs
import com.pulumi.hcloud.Server
import com.pulumi.hcloud.ServerArgs
import com.pulumi.hcloud.SshKey
import com.pulumi.hcloud.SshKeyArgs
import com.pulumi.hcloud.inputs.GetImageArgs
import com.pulumi.hcloud.inputs.GetPrimaryIpArgs
import com.pulumi.hcloud.inputs.GetSshKeysArgs
import com.pulumi.hcloud.inputs.ServerPublicNetArgs
import java.io.File

class Infrastructure {
    private lateinit var projectConfiguration: ProjectConfiguration
    private lateinit var context: Context

    fun main(args: Array<String>) {
        Pulumi.run { context ->
            this@Infrastructure.context = context
            loadConfig(context.config())
            val (primaryIpv6, primaryIpv4) = createPrimaryIps()
            val publicNetArgs = createPublicNetArgs(primaryIpv4, primaryIpv6)
            val standardSshKeys = setSshKeys()
            val dockerImageId = getDockerImageId()

            if (projectConfiguration.createServer) {
                Server(
                    "main",
                    ServerArgs.builder()
                        .name("pooly.octogene.dev")
                        .serverType("cx23")
                        .sshKeys(standardSshKeys)
                        .image(dockerImageId)
                        .publicNets(publicNetArgs)
                        .location(projectConfiguration.serverLocation)
                        .build()
                )
            }
        }
    }

    fun loadConfig(config: Config) {
        projectConfiguration =
            config.getObject<ProjectConfiguration>("data", ProjectConfiguration::class.java).get()
    }

    private fun createPublicNetArgs(
        primaryIpv4: Output<out Any?>?,
        primaryIpv6: Output<out Any?>
    ): ServerPublicNetArgs = ServerPublicNetArgs.builder().apply {
        if (primaryIpv4 != null) {
            ipv4Enabled(true)
            ipv4(primaryIpv4.applyValue { it.toString().toInt() })
        } else {
            ipv4Enabled(false)
        }
        ipv6Enabled(true)
        ipv6(
            primaryIpv6.applyValue {
                it.toString().toInt()
            }
        )
    }.build()

    private fun getDockerImageId(): Output<String?>? {
        val dockerImageId = getImage(
            GetImageArgs.builder().name("docker-ce").withArchitecture("x86").build()
        ).applyValue {
            it.name()
        }
        return dockerImageId
    }

    private fun setSshKeys(): Output<List<String?>?>? {
        val standardSshKeys =
            getSshKeys(
                GetSshKeysArgs.builder().withSelector("pooly-infra").build()
            ).applyValue {
                it.sshKeys().stream().map { element -> element.name() }.toList()
            }
        val sshKey = File("id_ed25519_deploy.pub").readText()

        val defaultSshKey = SshKey(
            "default-deploy",
            SshKeyArgs.builder()
                .name("pooly-deploy")
                .publicKey(sshKey)
                .build()
        )

        return standardSshKeys.applyValue { keys ->
            val list = keys.toMutableList()
            defaultSshKey.name().applyValue { list.add(it) }
            list
        }
    }

    private fun createPrimaryIps(): Pair<Output<out Any?>, Output<out Any?>?> {
        val prefix = "${context.projectName()}-${context.stackName()}"
        return if (projectConfiguration.createIp) {
            val primaryIpv4 = if (projectConfiguration.enableIpv4) {
                PrimaryIp(
                    "$prefix-primary_ip-v4",
                    PrimaryIpArgs.builder()
                        .assigneeType("server")
                        .location(projectConfiguration.serverLocation)
                        .autoDelete(true)
                        .type("ipv4")
                        .build()
                )
            } else {
                null
            }
            val primaryIpv6 = PrimaryIp(
                "$prefix-primary_ip-v6",
                PrimaryIpArgs.builder()
                    .assigneeType("server")
                    .location(projectConfiguration.serverLocation)
                    .autoDelete(true)
                    .type("ipv6")
                    .build()
            )
            primaryIpv6.id() to primaryIpv4?.id()
        } else {
            val primaryIpv4 = if (projectConfiguration.enableIpv4) {
                getPrimaryIp(
                    GetPrimaryIpArgs.builder()
                        .name("$prefix-primary_ip-v4").build()
                )
            } else {
                null
            }
            val primaryIpv6 =
                getPrimaryIp(GetPrimaryIpArgs.builder().name("$prefix-primary_ip-v6").build())
            primaryIpv6.applyValue { it.id() } to primaryIpv4?.applyValue { it.id() }
        }
    }
}
