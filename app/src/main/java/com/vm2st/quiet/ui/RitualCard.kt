package com.vm2st.quiet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vm2st.quiet.R
import com.vm2st.quiet.data.Ritual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualCard(
    ritual: Ritual,
    onToggleConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ritual.isConfirmedToday) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ritual.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToggleConfirm,
                    modifier = Modifier
                        .widthIn(min = 100.dp, max = 130.dp)
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ritual.isConfirmedToday)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (ritual.isConfirmedToday)
                            stringResource(R.string.done)
                        else
                            stringResource(R.string.did_it),
                        maxLines = 2,
                        softWrap = true,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}