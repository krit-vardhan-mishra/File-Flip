package com.just_for_fun.fileflip.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.screens.BottomBarItem
import com.just_for_fun.fileflip.ui.util.SurfaceDark

@Composable
fun FloatingBottomBar(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .padding(horizontal = 40.dp) // Indent to make it float
            .height(64.dp)
            .fillMaxWidth()
            .shadow(16.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.5f))
            .background(SurfaceDark.copy(alpha = 0.95f), CircleShape) // Slightly opaque
            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Rounded.GridView,
                label = "Home",
                isSelected = true,
                onClick = { /* Already on home */ }
            )
            BottomBarItem(
                icon = Icons.Rounded.FolderOpen,
                label = "Library",
                isSelected = false,
                onClick = { navController.navigate("file_explorer") }
            )
            BottomBarItem(
                icon = Icons.AutoMirrored.Rounded.Article,
                label = "Editor",
                isSelected = false,
                onClick = { navController.navigate("editor/empty") }
            )
            // BottomBarItem(
            //     icon = Icons.Rounded.AutoAwesome,
            //     label = "Pro",
            //     isSelected = false,
            //     onClick = { navController.navigate("pro") }
            // )
            BottomBarItem(
                icon = Icons.Rounded.Settings,
                label = "Settings",
                isSelected = false,
                onClick = { navController.navigate("settings") }
            )
        }
    }
}
