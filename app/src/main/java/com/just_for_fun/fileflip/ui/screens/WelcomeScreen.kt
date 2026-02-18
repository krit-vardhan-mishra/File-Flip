package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Color Constants matching Dashboard
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val SurfaceDark = Color(0xFF1A2830)
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // --- Central Illustration Area ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        ) {
            // Inner circle/card
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(SurfaceDark.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // MD Icon
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Markdown",
                            tint = TextWhite,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(".MD", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Arrow / Flow visual
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Convert",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // PDF Icon
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryBlue, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = BackgroundDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text("PDF", color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- Typography ---
        Text(
            text = buildAnnotatedString {
                append("Welcome to ")
                withStyle(style = SpanStyle(color = PrimaryBlue)) {
                    append("MarkPDF")
                }
            },
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = TextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Convert your Markdown notes to professional PDFs instantly, all offline.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- Bottom Actions ---

        // Pagination Dots
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(width = 24.dp, height = 8.dp).clip(CircleShape).background(PrimaryBlue))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
        }

        // CTA Button
        Button(
            onClick = { navController.navigate("permissions") },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = BackgroundDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}