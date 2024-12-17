package com.example.vivibe

import com.google.accompanist.navigation.animation.composable
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberSwipeableState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.vivibe.components.song.SongFullDetails
import com.example.vivibe.router.*
import com.example.vivibe.pages.*
import com.example.vivibe.pages.home.Home
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(applicationContext))[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        makeStatusBarTransparent()


        setContent {
            AppScreen()
        }
    }

    enum class PlayerState {
        MINI, EXPANDED, TOP
    }

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun AppScreen() {
        val playingSong = viewModel.playingSong.collectAsState()
        val navController = rememberNavController()
        val saveableStateHolder = rememberSaveableStateHolder()
        val scope = rememberCoroutineScope()

        Scaffold(
            bottomBar = {
                BottomNavigation(navController)
            },
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFF101010))
            ) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = HomeRouter.route
                ) {
                    composable(HomeRouter.route) {
                        saveableStateHolder.SaveableStateProvider(HomeRouter.route) {
                            Home(
                                LocalContext.current,
                                viewModel.token!!,
                                viewModel.googleId!!,
                                viewModel.songClient
                            ).HomeScreen(
                                navController,
                                onSongMoreClick = { song ->
                                    viewModel.showBottomSheet(song)
                                },
                                onPlayMusicNavigate = { songId ->
                                    scope.launch {
                                        viewModel.fetchPlaySong(songId)
                                    }
                                }
                            )
                        }
                    }

                    composable(SamplesRouter.route) {
                        saveableStateHolder.SaveableStateProvider(SamplesRouter.route) {
                            Samples().SamplesScreen()
                        }
                    }

                    composable(ExploreRouter.route) {
                        saveableStateHolder.SaveableStateProvider(ExploreRouter.route) {
                            Explore().ExploreScreen()
                        }
                    }

                    composable(LibraryRouter.route) {
                        saveableStateHolder.SaveableStateProvider(LibraryRouter.route) {
                            Library().LibraryScreen()
                        }
                    }

                    composable(NotificationsRouter.route) {
                        saveableStateHolder.SaveableStateProvider(NotificationsRouter.route) {
                            Notifications().NotificationsScreen()
                        }
                    }

                    composable(SearchRouter.route) {
                        saveableStateHolder.SaveableStateProvider(SearchRouter.route) {
                            Search().SearchScreen()
                        }
                    }
                }

                if(playingSong.value != null) {
                    MotionPlayerLayout(
                        song = playingSong.value!!,
                        onSwipeToFullScreen = {
                            // Optional action when swiping
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(1f)
                    )
                }
            }
        }

        BottomSheetArea(
            onHideBottomSheet = { viewModel.hideBottomSheet() }
        )
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    @Composable
    fun MotionPlayerLayout(
        song: PlaySong,
        onSwipeToFullScreen: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val configuration = LocalConfiguration.current

        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
        val swipeAreaHeight = screenHeight - 200

        val motionSceneContent = remember {
            context.resources.openRawResource(R.raw.motion_scene)
                .readBytes()
                .decodeToString()
        }

        val swipeableState = rememberSwipeableState(0)
        val anchors = mapOf(0f to 0, -swipeAreaHeight to 1)
        val swipeProgress = swipeableState.offset.value / -swipeAreaHeight
        val motionProgress = max(min(swipeProgress, 1f), 0f)

        val enableTouch = remember { mutableStateOf(false) }

        LaunchedEffect(song) {
            if (swipeableState.currentValue == 0) {
                swipeableState.animateTo(1)
            }
        }
        Box(
            modifier = if(motionProgress > 0.1f) {
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(enabled = enableTouch.value, indication = null, interactionSource = remember { MutableInteractionSource() }) {  }
                    .background(Color.Transparent)
            } else {
                modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clickable(enabled = enableTouch.value, indication = null, interactionSource = remember { MutableInteractionSource() }) {  }
                    .background(Color.Transparent)
            }
        ) {
            MotionLayout(
                motionScene = MotionScene(content = motionSceneContent),
                progress = motionProgress,
                modifier = if(motionProgress > 0.5f) {
                    modifier
                        .fillMaxSize()
                        .background(Color(0xFF101010))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            scope.launch { swipeableState.animateTo(0) }
                        }
                } else{
                    modifier
                        .fillMaxSize()
                        .background(Color(0xFF101010))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            scope.launch { swipeableState.animateTo(1) }
                        }
                        .align(Alignment.BottomEnd)
                },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().layoutId("navigationBar").padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                scope.launch {
                                    swipeableState.animateTo(0)
                                }
                            }
                            .background(Color(0xFF101010)),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_out_full_screen),
                            contentDescription = "Close",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                viewModel.showBottomSheet(
                                    QuickPicksSong(
                                        id = song.id,
                                        title = song.title,
                                        thumbnailUrl = song.thumbnailUrl,
                                        views = song.views,
                                        artist = song.artist
                                    )
                                )
                            }
                            .background(Color(0xFF101010)),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = "More",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }
                }


                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxHeight(0.3f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF101010))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch { swipeableState.animateTo(1) }
                        }
                        .alpha(1f)
                        .zIndex(1f)
                        .swipeable(
                            state = swipeableState,
                            anchors = anchors,
                            thresholds = { _, _ -> FractionalThreshold(0.2f) },
                            orientation = Orientation.Vertical
                        )
                        .layoutId("thumbnail"),
                    contentScale = ContentScale.Crop
                )

                // Song Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .alpha(1f - min(motionProgress * 2, 1f))
                        .zIndex(1f)
                        .layoutId("details"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = song.title.replaceFirstChar { it.uppercaseChar() },
                            color = Color.White,
                            maxLines = 1,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = song.artist.name,
                            color = Color.LightGray,
                            maxLines = 1,
                            fontSize = 14.sp
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_play_outline),
                        contentDescription = "Play Or Pause",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101010))
                        .layoutId("content")
                ) {
                    SongFullDetails(
                        song = song,
                        modifier = Modifier
                            .alpha(min(motionProgress, 1f))
                            .fillMaxWidth()
                    )
                }
            }
        }
        if (motionProgress > 0.5f) {
            enableTouch.value = true
        } else {
            enableTouch.value = false
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    @Composable
    fun EnhancedMotionPlayerLayout(
        song: PlaySong,
        modifier: Modifier
    ) {
        var currentState by remember { mutableStateOf(PlayerState.MINI) }
        var motionProgress by remember { mutableFloatStateOf(0f) }

        val swipeableState = rememberSwipeableState(
            initialValue = PlayerState.MINI,
            animationSpec = tween(300)
        )

        val context = LocalContext.current
        val motionSceneContent = remember {
            context.resources.openRawResource(R.raw.motion_scene)
                .readBytes()
                .decodeToString()
        }

        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val screenHeight = with(density) {configuration.screenHeightDp.dp.toPx()}

        val anchors = remember {
            mapOf(
                0f to PlayerState.MINI,
                -screenHeight * 0.5f to PlayerState.EXPANDED,
                -screenHeight to PlayerState.TOP
            )
        }

        LaunchedEffect(song) {
            if (swipeableState.currentValue == PlayerState.MINI) {
                swipeableState.animateTo(PlayerState.EXPANDED)
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = {_, _ -> FractionalThreshold(0.2f)},
                    orientation = Orientation.Vertical
                )
        ) {
//            LaunchedEffect(swipeableState.offset.value) {
//                motionProgress = abs(swipeableState.offset.value / screenHeight)
//
//                currentState = when {
//                    swipeableState.offset.value == 0f -> PlayerState.MINI
//                    swipeableState.offset.value < -screenHeight * 0.5f -> PlayerState.TOP
//                    else -> PlayerState.EXPANDED
//                }
//            }

            MotionLayout(
                motionScene = MotionScene(content = motionSceneContent),
                progress = motionProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF101010))
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier.layoutId("thumbnail"),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.layoutId("details")
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = song.artist.name,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.layoutId("navigationBar")
                ) {
                    IconButton(onClick = { /* Xử lý previous */ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_skip_previous),
                            contentDescription = "Previous"
                        )
                    }
                    IconButton(onClick = { /* Xử lý play/pause */ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_outline),
                            contentDescription = "Play/Pause"
                        )
                    }
                    IconButton(onClick = { /* Xử lý next */ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_skip_next),
                            contentDescription = "Next"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .layoutId("content")
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Chi tiết bài hát",
                        color = Color.White
                    )
                }
            }
        }

        LaunchedEffect(swipeableState.offset.value) {
            motionProgress = calculateProgress(
                swipeableState.offset.value,
                screenHeight
            )

            currentState = determineState(swipeableState.offset.value, screenHeight)
        }
    }


    private fun calculateProgress(offset: Float, screenHeight: Float): Float {
        return abs(offset / screenHeight)
    }

    private fun determineState(offset: Float, screenHeight: Float): PlayerState {
        return when {
            offset == 0f -> PlayerState.MINI
            offset < -screenHeight * 0.5f -> PlayerState.TOP
            else -> PlayerState.EXPANDED
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheetArea(
        onHideBottomSheet: () -> Unit
    ) {
        val showBottomSheet by viewModel.showBottomSheet.collectAsState()
        val currentBottomSheetSong by viewModel.currentBottomSheetSong.collectAsState()

        if (showBottomSheet && currentBottomSheetSong != null) {
            ModalBottomSheet(
                onDismissRequest = { onHideBottomSheet() },
                containerColor =  Color(0xFF101010),
                dragHandle = {},

                ) {
                BottomSheetContent(currentBottomSheetSong!!)
            }
        }
    }


    @Composable
    private fun BottomSheetContent(song: QuickPicksSong) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent)
                .padding(horizontal = 8.dp),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 400.dp),
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

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    @Composable
    private fun BottomSheetItem(icon: Int, title: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
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