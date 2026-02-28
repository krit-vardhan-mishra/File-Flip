// Just for demo, might add it later
package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.viewmodels.ProViewModel
import com.just_for_fun.fileflip.ui.theme.LocalAppColors

import com.just_for_fun.fileflip.ui.util.FileIconHelper

// Colors matching Dashboard (Theme-Aware)
private val PrimaryBlue @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark @Composable get() = LocalAppColors.current.background
private val SurfaceDark @Composable get() = LocalAppColors.current.surface
private val TextWhite @Composable get() = LocalAppColors.current.textPrimary
private val TextGray @Composable get() = LocalAppColors.current.textSecondary

data class ProFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isPremium: Boolean = true
)

@Composable
fun ProScreen(
    navController: NavController,
    viewModel: ProViewModel = hiltViewModel()
) {
    val features = listOf(
        ProFeature(
            icon = Icons.Rounded.Star,
            title = "Unlimited Conversions",
            description = "Convert as many Markdown files to PDF as you want, without limits."
        ),
        ProFeature(
            icon = Icons.Rounded.Block,
            title = "Ad-Free Experience",
            description = "Enjoy the app without any banner ads or interruptions."
        ),
        ProFeature(
            icon = Icons.Rounded.Code,
            title = "Advanced File Formats",
            description = "Support for CSV, JSON, YAML, XML, HTML, TXT, and more with rich previews."
        ),
        ProFeature(
            icon = Icons.Rounded.Palette,
            title = "Custom Themes",
            description = "Light/dark modes, custom colors, fonts, and text sizes."
        ),
        ProFeature(
            icon = Icons.Rounded.Edit,
            title = "Enhanced Editor",
            description = "Undo/redo, auto-save, search/replace, and advanced editing modes."
        ),
        ProFeature(
            icon = Icons.Rounded.Security,
            title = "File Encryption",
            description = "Encrypt sensitive files with AES encryption and PIN protection."
        ),
        ProFeature(
            icon = Icons.Rounded.Backup,
            title = "Backup & Restore",
            description = "Local backup to device storage with easy restore options."
        ),
        ProFeature(
            icon = Icons.Rounded.Widgets,
            title = "Home Screen Widgets",
            description = "Quick access widgets for file shortcuts and conversions."
        )
    )

    // Main Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Status Bar Padding
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
            Spacer(modifier = Modifier.height(16.dp))

            // Top Navigation
            ProTopNavigationBar(navController)

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Section
            ProHeroSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Features List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(features) { feature ->
                    ProFeatureItem(feature)
                }
            }

            // Upgrade Button
            ProUpgradeButton()
        }
    }
}

@Composable
fun ProTopNavigationBar(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = PrimaryBlue
            )
        }

        // Title
        Text(
            text = "Pro Features",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )

        // Placeholder for balance
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun ProHeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlue, Color(0xFF2563EB))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unlock Premium Features",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Get unlimited access and advanced tools",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProFeatureItem(feature: ProFeature) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (feature.isPremium) PrimaryBlue.copy(alpha = 0.1f) else FileIconHelper.IconEmerald.copy(alpha = 0.1f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = if (feature.isPremium) PrimaryBlue else FileIconHelper.IconEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            if (feature.isPremium) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Premium",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProUpgradeButton() {
    Button(
        onClick = { /* TODO: Implement in-app purchase */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
    ) {
        Text(
            text = "Upgrade to Pro - $4.99/month",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
    }
}