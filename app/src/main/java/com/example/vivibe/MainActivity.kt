package com.example.vivibe

import com.google.accompanist.navigation.animation.composable
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.rememberSwipeableState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
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
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.vivibe.components.song.SongFullDetails
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.router.*
import com.example.vivibe.pages.*
import com.example.vivibe.pages.home.Home
import com.google.accompanist.navigation.animation.AnimatedNavHost
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
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
        MINI, EXPANDED, TOP_BAR
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalCoroutinesApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun AppScreen() {
        val scope = rememberCoroutineScope()
        val playingSong = remember {
            viewModel.playingSong.transformLatest { value ->
                emit(value)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
        }.collectAsState()

        val navController = rememberNavController()
        val saveableStateHolder = rememberSaveableStateHolder()

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

                    composable(SamplesRouter.route) {
                        Samples().SamplesScreen()
                    }

                    composable(ExploreRouter.route) {
                        Explore().ExploreScreen()
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

                saveableStateHolder.SaveableStateProvider("Play Music Screen") {
                    val currentSwipe = remember { mutableIntStateOf(0) }

                    playingSong.value?.let { song ->
                        MotionPlayerLayout(
                            song = song,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(if(currentSwipe.intValue == 0) Alignment.BottomEnd else Alignment.TopStart)
                                .zIndex(1f),
                            onStateChanged = { currentSwipe.intValue = it}
                        )
                    }
                }
            }
        }

        BottomSheetArea(
            onHideBottomSheet = { viewModel.hideBottomSheet() }
        )
    }

