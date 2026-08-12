package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CalculationType
import com.example.domain.calculator.PersianNumberFormatter
import com.example.ui.theme.accentFor
import com.example.ui.theme.borderColor

fun iconForModule(calculationType: CalculationType): ImageVector = when (calculationType) {
    CalculationType.PAYSLIP -> Icons.Default.ReceiptLong
    CalculationType.BONUS_EIDI -> Icons.Default.CardGiftcard
    CalculationType.SEVERANCE -> Icons.Default.Shield
    CalculationType.LEAVE_BALANCE -> Icons.Default.EventBusy
    CalculationType.OVERTIME_SHIFTS -> Icons.Default.Schedule
    CalculationType.TAX -> Icons.Default.AccountBalance
    CalculationType.INSURANCE -> Icons.Default.HealthAndSafety
    CalculationType.UNEMPLOYMENT -> Icons.Default.WorkHistory
}

/** "۳۰ ثانیه" / "۱ دقیقه" / "۱.۵ دقیقه" */
private fun durationLabel(seconds: Int, usePersianDigits: Boolean): String {
    val raw = when {
        seconds < 60 -> "$seconds ثانیه"
        seconds % 60 == 0 -> "${seconds / 60} دقیقه"
        else -> "${seconds / 60}.${(seconds % 60) * 10 / 60} دقیقه"
    }
    return PersianNumberFormatter.toPersianDigits(raw, usePersianDigits)
}

@Composable
fun ModuleCard(
    calculationType: CalculationType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPopular: Boolean = false,
    usePersianDigits: Boolean = true
) {
    val accent = accentFor(calculationType)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "module_card_scale")

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .semantics { contentDescription = calculationType.title }
            .testTag("module_card_${calculationType.name}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accent.cardBg),
        border = BorderStroke(1.dp, accent.borderColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 136.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = iconForModule(calculationType),
                                contentDescription = null,
                                tint = accent.tint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (isPopular) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accent.tint.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "محبوب‌ترین",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent.tint,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = calculationType.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaChip(
                    icon = Icons.Default.Inventory2,
                    label = PersianNumberFormatter.toPersianDigits(
                        "${calculationType.inputCount} ورودی",
                        usePersianDigits
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetaChip(
                    icon = Icons.Default.Schedule,
                    label = durationLabel(calculationType.estimatedSeconds, usePersianDigits),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: ImageVector,
    label: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}
