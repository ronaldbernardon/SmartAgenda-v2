package com.smartagenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.data.AppConfig

@Composable
fun SettingsScreen(
    currentUrl: String,
    currentPassword: String,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var password     by remember { mutableStateOf(currentPassword) }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0EEF8))
            .verticalScroll(rememberScrollState())
    ) {
        // Header violet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF6A35C2), Color(0xFF4A7FD4))))
                .padding(horizontal = 24.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📅", fontSize = 48.sp)
                Text("SmartAgenda", color = Color.White,
                    fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Première connexion", color = Color.White.copy(alpha = 0.80f),
                    fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("v${AppConfig.VERSION}", color = Color.White,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Info serveur (lecture seule)
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🖥️ Serveur", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF43A047)))
                        Text(AppConfig.SERVER_URL,
                            color = Color(0xFF444444), fontSize = 13.sp,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Mot de passe
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🔑 Authentification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff
                                     else Icons.Default.Visibility, null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (errorMsg.isNotEmpty())
                        Text(errorMsg, color = Color(0xFFE53935), fontSize = 13.sp)
                }
            }

            // Info VPN
            Card(modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔒", fontSize = 20.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("VPN WireGuard requis",
                            fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            color = Color(0xFF3A1A80))
                        Text("Activez WireGuard avant d'ouvrir l'app.",
                            color = Color(0xFF555599), fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            Button(
                onClick = {
                    if (password.isEmpty()) errorMsg = "Le mot de passe est requis"
                    else onSave(AppConfig.SERVER_URL, password.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A35C2))
            ) {
                Text("✅  Se connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
