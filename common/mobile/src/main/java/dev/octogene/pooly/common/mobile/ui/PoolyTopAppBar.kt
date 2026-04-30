package dev.octogene.pooly.common.mobile.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.painterResource
import dev.octogene.pooly.common.mobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoolyTopAppBar(
    title: String,
    backStack: SnapshotStateList<Destination>,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (backStack.size > 1) {
                IconButton(onClick = { backStack.removeAt(backStack.size - 1) }) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = actions
    )
}