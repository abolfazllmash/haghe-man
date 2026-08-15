package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyUnit
import com.example.domain.calculator.PersianNumberFormatter

@Composable
fun CurrencyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    usePersianDigits: Boolean = true,
    showWordsEquivalent: Boolean = true,
    presetLabel: String? = null,
    onApplyPreset: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    testTag: String = "currency_input"
) {
    val cleanNumeric = PersianNumberFormatter.parseToLong(value)
    val displayAmount = if (currencyUnit == CurrencyUnit.TOMAN) cleanNumeric / 10L else cleanNumeric

    val words = if (cleanNumeric > 0L) {
        PersianNumberFormatter.currencyToWords(
            cleanNumeric,
            currencyUnit
        )
    } else ""

    val formattedDisplayValue = if (value.isNotEmpty() && cleanNumeric >= 0) {
        PersianNumberFormatter.formatNumber(displayAmount, usePersianDigits, includeCommas = true)
    } else ""

    // متن فیلد به‌همراه موقعیت مکان‌نما نگهداری می‌شود تا پس از درج جداکننده‌ی هزارگان
    // مکان‌نما به ابتدای فیلد نپرد و ارقام بعدی برعکس وارد نشوند.
    var fieldState by remember {
        mutableStateOf(
            TextFieldValue(formattedDisplayValue, TextRange(formattedDisplayValue.length))
        )
    }

    // هر بار که مقدار قالب‌بندی‌شده تغییر می‌کند، متن هم‌گام و مکان‌نما به انتها منتقل می‌شود.
    LaunchedEffect(formattedDisplayValue) {
        if (fieldState.text != formattedDisplayValue) {
            fieldState = TextFieldValue(
                text = formattedDisplayValue,
                selection = TextRange(formattedDisplayValue.length)
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (presetLabel != null && onApplyPreset != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable { onApplyPreset() }
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "⚡ $presetLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = fieldState,
            onValueChange = { input ->
                val clean = PersianNumberFormatter.cleanNumberInput(input.text)
                // مکان‌نما موقتاً در انتهای متن تازه قرار می‌گیرد؛ سپس با مقدار قالب‌بندی‌شده هم‌گام می‌شود.
                fieldState = input.copy(selection = TextRange(input.text.length))
                if (clean.isEmpty()) {
                    onValueChange("")
                } else {
                    val rawNumeric = clean.toLongOrNull() ?: 0L
                    val rialValue = if (currencyUnit == CurrencyUnit.TOMAN) rawNumeric * 10L else rawNumeric
                    onValueChange(rialValue.toString())
                }
            },
            suffix = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (formattedDisplayValue.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "پاک کردن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = currencyUnit.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            modifier = Modifier.fillMaxWidth().testTag(testTag)
        )

        if (showWordsEquivalent && words.isNotEmpty()) {
            Text(
                text = "« $words »",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
