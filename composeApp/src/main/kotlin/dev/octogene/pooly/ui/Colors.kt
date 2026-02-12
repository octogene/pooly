package dev.octogene.pooly.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Teal = Color(color = 0xFF35F0D0)
val TealDarker = Color(color = 0xFF0DC5A5)
val PurpleDark2 = Color(color = 0xFF351d80)
val Gold = Color(color = 0xFFFFB636)
val PurpleDark = Color(color = 0xFF21064E)
val Purple = Color(color = 0xFF2D0C66)
val PurpleLight = Color(color = 0xFF5D3A97)

val lightColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = Purple,
    tertiary = Teal,
    onTertiary = PurpleDark2,
    tertiaryContainer = TealDarker,
    background = PurpleDark,
    onBackground = Color.White,
)

@Composable
fun poolyButtonColors() = ButtonDefaults.buttonColors(
    containerColor = lightColorScheme.tertiary,
    contentColor = lightColorScheme.onTertiary,
    disabledContentColor = lightColorScheme.primary.copy(alpha = 0.5f),
    disabledContainerColor = lightColorScheme.onPrimary.copy(alpha = 0.5f),
)
