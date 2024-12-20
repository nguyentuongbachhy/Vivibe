package com.example.vivibe.pages.home

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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.R
import com.example.vivibe.model.User
import com.example.vivibe.api.song.SongClient
import com.example.vivibe.components.home.HomeComponent
import com.example.vivibe.router.NotificationsRouter
import com.example.vivibe.router.SearchRouter
import com.example.vivibe.router.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class Home(appContext: Context, private val token: String, private val googleId: String, songClient: SongClient) {
    private val homeViewModel = HomeViewModel(appContext, token, googleId, songClient)

    @Composable
    fun HomeScreen(navController: NavController, onSongMoreClick: (QuickPicksSong) -> Unit, onPlayMusicNavigate: (Int) -> Unit) {
        val isSignedIn = homeViewModel.isSignedIn.collectAsState()
        val user = homeViewModel.user.collectAsState()
        val showTokenExpiredDialog = homeViewModel.showTokenExpiredDialog.collectAsState()
        val homeComponentViewModel = homeViewModel.homeComponentViewModel.collectAsState()
        val isRefreshing = homeViewModel.isRefreshing.collectAsState()

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
                            homeViewModel.dismissTokenExpiredDialog()
                            homeViewModel.signOut()
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
                TopAppBar { HomeActions(navController, isSignedIn, user, homeViewModel) }

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
        navController: NavController,
        isSignedIn: State<Boolean>,
        user: State<User?>,
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

            if (!isSignedIn.value) {
                IconButton(
                    onClick = { homeViewModel.signIn() },
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
                    onClick = {},
                    modifier = Modifier.size(24.dp)
                ) {
                    user.value?.profilePictureUri?.let { uri ->
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
