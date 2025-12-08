package com.example.landmarkbangladesh.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.landmarkbangladesh.data.model.Landmark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkCard(
    landmark: Landmark,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image placeholder with category-based color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = when (landmark.category.lowercase()) {
                        "natural heritage", "natural" -> MaterialTheme.colorScheme.primaryContainer
                        "beach", "island" -> MaterialTheme.colorScheme.tertiaryContainer
                        "historical" -> MaterialTheme.colorScheme.secondaryContainer
                        "religious" -> MaterialTheme.colorScheme.errorContainer
                        "archaeological" -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🏛️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = landmark.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = landmark.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = landmark.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category chip and action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category chip
                if (landmark.category.isNotBlank()) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = landmark.category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Action buttons
                if (onEdit != null || onDelete != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onEdit != null) {
                            FilledTonalButton(
                                onClick = onEdit,
                                modifier = Modifier.size(width = 64.dp, height = 32.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Edit", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (onDelete != null) {
                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.size(width = 72.dp, height = 32.dp),
                                contentPadding = PaddingValues(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
