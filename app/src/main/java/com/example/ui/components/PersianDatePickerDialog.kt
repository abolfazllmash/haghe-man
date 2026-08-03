package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.domain.calculator.JalaliDate
import com.example.domain.calculator.PersianNumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianDatePickerField(
    label: String,
    selectedDate: JalaliDate,
    onDateSelected: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier,
    usePersianDigits: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.formatString(usePersianDigits),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "انتخاب تاریخ شمسی",
                modifier = Modifier.clickable { showDialog = true }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        PersianDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDialog = false },
            onConfirm = { newDate ->
                onDateSelected(newDate)
                showDialog = false
            },
            usePersianDigits = usePersianDigits
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianDatePickerDialog(
    initialDate: JalaliDate,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit,
    usePersianDigits: Boolean = true
) {
    var year by remember { mutableStateOf(initialDate.year) }
    var month by remember { mutableStateOf(initialDate.month) }
    var day by remember { mutableStateOf(initialDate.day) }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val maxDays = JalaliDate.getJalaliMonthDays(year, month)
    if (day > maxDays) day = maxDays

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "انتخاب تاریخ شمسی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day Selector
                    var expandedDay by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDay,
                        onExpandedChange = { expandedDay = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (usePersianDigits) PersianNumberFormatter.toPersianDigits(day.toString()) else day.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("روز") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDay,
                            onDismissRequest = { expandedDay = false }
                        ) {
                            for (d in 1..maxDays) {
                                DropdownMenuItem(
                                    text = { Text(if (usePersianDigits) PersianNumberFormatter.toPersianDigits(d.toString()) else d.toString()) },
                                    onClick = {
                                        day = d
                                        expandedDay = false
                                    }
                                )
                            }
                        }
                    }

                    // Month Selector
                    var expandedMonth by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = it },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        OutlinedTextField(
                            value = monthNames[month - 1],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ماه") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false }
                        ) {
                            monthNames.forEachIndexed { idx, mName ->
                                DropdownMenuItem(
                                    text = { Text(mName) },
                                    onClick = {
                                        month = idx + 1
                                        expandedMonth = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Selector
                    var expandedYear by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedYear,
                        onExpandedChange = { expandedYear = it },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = if (usePersianDigits) PersianNumberFormatter.toPersianDigits(year.toString()) else year.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("سال") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedYear,
                            onDismissRequest = { expandedYear = false }
                        ) {
                            for (y in 1370..1415) {
                                DropdownMenuItem(
                                    text = { Text(if (usePersianDigits) PersianNumberFormatter.toPersianDigits(y.toString()) else y.toString()) },
                                    onClick = {
                                        year = y
                                        expandedYear = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(JalaliDate(year, month, day)) }) {
                        Text("تأیید")
                    }
                }
            }
        }
    }
}
