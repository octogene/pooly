package dev.octogene.pooly.common.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

class Destination(val name: String, val topBar: (@Composable () -> Unit)? = null, val ui: @Composable () -> Unit)

fun interface DestinationBuilder {
    operator fun invoke(backStack: SnapshotStateList<Destination>): Destination
}
