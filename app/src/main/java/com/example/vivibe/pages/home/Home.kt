package com.example.vivibe.pages.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vivibe.MainActivity
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.R
import com.example.vivibe.model.User
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.home.HomeComponent
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.router.NotificationsRouter
import com.example.vivibe.router.SearchRouter
import com.example.vivibe.router.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Home(private val appContext: Context, songClient: SongClient) {
    private val user = GlobalStateManager.userState
    private val homeViewModel = HomeViewModel(appContext, songClient)

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun HomeScreen(navController: NavController, onSongMoreClick: (QuickPicksSong) -> Unit, onPlayMusicNavigate: (Int) -> Unit) {
        val showTokenExpiredDialog = homeViewModel.showTokenExpiredDialog.collectAsState()
        val homeComponentViewModel = homeViewModel.homeComponentViewModel.collectAsState()
        val isRefreshing = homeViewModel.isRefreshing.collectAsState()
        val scope = rememberCoroutineScope()

        val homeComponent = HomeComponent(homeComponentViewModel.value)

        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing.value)


        if (showTokenExpiredDialog.value) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Session Expired") },
                text = { Text(text = "Your session has expired. Please sign in again.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                homeViewModel.dismissTokenExpiredDialog()
                                val success = homeViewModel.signOut()
                                if(success) {
                                    homeViewModel.reload()
                                }
                            }
                        }
                    ) {
                        Text(text = "OK")
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(bottom = 64.dp).background(Color.Transparent)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopAppBar { HomeActions(appContext, scope, navController, user.value, homeViewModel) }

                homeComponent.GenresScreen()

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = homeViewModel::loadHomeComponent
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            homeComponent.QuickPicksScreen(onSongMoreClick, onPlayMusicNavigate)
                        }

                        item {
                            homeComponent.SpeedDialScreen(user.value, onPlayMusicNavigate)
                        }
                    }

                }
            }
        }
    }

    @Composable
    private fun HomeActions(
        context: Context,
        scope: CoroutineScope,
        navController: NavController,
        user: User?,
        homeViewModel: HomeViewModel
    ) {
        Row(
            modifier = Modifier.width(150.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.navigate(NotificationsRouter.route)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_notifications),
                    contentDescription = "Notifications",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            IconButton(
                onClick = {
                    navController.navigate(SearchRouter.route)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            if (user == User("", "", "", "", "", 0)) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val success = homeViewModel.signIn()
                            if(success) {
                                (context as? MainActivity)?.reloadActivity()
                            }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = "Google Sign In",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        scope.launch {
                            homeViewModel.signOut()
                            (context as? MainActivity)?.reloadActivity()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    user?.profilePictureUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}
