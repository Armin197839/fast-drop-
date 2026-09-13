package com.example.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransferProgress
import com.example.model.TransferState
import com.example.ui.theme.FastDropBlue
import com.example.ui.theme.FastDropGreen
import com.example.ui.theme.FastDropRed
import com.example.util.FileUtils

@Composable
fun ProgressCard(
    progress: TransferProgress,
    isSender: Boolean,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isSender) FastDropBlue else FastDropGreen

    val animatedProgress by animateFloatAsState(
        targetValue = if (progress.progress >= 0f) progress.progress else 0f,
        label = "progress_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Status Title and State Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = when (progress.state) {
                        TransferState.DONE -> Icons.Default.CheckCircle to FastDropGreen
                        TransferState.ERROR -> Icons.Default.Error to FastDropRed
                        TransferState.CANCELLED -> Icons.Default.Close to Color(0xFFF39C12)
                        else -> Icons.Default.Bolt to accentColor
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isSender) "وضعیت ارسال فایل" else "وضعیت دریافت فایل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (progress.fileName.isNotBlank()) {
                            Text(
                                text = progress.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Speed Badge
                if (progress.state == TransferState.TRANSFERRING && progress.speedMBps > 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = FileUtils.formatSpeed(progress.speedMBps),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            if (progress.state == TransferState.TRANSFERRING) {
                if (progress.progress >= 0f) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.15f)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.15f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row: transferred / total and percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${FileUtils.formatBytes(progress.transferredBytes)} / ${if (progress.totalBytes > 0) FileUtils.formatBytes(progress.totalBytes) else "نامشخص"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (progress.progress >= 0f) {
                        Text(
                            text = "${(progress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = accentColor
                        )
                    }
                }
            }

            // Status message
            if (progress.status.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = progress.status,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = when (progress.state) {
                        TransferState.DONE -> FastDropGreen
                        TransferState.ERROR -> FastDropRed
                        TransferState.CANCELLED -> Color(0xFFF39C12)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Cancel button
            if (onCancel != null && (progress.state == TransferState.TRANSFERRING || progress.state == TransferState.PREPARING)) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cancel_transfer_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FastDropRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "لغو انتقال",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
