package com.just_for_fun.fileflip.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Color Constants matching Dashboard
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)

@Composable
fun TutorialScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // --- Header ---
        Text(
            text = "How it Works",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your workflow simplified in three easy steps.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- Steps List ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            TutorialStepItem(
                icon = Icons.Default.FolderOpen,
                title = "1. Open File",
                description = "Import your markdown or text files instantly from your device.",
                isLast = false
            )
            TutorialStepItem(
                icon = Icons.Default.EditNote,
                title = "2. Edit & Preview",
                description = "Real-time editing with a seamless live PDF visualization experience.",
                isLast = false
            )
            TutorialStepItem(
                icon = Icons.Default.PictureAsPdf,
                title = "3. Export PDF",
                description = "Save and share professional-grade documents with one tap.",
                isLast = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Footer ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // CTA Button
            Button(
                onClick = {
                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
                    navController.navigate("dashboard")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Start Using MarkPDF",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pagination Dots
            Row(horizontalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(width = 24.dp, height = 8.dp).clip(CircleShape).background(PrimaryBlue))
            }
        }
    }
}

@Composable
fun TutorialStepItem(
    icon: ImageVector,
    title: String,
    description: String,
    isLast: Boolean
) {
    IntrinsicHeightRow(
        isLast = isLast,
        iconContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        textContent = {
            Column(modifier = Modifier.padding(bottom = 40.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    lineHeight = 20.sp
                )
            }
        }
    )
}

@Composable
fun IntrinsicHeightRow(
    isLast: Boolean,
    iconContent: @Composable () -> Unit,
    textContent: @Composable () -> Unit
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            iconContent()
            if (!isLast) {
                // Vertical Line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 12.dp) // Spacing from icon
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(PrimaryBlue.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        textContent()
    }
}