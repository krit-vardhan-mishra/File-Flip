package com.just_for_fun.fileflip.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.just_for_fun.fileflip.ui.theme.LocalAppColors

// Shared Colors (Theme-Aware)
private val PrimaryBlue @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark @Composable get() = LocalAppColors.current.background
private val SurfaceDark @Composable get() = LocalAppColors.current.surface
private val TextWhite @Composable get() = LocalAppColors.current.textPrimary
private val TextGray @Composable get() = LocalAppColors.current.textSecondary
private val CardBackground @Composable get() = LocalAppColors.current.surface

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Check initial permission state
    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Dynamic page count: Block swipe to page 3 (index 2) if permission is not granted
    val pagerState = rememberPagerState(
        pageCount = { if (isPermissionGranted) 3 else 2 }
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) { page ->
        when (page) {
            0 -> WelcomeContent(
                onNextClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
            )
            1 -> PermissionsContent(
                permissionToRequest = permissionToRequest,
                isPermissionGranted = isPermissionGranted,
                onPermissionResult = { isPermissionGranted = it },
                onNextClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
            )
            2 -> TutorialContent(
                onFinishClick = {
                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
    }
}

// ==========================================
// PAGE 1: WELCOME
// ==========================================
@Composable
private fun WelcomeContent(onNextClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Illustration Area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(colors = listOf(PrimaryBlue.copy(alpha = 0.1f), Color.Transparent)),
                    shape = CircleShape
                )
        ) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TextWhite, modifier = Modifier.size(48.dp))
                        Text(".MD", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.background(PrimaryBlue, RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(32.dp))
                        }
                        Text("PDF", color = PrimaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = buildAnnotatedString { append("Welcome to "); withStyle(SpanStyle(color = PrimaryBlue)) { append("File Flip") } },
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = TextWhite, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Convert your any notes to professional PDFs instantly, all offline.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
            color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Pagination Dots
        Row(modifier = Modifier.padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(width = 24.dp, height = 8.dp).clip(CircleShape).background(PrimaryBlue))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
        }

        Button(
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = BackgroundDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

// ==========================================
// PAGE 2: PERMISSIONS
// ==========================================
@Composable
private fun PermissionsContent(
    permissionToRequest: String,
    isPermissionGranted: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onNextClick: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        onPermissionResult(isGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = buildAnnotatedString { append("We need a few "); withStyle(SpanStyle(color = PrimaryBlue)) { append("permissions") } },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextWhite, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To open and save your Markdown files directly on your device, MarkPDF requires access to your local storage.",
            style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Storage", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextWhite)
                Spacer(modifier = Modifier.height(4.dp))
                Text("File System Access", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { launcher.launch(permissionToRequest) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPermissionGranted) Color(0xFF22C55E) else PrimaryBlue, // Green if granted
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isPermissionGranted) "Access Granted" else "Allow Access", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Pagination
            Row(horizontalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(width = 24.dp, height = 8.dp).clip(CircleShape).background(PrimaryBlue))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TextGray.copy(alpha = 0.3f)))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /* Handle skip if needed */ }) {
                    Text("Skip", color = TextGray.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }

                TextButton(
                    onClick = { if (isPermissionGranted) onNextClick() },
                    colors = ButtonDefaults.textButtonColors(contentColor = if (isPermissionGranted) PrimaryBlue else TextGray.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// PAGE 3: TUTORIAL
// ==========================================
@Composable
private fun TutorialContent(onFinishClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text("How it Works", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold), color = TextWhite)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your workflow simplified in three easy steps.", style = MaterialTheme.typography.bodyLarge, color = TextGray)
        Spacer(modifier = Modifier.height(48.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            TutorialStepItem(Icons.Default.FolderOpen, "1. Open File", "Import your markdown or text files instantly from your device.", false)
            TutorialStepItem(Icons.Default.EditNote, "2. Edit & Preview", "Real-time editing with a seamless live PDF visualization experience.", false)
            TutorialStepItem(Icons.Default.PictureAsPdf, "3. Export PDF", "Save and share professional-grade documents with one tap.", true)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onFinishClick,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = BackgroundDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Using FlipFile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
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
private fun TutorialStepItem(icon: ImageVector, title: String, description: String, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 12.dp)
                        .background(Brush.verticalGradient(listOf(PrimaryBlue.copy(alpha = 0.2f), Color.Transparent)))
                )
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.padding(bottom = 40.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextGray, lineHeight = 20.sp)
        }
    }
}