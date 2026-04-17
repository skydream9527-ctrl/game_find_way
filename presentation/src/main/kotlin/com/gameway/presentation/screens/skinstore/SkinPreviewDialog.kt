package com.gameway.presentation.screens.skinstore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gameway.domain.model.Currency
import com.gameway.domain.model.Skin

@Composable
fun SkinPreviewDialog(
    skin: Skin,
    isOwned: Boolean,
    onDismiss: () -> Unit,
    onPurchase: (isRmb: Boolean) -> Unit,
    onEquip: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("皮肤预览", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Text(skin.emoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(skin.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (skin.currency == Currency.COIN) "${skin.price} 🪙" else "¥${skin.price / 10.0}",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isOwned) {
                    Button(onClick = onEquip, modifier = Modifier.fillMaxWidth()) {
                        Text("装备")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (skin.currency == Currency.RMB) {
                            Button(onClick = { onPurchase(true) }) {
                                Text("¥购买")
                            }
                        } else {
                            Button(onClick = { onPurchase(false) }) {
                                Text("🪙购买")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    }
}
