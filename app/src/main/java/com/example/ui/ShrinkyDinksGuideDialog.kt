package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShrinkyDinksGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Shrinky Dinks Tracing Tips", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                TipItem(
                    title = "1. Size It 2.5x Larger",
                    description = "Shrink plastic shrinks by ~60% in the oven and becomes 9x thicker. Size your image 2 to 3 times bigger than the final charm you want!"
                )

                TipItem(
                    title = "2. Use 'Mirror' for Lettering",
                    description = "Colored pencils adhere best to the frosted/sanded side. Because you view the finished charm from the glossy front, tap 'Mirror' to reverse text before tracing!"
                )

                TipItem(
                    title = "3. Punch Holes Before Baking",
                    description = "If you're making keychains or jewelry charms, punch your hole now with a standard 1/4\" hole punch. It will shrink down to jump-ring size."
                )

                TipItem(
                    title = "4. Lock for 100% Brightness & Zero Touch Slips",
                    description = "Tapping 'Lock' turns your device screen all the way up and locks touches, so your resting hand won't move the drawing. Drag the lock button to any corner out of your way."
                )

                TipItem(
                    title = "5. Oven Baking (325°F / 165°C)",
                    description = "Bake on parchment paper for 2-3 minutes. Don't panic when it curls up like a taco — it will flatten itself out completely once fully shrunk!"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("guide_dialog_got_it_button")
            ) {
                Text("Got It! Let's Trace")
            }
        },
        modifier = Modifier.testTag("shrinky_guide_dialog")
    )
}

@Composable
private fun TipItem(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
