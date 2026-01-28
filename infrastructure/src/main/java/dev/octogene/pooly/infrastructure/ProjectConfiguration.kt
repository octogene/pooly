package dev.octogene.pooly.infrastructure

data class ProjectConfiguration(
    val enableIpv4: Boolean,
    val serverLocation: String,
    val serverType: String,
    val createIp: Boolean,
    val createServer: Boolean
)
