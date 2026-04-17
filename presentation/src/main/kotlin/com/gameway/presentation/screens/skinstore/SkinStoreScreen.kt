package com.gameway.presentation.screens.skinstore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.Currency
import com.gameway.domain.model.Skin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinStoreScreen(
    viewModel: SkinStoreViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("皮肤商店") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "金币")
                        Text("${uiState.playerCoins}", fontSize = 16.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text("已拥有", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.ownedSkins) { skin ->
                                OwnedSkinItem(
                                    skin = skin,
                                    isEquipped = uiState.equippedSkin?.id == skin.id,
                                    onClick = { viewModel.showPreview(skin) }
                                )
                            }
                        }
                    }

                    val coinSkins = uiState.allSkins.filter { it.currency == Currency.COIN && it.id !in uiState.ownedSkins.map { s -> s.id } }
                    if (coinSkins.isNotEmpty()) {
                        item {
                            Text("可购买 (金币)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(coinSkins) { skin ->
                            PurchasableSkinItem(
                                skin = skin,
                                onPurchase = { viewModel.purchaseWithCoins(skin.id) },
                                onClick = { viewModel.showPreview(skin) }
                            )
                        }
                    }

                    val rmbSkins = uiState.allSkins.filter { it.currency == Currency.RMB && it.id !in uiState.ownedSkins.map { s -> s.id } }
                    if (rmbSkins.isNotEmpty()) {
                        item {
                            Text("VIP专属", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(rmbSkins) { skin ->
                            PurchasableSkinItem(
                                skin = skin,
                                onPurchase = { viewModel.purchaseWithRMB(skin.id) },
                                onClick = { viewModel.showPreview(skin) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showPreviewDialog && uiState.selectedSkin != null) {
            SkinPreviewDialog(
                skin = uiState.selectedSkin!!,
                isOwned = uiState.selectedSkin!!.id in uiState.ownedSkins.map { it.id },
                onDismiss = { viewModel.hidePreview() },
                onPurchase = { isRmb ->
                    if (isRmb) viewModel.purchaseWithRMB(uiState.selectedSkin!!.id)
                    else viewModel.purchaseWithCoins(uiState.selectedSkin!!.id)
                },
                onEquip = { viewModel.equipSkin(uiState.selectedSkin!!.id) }
            )
        }
    }
}

@Composable
private fun OwnedSkinItem(skin: Skin, isEquipped: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(80.dp).clickable(onClick = onClick),
        colors = if (isEquipped) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(skin.emoji, fontSize = 32.sp)
                if (isEquipped) Text("已装备", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun PurchasableSkinItem(skin: Skin, onPurchase: () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skin.emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(skin.name, fontWeight = FontWeight.Bold)
                    Text(if (skin.currency == Currency.COIN) "${skin.price} 🪙" else "¥${skin.price / 10.0}", fontSize = 12.sp)
                }
            }
            Button(onClick = onPurchase) { Text("购买") }
        }
    }
}
