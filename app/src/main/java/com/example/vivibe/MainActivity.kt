package com.example.vivibe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.FractionalThreshold
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberSwipeableState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.swipeable
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.vivibe.components.song.SongFullDetails
import com.example.vivibe.manager.GlobalStateManager
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.router.*
import com.example.vivibe.pages.*
import com.example.vivibe.pages.home.Home
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(applicationContext))[MainViewModel::class.java]
    }

    private val localPlayerProgress = compositionLocalOf { 0f }


    enum class PlayerState {
        MINI, EXPANDED, TOP_BAR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        makeStatusBarTransparent()


        setContent {
            AppScreen()
        }
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun AppScreen() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val saveableStateHolder = rememberSaveableStateHolder()
        val currentSong = remember {
            viewModel.playingSong.transformLatest { value ->
                emit(value)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
        }.collectAsState()


        GlobalStateManager.loadUserFromFile(context)

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            val progress = localPlayerProgress.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (1f - progress) * (1f - progress)
                    }
            ) {
                Scaffold(
                    modifier = Modifier.padding(
                        bottom = if(currentSong.value != null) 64.dp else 0.dp
                    ),
                    containerColor = Color(0xFF101010)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = HomeRouter.route
                        ) {
                            composable(HomeRouter.route) {
                                saveableStateHolder.SaveableStateProvider(HomeRouter.route) {
                                    Home(
                                        LocalContext.current,
                                        viewModel.songClient.value!!
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
                    }
                }
            }
            saveableStateHolder.SaveableStateProvider("Player") {
                    Player(
                        currentSong = currentSong.value,
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }

        BottomSheetArea(
            onHideBottomSheet = { viewModel.hideBottomSheet() }
        )
    }

    @SuppressLint("UnrememberedMutableState")
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class,
    )
    @Composable
    fun Player(
        currentSong: PlaySong?,
        navController: NavHostController,
        viewModel: MainViewModel,
        modifier: Modifier
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val scope = rememberCoroutineScope()

        val screenHeight = with(density) {
            configuration.screenHeightDp.dp.toPx()
        }

        val motionSceneContent = remember {
            context.resources.openRawResource(R.raw.mini_to_expanded_motion_sence)
                .readBytes()
                .decodeToString()
        }

        val swipeableState = rememberSwipeableState(PlayerState.MINI)

        val anchors = mapOf(
            0f to PlayerState.MINI,
            -screenHeight to PlayerState.EXPANDED,
        )

        val progress = when {
            swipeableState.offset.value >= 0 -> 0f // MINI
            else -> abs(swipeableState.offset.value / screenHeight)
        }

        val exoPlayer = remember {
            ExoPlayer.Builder(context).build()
        }

        LaunchedEffect(currentSong) {
            if(swipeableState.currentValue == PlayerState.MINI) {
                swipeableState.animateTo(PlayerState.EXPANDED)
                val mediaItem = MediaItem.fromUri(currentSong!!.audio)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                        exoPlayer.pause()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        exoPlayer.release()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        exoPlayer.play()
                    }

                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }

        val isPlaying = rememberSaveable { mutableStateOf(true) }
        val currentPosition = rememberSaveable { mutableLongStateOf(0L) }
        val totalDuration = rememberSaveable { mutableLongStateOf(0L) }

        LaunchedEffect(exoPlayer) {
            while (true) {
                currentPosition.longValue = exoPlayer.currentPosition
                totalDuration.longValue = exoPlayer.duration
                delay(500L)
            }
        }

        fun playPause() {
            if (isPlaying.value) {
                exoPlayer.pause()
                isPlaying.value = false
            } else {
                exoPlayer.play()
                isPlaying.value = true
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(progressColor(progress))
        ) {
            MotionLayout(
                motionScene = MotionScene(content = motionSceneContent),
                progress = progress,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                BottomNavigation(
                    modifier = Modifier
                        .layoutId("bottomNavigation")
                        .alpha((1f - progress) * (1f - progress))
                        .zIndex(if (currentSong == null) 2f else 0f),
                    navController = navController
                )

                currentSong?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("navigationBar")
                            .padding(8.dp)
                            .alpha(progress * progress),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    if (swipeableState.currentValue == PlayerState.EXPANDED) {
                                        scope.launch { swipeableState.animateTo(PlayerState.MINI) }
                                    }
                                }
                                .background(Color.Transparent),
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
                                            id = currentSong.id,
                                            title = currentSong.title,
                                            thumbnailUrl = currentSong.thumbnailUrl,
                                            views = currentSong.views,
                                            artist = currentSong.artist
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
                        model = currentSong.thumbnailUrl,
                        contentDescription = currentSong.title,
                        modifier = Modifier
                            .fillMaxHeight(0.3f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                scope.launch {
                                    swipeableState.animateTo(PlayerState.EXPANDED)
                                }
                            }
                            .zIndex(1f)
                            .swipeable(
                                state = swipeableState,
                                anchors = anchors,
                                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                                orientation = Orientation.Vertical,
                                enabled = true,
                            )
                            .layoutId("thumbnail"),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .swipeable(
                                state = swipeableState,
                                anchors = anchors,
                                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                                orientation = Orientation.Vertical,
                                enabled = true,
                            )
                            .clickable {
                                scope.launch {
                                    if (swipeableState.currentValue == PlayerState.MINI) {
                                        swipeableState.animateTo(PlayerState.EXPANDED)
                                    }
                                }
                            }
                            .alpha((1f - progress) * (1f - progress))
                            .layoutId("short")
                            .zIndex(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Text(
                                text = currentSong.title.replaceFirstChar { it.uppercaseChar() },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = currentSong.artist.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }

                        Row(
                            modifier = Modifier.width(96.dp).fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Box(modifier = Modifier.size(24.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_previous),
                                    contentDescription = "Previous",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(if (isPlaying.value) R.drawable.ic_pause_outline else R.drawable.ic_play_outline),
                                    contentDescription = "Play",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { playPause() },
                                    tint = Color.White
                                )
                            }

                            Box(modifier = Modifier.size(24.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_next),
                                    contentDescription = "Next",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(1.25f * (progress - 0.8f) * (progress - 1f))
                            .zIndex(1f)
                            .layoutId("track")
                    ) {
                        ProgressBarShort(
                            progress = currentPosition.longValue.toFloat() / totalDuration.longValue.toFloat()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .alpha(progress * progress)
                            .layoutId("details")
                            .zIndex(1f)
                    ) {
                        SongFullDetails(
                            song = currentSong,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            ProgressBarDetail(
                                modifier = Modifier,
                                progress = currentPosition.longValue.toFloat() / totalDuration.longValue.toFloat(),
                                onProgressChanged = { newProgress ->
                                    val seekPosition =
                                        (newProgress * totalDuration.longValue).toLong()
                                    currentPosition.longValue = seekPosition
                                    exoPlayer.seekTo(seekPosition)
                                },
                                onSeekTo = { newProgress ->
                                    val seekPosition =
                                        (newProgress * totalDuration.longValue).toLong()
                                    exoPlayer.seekTo(seekPosition)
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(currentPosition.longValue),
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatTime(totalDuration.longValue),
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {

                            Box(modifier = Modifier.size(42.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_previous),
                                    contentDescription = "Previous",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.White
                                )
                            }

                            Box(modifier = Modifier.size(84.dp)) {
                                Icon(
                                    painter = painterResource(if (isPlaying.value) R.drawable.ic_pause_filled else R.drawable.ic_play_filled),
                                    contentDescription = "Play",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { playPause() },
                                    tint = Color.White
                                )
                            }

                            Box(modifier = Modifier.size(42.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_next),
                                    contentDescription = "Next",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun progressColor(progress: Float): Color {
        return lerp(
            start = Color.Transparent,
            stop = Color(0xFF101010),
            fraction = progress
        )
    }

    @Composable
    fun ProgressBarShort(
        progress: Float,
    ) {
        val currentProgress = remember { mutableFloatStateOf(progress) }
        val trackHeight = 2.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
                val width = size.width
                val centerY = size.height / 2

                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(0f, centerY - trackHeight.toPx() / 2),
                    size = Size(width, trackHeight.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(0f, centerY - trackHeight.toPx() / 2),
                    size = Size(width * progress, trackHeight.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        LaunchedEffect(progress) {
            currentProgress.floatValue = progress
        }
    }


    @Composable
    fun ProgressBarDetail(
        modifier: Modifier,
        progress: Float,
        onProgressChanged: (Float) -> Unit,
        onSeekTo: (Float) -> Unit
    ) {
        val currentProgress = remember { mutableFloatStateOf(progress) }
        val trackHeight = 4.dp
        val thumbRadius = 8.dp

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val newProgress = (tapOffset.x / width).coerceIn(0f, 1f)
                        currentProgress.floatValue = newProgress
                        onProgressChanged(newProgress)
                        onSeekTo(newProgress)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val width = size.width.toFloat()
                        val newProgress = ((currentProgress.floatValue * width + dragAmount) / width).coerceIn(0f, 1f)
                        currentProgress.floatValue = newProgress
                        onProgressChanged(newProgress)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
                val width = size.width
                val centerY = size.height / 2

                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(0f, centerY - trackHeight.toPx() / 2),
                    size = Size(width, trackHeight.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(0f, centerY - trackHeight.toPx() / 2),
                    size = Size(width * progress, trackHeight.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                drawCircle(
                    color = Color.White,
                    radius = thumbRadius.toPx(),
                    center = Offset(width * progress, centerY)
                )
            }
        }

        LaunchedEffect(progress) {
            currentProgress.floatValue = progress
        }
    }


    @SuppressLint("DefaultLocale")
    fun formatTime(milliseconds: Long): String {
        if(milliseconds <= 0L)
            return "00:00"
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
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