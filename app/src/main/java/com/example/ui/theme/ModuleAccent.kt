package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import com.example.data.model.CalculationType

/**
 * Visual identity for a single calculator module: a pastel card background and a
 * saturated icon tint. In dark mode the pastel is composited at low alpha over the
 * surface color and the tint is lightened so contrast stays readable.
 */
@Immutable
data class ModuleAccent(
    val cardBg: Color,
    val tint: Color
)

private fun lightAccentFor(type: CalculationType): ModuleAccent = when (type) {
    CalculationType.PAYSLIP -> ModuleAccent(ModuleBlueBg, ModuleBlueTint)
    CalculationType.BONUS_EIDI -> ModuleAccent(ModulePurpleBg, ModulePurpleTint)
    CalculationType.SEVERANCE -> ModuleAccent(ModuleAmberBg, ModuleAmberTint)
    CalculationType.LEAVE_BALANCE -> ModuleAccent(ModulePinkBg, ModulePinkTint)
    CalculationType.OVERTIME_SHIFTS -> ModuleAccent(ModuleTealBg, ModuleTealTint)
    CalculationType.TAX -> ModuleAccent(ModuleOliveBg, ModuleOliveTint)
    CalculationType.INSURANCE -> ModuleAccent(ModuleRoseBg, ModuleRoseTint)
    CalculationType.NET_GROSS_CONVERTER -> ModuleAccent(ModuleCyanBg, ModuleCyanTint)
    CalculationType.UNEMPLOYMENT -> ModuleAccent(ModuleIndigoBg, ModuleIndigoTint)
}

/** Pushes a color toward white so it stays legible on a dark background. */
private fun Color.lighten(fraction: Float): Color = Color(
    red = red + (1f - red) * fraction,
    green = green + (1f - green) * fraction,
    blue = blue + (1f - blue) * fraction,
    alpha = alpha
)

@Composable
fun accentFor(type: CalculationType): ModuleAccent {
    val base = lightAccentFor(type)
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    return if (!isDark) {
        base
    } else {
        ModuleAccent(
            cardBg = base.tint.copy(alpha = 0.14f).compositeOver(scheme.surface),
            tint = base.tint.lighten(0.42f)
        )
    }
}

/** Slightly stronger version of the card background, used for hairline borders. */
@Composable
fun ModuleAccent.borderColor(): Color = tint.copy(alpha = 0.18f)