//    private fun Modifier.gestures(
//        onVerticalDrag: (Float) -> Unit
//    ) = this.pointerInput(Unit) {
//        detectVerticalDragGestures { _, dragAmount ->
//            onVerticalDrag(dragAmount)
//        }
//    }

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

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    @Composable
    fun MotionPlayerLayout(
        song: PlaySong,
        modifier: Modifier = Modifier,
        onStateChanged: (Int) -> Unit = {}
    ) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val configuration = LocalConfiguration.current

        val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

        val miniToExpandedMotionSceneContent = remember {
            context.resources.openRawResource(R.raw.mini_to_expanded_motion_sence)
                .readBytes()
                .decodeToString()
        }

        val expandedToTopBarMotionSceneContent = remember {
            context.resources.openRawResource(R.raw.expanded_to_topbar_motion_sence)
                .readBytes()
                .decodeToString()
        }

        val swipeableState = rememberSwipeableState(PlayerState.MINI)


        val anchors = mapOf(
            0f to PlayerState.MINI,
            -screenHeight / 2 to PlayerState.EXPANDED,
            -screenHeight to PlayerState.TOP_BAR
        )

        val swipeAreaHeight = screenHeight / 2

        val motionProgress = when {
            swipeableState.offset.value >= 0 -> 0f // MINI
            swipeableState.offset.value in -swipeAreaHeight..0f ->
                abs(swipeableState.offset.value/ -swipeAreaHeight) // MINI -> EXPANDED
            swipeableState.offset.value in -(screenHeight - 100f)..-swipeAreaHeight ->
                1f - abs((swipeableState.offset.value + swipeAreaHeight) / (screenHeight - swipeAreaHeight)) // EXPANDED -> TOP_BAR
            else -> 1f
        }
        var currentScene = remember(swipeableState.currentValue) {
            when {
                swipeableState.offset.value >= 0 -> miniToExpandedMotionSceneContent
                swipeableState.offset.value > -screenHeight / 1.5f -> miniToExpandedMotionSceneContent
                else -> expandedToTopBarMotionSceneContent
            }
        }


        val exoPlayer = remember {
            ExoPlayer.Builder(context).build()
        }


        LaunchedEffect(song) {
            if (swipeableState.currentValue == PlayerState.MINI) {
                swipeableState.animateTo(PlayerState.EXPANDED)
            }

            val mediaItem = MediaItem.fromUri(song.audio)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
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

        LaunchedEffect(currentScene) {
            if(currentScene == miniToExpandedMotionSceneContent) {
                onStateChanged(0)
            } else {
                onStateChanged(1)
            }
        }

        LaunchedEffect(swipeableState.offset.value) {
            if (swipeableState.offset.value > -screenHeight / 2 && swipeableState.currentValue != PlayerState.MINI) {
                swipeableState.animateTo(PlayerState.MINI)
            }
        }

        BackHandler {
            scope.launch {
                when (swipeableState.currentValue) {
                    PlayerState.EXPANDED -> swipeableState.animateTo(PlayerState.MINI)
                    PlayerState.TOP_BAR -> swipeableState.animateTo(PlayerState.EXPANDED)
                    else -> {}
                }
            }
        }

        Box(
            modifier = modifier
                .background(Color(0xFF101010))
                .fillMaxWidth()
                .height(
                    when(swipeableState.currentValue) {
                        PlayerState.TOP_BAR -> Dp.Unspecified
                        else->
                            with(LocalDensity.current) {
                                max(68.dp, (screenHeight * motionProgress).toDp())
                            }

                    }
                )
                .clickable(
                    enabled = true,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    scope.launch {
                        swipeableState.animateTo(PlayerState.EXPANDED)
                    }
                }
        ) {
            MotionLayout(
                motionScene = MotionScene(content = currentScene),
                progress = motionProgress,
                modifier = Modifier.fillMaxSize()
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth().layoutId("navigationBar").padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    if (delta > 0 && swipeableState.currentValue == PlayerState.TOP_BAR) {
                                        scope.launch { swipeableState.animateTo(PlayerState.MINI) }
                                    }
                                }
                            )
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
                        .background(Color.Transparent)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch { swipeableState.animateTo(PlayerState.EXPANDED) }
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .layoutId("short")
                        .alpha(
                            when(swipeableState.currentValue) {
                                PlayerState.TOP_BAR -> 1f
                                else -> max(0f, 1f - 2 * motionProgress)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {

                        Text(
                            text = song.title.replaceFirstChar { it.uppercaseChar() },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = song.artist.name,
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
                                painter = painterResource(if(isPlaying.value) R.drawable.ic_pause_outline else R.drawable.ic_play_outline),
                                contentDescription = "Play",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        if(isPlaying.value) {
                                            exoPlayer.pause()
                                            isPlaying.value = false
                                        } else {
                                            exoPlayer.play()
                                            isPlaying.value = true
                                        }
                                    },
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

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .alpha(
                        when(swipeableState.currentValue) {
                            PlayerState.TOP_BAR -> 1f
                            else -> max(0f, 1f - 2 * motionProgress)
                        }
                    )
                    .layoutId("track")) {
                    ProgressBarShort(
                        progress = currentPosition.longValue.toFloat() / totalDuration.longValue.toFloat()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .alpha(
                            when(swipeableState.currentValue) {
                                PlayerState.TOP_BAR -> 0f
                                else -> max(1f, 2 * motionProgress)
                            }
                        )
                        .zIndex(1f)
                        .layoutId("details")
                ){
                    SongFullDetails(
                        song = song,
                        modifier = Modifier
                            .alpha(min(motionProgress, 1f))
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .zIndex(1f)
                            .layoutId("details")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        ProgressBarDetail(
                            modifier = Modifier,
                            progress = currentPosition.longValue.toFloat() / totalDuration.longValue.toFloat(),
                            onProgressChanged = { newProgress ->
                                val seekPosition = (newProgress * totalDuration.longValue).toLong()
                                currentPosition.longValue = seekPosition
                                exoPlayer.seekTo(seekPosition)
                            },
                            onSeekTo = { newProgress ->
                                val seekPosition = (newProgress * totalDuration.longValue).toLong()
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
                            Text(text = formatTime(totalDuration.longValue),
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
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
                                painter = painterResource(if(isPlaying.value) R.drawable.ic_pause_filled else R.drawable.ic_play_filled),
                                contentDescription = "Play",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        if(isPlaying.value) {
                                            exoPlayer.pause()
                                            isPlaying.value = false
                                        } else {
                                            exoPlayer.play()
                                            isPlaying.value = true
                                        }
                                    },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .background(Color.Gray)
                        .layoutId("optionBar")
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    Log.d("Gesture", "onDragStart called")
                                    if (swipeableState.currentValue == PlayerState.EXPANDED) {
                                        currentScene = expandedToTopBarMotionSceneContent
                                    }
                                },
                                onDragEnd = {
                                    if (swipeableState.currentValue == PlayerState.EXPANDED) {
                                        currentScene = miniToExpandedMotionSceneContent
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    Log.d("Gesture", "DragAmount: $dragAmount")
                                    scope.launch {
                                        if (dragAmount < 0) {
                                            if (swipeableState.currentValue == PlayerState.EXPANDED) {
                                                currentScene = expandedToTopBarMotionSceneContent
                                            }
                                            swipeableState.animateTo(
                                                when (swipeableState.currentValue) {
                                                    PlayerState.MINI -> PlayerState.EXPANDED
                                                    PlayerState.EXPANDED -> PlayerState.TOP_BAR
                                                    else -> PlayerState.TOP_BAR
                                                }
                                            )
                                        } else {
                                            if (swipeableState.currentValue == PlayerState.EXPANDED) {
                                                currentScene = miniToExpandedMotionSceneContent
                                            }
                                            swipeableState.animateTo(
                                                when (swipeableState.currentValue) {
                                                    PlayerState.TOP_BAR -> PlayerState.EXPANDED
                                                    PlayerState.EXPANDED -> PlayerState.MINI
                                                    else -> PlayerState.MINI
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                )
            }
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