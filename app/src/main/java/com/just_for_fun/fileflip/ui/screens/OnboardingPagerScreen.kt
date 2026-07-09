package com.just_for_fun.fileflip.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.components.onboarding.PermissionsContent
import com.just_for_fun.fileflip.ui.components.onboarding.TutorialContent
import com.just_for_fun.fileflip.ui.components.onboarding.WelcomeContent
import com.just_for_fun.fileflip.ui.util.BackgroundDark
import kotlinx.coroutines.launch
import androidx.core.content.edit

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
            1 -> {
                var isMicPermissionGranted by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    )
                }
                PermissionsContent(
                    context = context,
                    navController = navController,
                    permissionToRequest = permissionToRequest,
                    isPermissionGranted = isPermissionGranted,
                    isMicPermissionGranted = isMicPermissionGranted,
                    onPermissionResult = { isPermissionGranted = it },
                    onMicPermissionResult = { isMicPermissionGranted = it },
                    onNextClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                )
            }
            2 -> TutorialContent(
                onFinishClick = {
                    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit { putBoolean("onboarding_completed", true) }
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
    }
}