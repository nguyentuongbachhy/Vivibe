package com.example.vivibe.router

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vivibe.R

@Composable
fun TopAppBar(actions: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFF101010)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White),
            )
            Text(
                text = "Vivibify",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        actions()
    }
}

@Composable
fun BottomNavigation(navController: NavController, isPremium: Int, modifier: Modifier) {
    val baseItems = listOf(
        HomeRouter,
        SamplesRouter,
        ExploreRouter,
        LibraryRouter
    )

    val items = if(isPremium == 0) {
        baseItems + UpgradeRouter
    } else {
        baseItems
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = currentDestination?.route

    NavigationBar(
        modifier = modifier.defaultMinSize(minHeight = 64.dp),
        containerColor = Color(0xFF101010),
    ) {
        items.forEach{item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                label = { Text(text = item.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White) },
                icon = { Icon(
                    painter = painterResource(
                        if (isSelected) item.iconFilled else item.iconOutline
                    ),
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                    ) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(HomeRouter.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color(0xFF101010)
                ),
                modifier = Modifier.defaultMinSize(minHeight = 64.dp)
            )
        }
    }
}