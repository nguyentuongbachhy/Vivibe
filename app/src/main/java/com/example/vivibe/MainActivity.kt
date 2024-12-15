package com.example.vivibe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.vivibe.router.*
import com.example.vivibe.pages.*
import com.example.vivibe.pages.home.Home
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        makeStatusBarTransparent()

        setContent {
            AppScreen()
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun AppScreen() {
        val navController = rememberNavController()
        val context = LocalContext.current

        val bottomSheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        val bottomSheetController = remember { BottomSheetController() }

        Scaffold(
            bottomBar = { BottomNavigation(navController) },
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .background(Color(0xFF101010))
            )  {
                NavHost(
                    navController = navController,
                    startDestination = HomeRouter.route
                ) {
                    composable(HomeRouter.route) {
                        Home(context).HomeScreen(
                            navController,
                            onSongMoreClick = { song ->
                                scope.launch {
                                    bottomSheetController.showBottomSheet(song)
                                }
                            })
                    }
                    composable(SamplesRouter.route) {
                        Samples().SamplesScreen()
                    }

                    composable(ExploreRouter.route) {
                        Explore().SampleScreen()
                    }

                    composable(LibraryRouter.route) {
                        Library().LibraryScreen()
                    }

                    composable(NotificationsRouter.route) {
                        Notifications().NotificationsScreen()
                    }

                    composable(SearchRouter.route) {
                        Search().SearchScreen()
                    }
                }
            }
        }

        BottomSheetArea(
            bottomSheetController = bottomSheetController,
            bottomSheetState = bottomSheetState
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomSheetArea(
        bottomSheetController: BottomSheetController,
        bottomSheetState: SheetState
    ) {
        val showBottomSheet by bottomSheetController.showBottomSheet.collectAsState()
        val currentSong by bottomSheetController.currentSong.collectAsState()

        if (showBottomSheet && currentSong != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    bottomSheetController.hideBottomSheet()
                },
                sheetState = bottomSheetState,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxHeight()
            ) {
                BottomSheetContent(currentSong!!)
            }
        }
    }


    @Composable
    private fun BottomSheetContent(song: QuickPicksSong) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 8.dp),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF202020))
                    .padding(0.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                        ) {
                            Text(
                                text = song.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = song.artist.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.LightGray
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_like_outline),
                                contentDescription = "Dislike",
                                tint = Color.White,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(180f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_like_outline),
                                contentDescription = "Like",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        BottomSheetCard(
                            icon = R.drawable.ic_play_next,
                            title = "Play next"
                        )
                    }

                    item {
                        BottomSheetCard(
                            icon = R.drawable.ic_save_to_playlist,
                            title = "Save to playlist"
                        )
                    }

                    item {
                        BottomSheetCard(
                            icon = R.drawable.ic_share,
                            title = "Share"
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 0.dp, max = 400.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    item {
                        BottomSheetItem(R.drawable.ic_add_to_queue, "Add to queue")
                    }
                    item {
                        BottomSheetItem(R.drawable.ic_add_to_library, "Save to library")
                    }
                    item {
                        BottomSheetItem(R.drawable.ic_download, "Download")
                    }
                    item {
                        BottomSheetItem(R.drawable.ic_go_to_album, "Go to album")
                    }
                    item {
                        BottomSheetItem(R.drawable.ic_go_to_artist, "Go to artist")
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomSheetItem(icon: Int, title: String) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable {  },
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    @Composable
    private fun BottomSheetCard(icon: Int, title: String) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = CardDefaults.cardColors(Color(0xFF303030)),
                shape = RoundedCornerShape(8.dp),

            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    private fun makeStatusBarTransparent() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color(0xFF101010).toArgb()
        window.navigationBarColor = Color(0xFF101010).toArgb()

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
    }
}
