package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundTheme
import com.example.ui.theme.FastDropBlue
import com.example.ui.theme.FastDropBlueSoft
import com.example.ui.theme.FastDropCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundThemePickerSheet(
    currentTheme: BackgroundTheme,
    onSelectTheme: (BackgroundTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = FastDropBlueSoft,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = FastDropBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "انتخاب سبک پس‌زمینه (تم بصری)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "طرح‌های گرافیکی جذاب متحرک و تصاویر نوین",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B)
                        )
                    )
                }
            }

            // List of themes
            BackgroundTheme.values().forEach { theme ->
                val isSelected = theme == currentTheme

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("theme_card_${theme.id}")
                        .clickable {
                            onSelectTheme(theme)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) FastDropBlueSoft else Color(0xFFF8FAFC)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) FastDropBlue else Color(0xFFE2E8F0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Swatch preview box
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(getThemePreviewBrush(theme))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            )

                            Column {
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) FastDropBlue else Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = theme.description,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF64748B)
                                    )
                                )
                            }
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = FastDropBlue,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getThemePreviewBrush(theme: BackgroundTheme): Brush {
    return when (theme) {
        BackgroundTheme.AURORA_BLUE -> Brush.linearGradient(
            listOf(Color(0xFFE8F1FF), Color(0xFF1A68FF), Color(0xFF4C8DFF))
        )
        BackgroundTheme.CYBER_NEON -> Brush.linearGradient(
            listOf(Color(0xFF070B19), Color(0xFF00B4D8), Color(0xFF1A68FF))
        )
        BackgroundTheme.FROST_MINIMAL -> Brush.linearGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFBFDBFE))
        )
        BackgroundTheme.MIDNIGHT_PULSE -> Brush.linearGradient(
            listOf(Color(0xFF090D1A), Color(0xFF1E293B), Color(0xFF38BDF8))
        )
        BackgroundTheme.ROYAL_CYAN -> Brush.linearGradient(
            listOf(Color(0xFF0D2554), Color(0xFF0077B6), Color(0xFF48CAE4))
        )
    }
}
