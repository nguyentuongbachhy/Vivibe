package com.example.vivibe

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.vivibe.components.home.HomeComponent
import com.example.vivibe.components.song.SongFullDetails
import com.example.vivibe.manager.SharedExoPlayer
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.PlaySong
import com.example.vivibe.model.QuickPicksSong
import com.example.vivibe.router.*
import com.example.vivibe.pages.*
import com.example.vivibe.pages.artist.Artist
import com.example.vivibe.pages.artist.ArtistViewModel
import com.example.vivibe.pages.home.Home
import com.example.vivibe.pages.home.HomeViewModel
import com.example.vivibe.router.ArtistRouter.artistIdArg
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

class MainActivity: ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeComponent: HomeComponent
    private lateinit var artistViewModel: ArtistViewModel
    private lateinit var userManager: UserManager
    private lateinit var exoPlayer: SharedExoPlayer

    enum class PlayerState {
        MINI, EXPANDED, TOP_BAR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            initializeComponents()

            if (!checkComponentsInitialized()) {
                throw IllegalStateException("Components not properly initialized")
            }

            makeStatusBarTransparent()

            setContent {
                AppScreen(
                    viewModel = viewModel,
                    homeViewModel = homeViewModel,
                    homeComponent = homeComponent,
                    artistViewModel = artistViewModel
                )
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_LONG).show()
            finish()
        }

        makeStatusBarTransparent()

        setContent {
            AppScreen(
                viewModel =viewModel,
                homeViewModel = homeViewModel,
                homeComponent = homeComponent,
                artistViewModel = artistViewModel
            )
        }
    }

    private fun initializeComponents() {
        try {
            userManager = UserManager.getInstance(applicationContext)

            exoPlayer = SharedExoPlayer.getInstance(applicationContext)

            viewModel = ViewModelProvider(
                this,
                MainViewModelFactory(applicationContext, userManager, exoPlayer)
            )[MainViewModel::class.java]

            homeViewModel = HomeViewModel(applicationContext, exoPlayer)

            val homeComponentVM = homeViewModel.homeComponentViewModel.value

            homeComponent = HomeComponent(homeComponentVM, exoPlayer)

            artistViewModel = ArtistViewModel(applicationContext, userManager, exoPlayer)

            Log.d("MainActivity", "All components initialized successfully")

        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing components", e)
            throw RuntimeException("Failed to initialize components", e)
        }
    }

    private fun checkComponentsInitialized(): Boolean {
        return ::userManager.isInitialized &&
                ::exoPlayer.isInitialized &&
                ::viewModel.isInitialized &&
                ::homeViewModel.isInitialized &&
                ::homeComponent.isInitialized &&
                ::artistViewModel.isInitialized
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    private fun AppScreen(
        viewModel: MainViewModel,
        homeViewModel: HomeViewModel,
        homeComponent: HomeComponent,
        artistViewModel: ArtistViewModel
    ) {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val saveableStateHolder = rememberSaveableStateHolder()
        val user = userManager.userState.collectAsState()
        val currentSongId = remember {
            viewModel.currentSongId.transformLatest { value ->
                emit(value)
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = -1
            )
        }.collectAsState()

        val listSong = remember {
            viewModel.listSong.transformLatest { value ->
                emit(value)
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                PlayerPadding(currentSongId = currentSongId.value) {
                    Scaffold(
                        containerColor = Color(0xFF101010)
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .zIndex(0f)
                                .background(Color.Transparent)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = HomeRouter.route
                            ) {
                                composable(
                                    route = HomeRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(HomeRouter.route) {
                                        Home(
                                            LocalContext.current,
                                        ).HomeScreen(
                                            homeViewModel,
                                            homeComponent,
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

                                composable(
                                    route = SamplesRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(SamplesRouter.route) {
                                        Samples().SamplesScreen()
                                    }
                                }

                                composable(
                                    route = ExploreRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(ExploreRouter.route) {
                                        Explore().ExploreScreen()
                                    }
                                }

                                composable(
                                    route = LibraryRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(LibraryRouter.route) {
                                        Library().LibraryScreen()
                                    }
                                }

                                composable(
                                    route = NotificationsRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(NotificationsRouter.route) {
                                        Notifications().NotificationsScreen()
                                    }
                                }

                                composable(
                                    route = SearchRouter.route,
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    saveableStateHolder.SaveableStateProvider(SearchRouter.route) {
                                        Search().SearchScreen()
                                    }
                                }

                                composable(
                                    "${ArtistRouter.route}/{${ artistIdArg }}",
                                    arguments = listOf(navArgument(artistIdArg) { type = NavType.IntType }),
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None }
                                ) {
                                    val artistId = it.arguments?.getInt(artistIdArg)
                                    artistId?.let {
                                        saveableStateHolder.SaveableStateProvider(ArtistRouter.route) {
                                            Artist()
                                                .ArtistScreen(
                                                    exoPlayer = exoPlayer,
                                                    viewModel = artistViewModel,
                                                    artistId = artistId,
                                                    navController = navController,
                                                    onSongMoreClick = { song ->
                                                        scope.launch {
                                                            viewModel.showBottomSheet(song)
                                                        }
                                                    },
                                                    onPlayMusicNavigate = { songId ->
                                                        scope.launch {
                                                            viewModel.fetchPlaySong(songId)
                                                        }
                                                    }
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            saveableStateHolder.SaveableStateProvider("Player") {
                Player(
                    isPremium = user.value?.premium ?: 0,
                    currentSongId = currentSongId.value,
                    listSong = listSong.value,
                    navController = navController,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BottomSheetArea(
            userManager = userManager,
            viewModel = viewModel,
            onHideBottomSheet = { viewModel.hideBottomSheet() }
        )
    }


    @Composable
    fun PlayerPadding(currentSongId: Int, content: @Composable () -> Unit) {
        Box(
            modifier = Modifier.padding(
                bottom = if (currentSongId > -1) 64.dp else 0.dp
            )
        ) {
            content()
        }
    }

    @Composable
    fun Modifier.myOwnClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
        this.clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onClick()
        }



    @SuppressLint("UnrememberedMutableState")
    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalMotionApi::class,
    )
    @Composable
    fun Player(
        isPremium: Int,
        currentSongId: Int,
        listSong: List<PlaySong>,
        navController: NavHostController,
        viewModel: MainViewModel,
        modifier: Modifier
    ) {

        if (currentSongId == -1 || listSong.isEmpty()) {
            Box(modifier = modifier.background(Color.Transparent)) {

                BottomNavigation(
                    modifier = Modifier
                        .zIndex(2f)
                        .align(Alignment.BottomCenter),
                    navController = navController
                )
            }
        return
    }

        val currentSong = listSong.find { it.id == currentSongId } ?: return
        val context = LocalContext.current
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val scope = rememberCoroutineScope()
        val lifecycleOwner = LocalLifecycleOwner.current

        val isPlaying by viewModel.isPlaying.collectAsState()
        val isShuffle by viewModel.isShuffle.collectAsState()
        val isRepeat by viewModel.isRepeat.collectAsState()
        val currentPosition by viewModel.currentPosition.collectAsState()
        val duration by viewModel.duration.collectAsState()

        val screenHeight = with(density) {
            configuration.screenHeightDp.dp.toPx()
        }
        val dominantColor = blendColors(color1 = Color(currentSong.dominantColor), color2 = Color(0xFF101010), ratio1 = 0.3f)

        val miniToExpandedState = rememberSwipeableState(PlayerState.MINI)
        val expandedToTopBarState = rememberSwipeableState(PlayerState.EXPANDED)

        val miniToExpandedMotionSceneContent = remember {
            context.resources.openRawResource(R.raw.mini_to_expanded_motion_scene)
                .readBytes()
                .decodeToString()
        }

        val expandedToTopBarMotionSceneContent = remember {
            context.resources.openRawResource(R.raw.expanded_to_topbar_motion_scene)
                .readBytes()
                .decodeToString()
        }

        val miniToExpandedAnchors = mapOf(
            0f to PlayerState.MINI,
            -screenHeight to PlayerState.EXPANDED
        )

        val expandedToTopBarAnchors = mapOf(
            0f to PlayerState.EXPANDED,
            -screenHeight to PlayerState.TOP_BAR
        )

        val miniToExpandedProgress by remember {
            derivedStateOf {
                when {
                    miniToExpandedState.offset.value >= 0 -> 0f
                    else -> abs(miniToExpandedState.offset.value / screenHeight)
                }
            }
        }

        val expandedToTopBarProgress by remember {
            derivedStateOf {
                when {
                    expandedToTopBarState.offset.value >= 0 -> 0f
                    else -> abs(expandedToTopBarState.offset.value / screenHeight)
                }
            }
        }

        LaunchedEffect(currentSong) {
            if (miniToExpandedState.currentValue == PlayerState.MINI) {
                viewModel.prepareSong(currentSong)
                miniToExpandedState.animateTo(PlayerState.EXPANDED)
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver {_, event ->
                viewModel.handleLifecycleEvent(event, isPremium)
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        BackHandler {
            scope.launch {
                if(expandedToTopBarState.currentValue == PlayerState.TOP_BAR) {
                    expandedToTopBarState.animateTo(targetValue = PlayerState.EXPANDED)
                } else {
                    if(miniToExpandedState.currentValue == PlayerState.EXPANDED) {
                        miniToExpandedState.animateTo(targetValue = PlayerState.MINI)
                    }
                }
            }
        }

        Box(
            modifier = modifier
                .background(
                    progressColor(
                        color1 = Color.Transparent,
                        color2 = dominantColor,
                        progress = miniToExpandedProgress
                    )
                )
        ) {
            MotionLayout(
                motionScene = MotionScene(content = miniToExpandedMotionSceneContent),

                progress = miniToExpandedProgress,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {

                BottomNavigation(
                    modifier = Modifier
                        .layoutId("bottomNavigation")
                        .alpha((1f - miniToExpandedProgress).pow(20))
                        .zIndex(2f),
                    navController = navController
                )

                currentSong.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .layoutId("navigationBar")
                            .padding(8.dp)
                            .zIndex(1.5f * miniToExpandedProgress)
                            .alpha((miniToExpandedProgress * (1f - expandedToTopBarProgress)).pow(20)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .myOwnClickable {
                                    if (miniToExpandedState.currentValue == PlayerState.EXPANDED) {
                                        scope.launch { miniToExpandedState.animateTo(PlayerState.MINI) }
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
                                .myOwnClickable {
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
                                .background(Color.Transparent),
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
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f)
                            .layoutId("thumbnail")
                            .zIndex(2f)
                            .alpha((1f - expandedToTopBarProgress).pow(20))
                            .clip(RoundedCornerShape(8.dp))
                            .myOwnClickable {
                                scope.launch {
                                    miniToExpandedState.animateTo(PlayerState.MINI)
                                }
                            }
                            .padding((16f * miniToExpandedProgress.pow(20)).dp)
                            .swipeable(
                                state = miniToExpandedState,
                                anchors = miniToExpandedAnchors,
                                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                                orientation = Orientation.Vertical,
                            ),
                        contentScale = ContentScale.Crop
                    )


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .myOwnClickable {
                                scope.launch {
                                    if (miniToExpandedState.currentValue == PlayerState.MINI) {
                                        miniToExpandedState.animateTo(PlayerState.EXPANDED)
                                    }
                                }
                            }
                            .alpha(
                                ((1f - miniToExpandedProgress) * (1f - expandedToTopBarProgress)).pow(
                                    10
                                )
                            )
                            .layoutId("short")
                            .zIndex(2f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(0.6f),
                            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Text(
                                text = currentSong.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        spacing = MarqueeSpacing(16.dp),
                                        repeatDelayMillis = 0,
                                        animationMode = MarqueeAnimationMode.Immediately
                                    )
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
                            modifier = Modifier
                                .width(96.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Box(modifier = Modifier
                                .size(24.dp)
                                .myOwnClickable { viewModel.previous() }) {
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
                                    painter = painterResource(
                                        if (isPlaying) R.drawable.ic_pause_outline
                                        else R.drawable.ic_play_outline
                                    ),
                                    contentDescription = "Play",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .myOwnClickable { viewModel.playPause() },
                                    tint = Color.White
                                )
                            }

                            Box(modifier = Modifier
                                .size(24.dp)
                                .myOwnClickable { viewModel.next() }) {
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
                            .alpha(
                                ((1f - miniToExpandedProgress) * (1f - expandedToTopBarProgress)).pow(
                                    10
                                )
                            )
                            .zIndex(2f)
                            .layoutId("track")
                    ) {
                        ProgressBarShort(
                            progress = currentPosition.toFloat() / duration.toFloat()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .alpha((miniToExpandedProgress * (1f - expandedToTopBarProgress)).pow(20))
                            .layoutId("details")
                            .zIndex(2f)
                            .myOwnClickable { }
                    ) {
                        SongFullDetails(
                            song = currentSong,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            LikeButton(it.likes)

                            OptionItem(
                                icon = R.drawable.ic_save_to_playlist,
                                iconSize = 24,
                                text = "Save"
                            )

                            OptionItem(
                                icon = R.drawable.ic_share,
                                iconSize = 24,
                                text = "Share"
                            )

                            OptionItem(
                                icon = R.drawable.ic_download,
                                iconSize = 18,
                                text = "Download"
                            )

                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterVertically
                            )
                        ) {
                            ProgressBarDetail(
                                modifier = Modifier,
                                progress = currentPosition.toFloat() / duration.toFloat(),
                                onProgressChanged = { newProgress ->
                                    viewModel.seekToProgress(newProgress)
                                },
                                onSeekTo = { newProgress ->
                                    viewModel.seekToProgress(newProgress)
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(currentPosition),
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatTime(duration),
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

                            Box(modifier = Modifier
                                .size(32.dp)
                                .myOwnClickable { viewModel.shuffle() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shuffle),
                                    contentDescription = "Previous",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = if(isShuffle) Color.White else Color.LightGray
                                )
                            }

                            Box(modifier = Modifier
                                .size(42.dp)
                                .myOwnClickable { viewModel.previous() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_previous),
                                    contentDescription = "Previous",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = if(listSong.indexOfFirst { it.id == currentSongId } > 0) Color.White else Color.LightGray
                                )
                            }

                            Box(modifier = Modifier.size(84.dp)) {
                                Icon(
                                    painter = painterResource(
                                        if (isPlaying) R.drawable.ic_pause_filled
                                        else R.drawable.ic_play_filled
                                    ),
                                    contentDescription = "Play",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .myOwnClickable { viewModel.playPause() },
                                    tint = Color.White
                                )
                            }

                            Box(modifier = Modifier
                                .size(42.dp)
                                .myOwnClickable { viewModel.next() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_skip_next),
                                    contentDescription = "Next",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = if(listSong.indexOfFirst { it.id == currentSongId } < listSong.size - 1) Color.White else Color.LightGray
                                )
                            }

                            Box(modifier = Modifier
                                .size(32.dp)
                                .myOwnClickable { viewModel.repeat() }) {
                                Icon(
                                    painter = painterResource(if(isRepeat == 2) R.drawable.ic_repeat_one else R.drawable.ic_repeat),
                                    contentDescription = "Previous",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = if(isRepeat != 0) Color.White else Color.LightGray
                                )
                            }

                        }
                    }

                    MotionLayout(
                        motionScene = MotionScene(content = expandedToTopBarMotionSceneContent),
                        progress = expandedToTopBarProgress,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .alpha(miniToExpandedProgress.pow(20))
                            .zIndex(if (miniToExpandedState.currentValue == PlayerState.EXPANDED) 2f else 1f)
                    ) {

                        AsyncImage(
                            model = currentSong.thumbnailUrl,
                            contentDescription = currentSong.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.3f)
                                .layoutId("thumbnail")
                                .zIndex(2f)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(16f * (1f - expandedToTopBarProgress).pow(20).dp)
                                ,
                            contentScale = ContentScale.Crop
                        )


                        Row(
                            modifier = Modifier
                                .height(64.dp)
                                .background(Color.Transparent)
                                .myOwnClickable {
                                    scope.launch {
                                        if (miniToExpandedState.currentValue == PlayerState.MINI) {
                                            miniToExpandedState.animateTo(PlayerState.EXPANDED)
                                        }
                                    }
                                }
                                .alpha(expandedToTopBarProgress * miniToExpandedProgress)
                                .layoutId("short")
                                .zIndex(2f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth(0.6f),
                                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                                horizontalAlignment = Alignment.Start
                            ) {

                                Text(
                                    text = currentSong.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            spacing = MarqueeSpacing(16.dp),
                                            repeatDelayMillis = 0,
                                            animationMode = MarqueeAnimationMode.Immediately
                                        )
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
                                modifier = Modifier
                                    .width(96.dp)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Box(modifier = Modifier
                                    .size(24.dp)
                                    .myOwnClickable { viewModel.previous() }) {
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
                                        painter = painterResource(
                                            if (isPlaying) R.drawable.ic_pause_outline
                                            else R.drawable.ic_play_outline
                                        ),
                                        contentDescription = "Play",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .myOwnClickable { viewModel.playPause() },
                                        tint = Color.White
                                    )
                                }

                                Box(modifier = Modifier
                                    .size(24.dp)
                                    .myOwnClickable { viewModel.next() }) {
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
                                .layoutId("anchorDraggable")
                                .alpha(miniToExpandedProgress)
                                .background(
                                    progressColor(
                                        color1 = Color.Transparent,
                                        color2 = Color(currentSong.dominantColor),
                                        progress = expandedToTopBarProgress
                                    ), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                .myOwnClickable(
                                    enabled = expandedToTopBarState.currentValue == PlayerState.EXPANDED,
                                ) {
                                    scope.launch {
                                        expandedToTopBarState.animateTo(PlayerState.TOP_BAR)
                                    }
                                }
                                .swipeable(
                                    state = expandedToTopBarState,
                                    anchors = expandedToTopBarAnchors,
                                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                                    orientation = Orientation.Vertical,
                                    enabled = expandedToTopBarState.currentValue == PlayerState.TOP_BAR || (
                                            expandedToTopBarState.currentValue == PlayerState.EXPANDED
                                                    && miniToExpandedState.currentValue == PlayerState.EXPANDED
                                            )
                                )
                        ) {
                            MusicPlayerTabs(
                                currentSong = currentSong,
                                modifier = Modifier
                                .align(Alignment.TopCenter),
                                dominantColor = dominantColor,
                                progress = expandedToTopBarProgress
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LikeButton(likeCount: Int) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(topStart = 100f, topEnd = 100f, bottomStart = 100f, bottomEnd = 100f))
                .padding(start = 16.dp)
                .background(Color.White.copy(0.3f), RoundedCornerShape(topStart = 100f, topEnd = 100f, bottomStart = 100f, bottomEnd = 100f))
                .padding(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_like_outline),
                contentDescription = "Like",
                tint = Color.White
            )
            Text(
                text = convertIntegerToString(likeCount.toLong()),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            VerticalDivider(Modifier.fillMaxHeight(), thickness = 0.5.dp, color = Color.LightGray)

            Icon(
                painter = painterResource(R.drawable.ic_like_outline),
                contentDescription = "Dislike",
                tint = Color.White,
                modifier = Modifier.rotate(180f)
            )

        }
    }

    @Composable
    private fun OptionItem(icon: Int, iconSize: Int, text: String) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(topStart = 100f, topEnd = 100f, bottomStart = 100f, bottomEnd = 100f))
                .padding(start = 8.dp)
                .background(Color.White.copy(0.3f), RoundedCornerShape(topStart = 100f, topEnd = 100f, bottomStart = 100f, bottomEnd = 100f))
                .padding(vertical = 4.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {

            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(iconSize.dp),
            )

            Text(
                text = text.replaceFirstChar { it.uppercaseChar() },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
                )
        }
    }

    @Composable
    fun MusicPlayerTabs(
        currentSong: PlaySong,
        modifier: Modifier,
        dominantColor: Color,
        progress: Float
    ) {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val coroutineScope = rememberCoroutineScope()
        val tabs = remember { listOf("UP NEXT", "LYRICS", "RELATED") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(64.dp),
                backgroundColor = Color.Transparent,
                contentColor = if(progress > 0f) Color.White else Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(
                            currentTabPosition = tabPositions[selectedTabIndex],
                        )
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTabIndex == index
                    Tab(
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                selectedTabIndex = index
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                color = if(selected) Color.White else Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.background(Color.Transparent)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Transparent),
            ) {
                when(selectedTabIndex) {
                    0 -> UpNextScreen(currentSong = currentSong, dominantColor = dominantColor)
                    1 -> LyricsScreen(lyrics = currentSong.lyrics)
                    2 -> RelatedScreen()
                }
            }
        }

    }


    @Composable
    fun UpNextScreen(dominantColor: Color, currentSong: PlaySong) {
        val listSong by viewModel.listSong.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SongInfoScreen(currentSong, dominantColor)

            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White,
                thickness = 1.dp
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(listSong) { song ->
                    SongItemInfo(
                        song = song,
                        isCurrentSongId = currentSong.id == song.id,
                        dominantColor = dominantColor,
                        onPlayMusicNavigate = { songId ->
                            viewModel.playOneInListSong(songId)
                        }
                    )
                }
            }
        }

    }

    @Composable
    fun SongItemInfo(
        isCurrentSongId: Boolean,
        song: PlaySong,
        dominantColor: Color,
        onPlayMusicNavigate: (Int) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 4.dp)
                .myOwnClickable { onPlayMusicNavigate(song.id) }
                .background(
                    if (isCurrentSongId) blendColors(
                        color1 = dominantColor,
                        color2 = Color.White,
                        ratio1 = 0.7f
                    ) else Color.Transparent
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
        ) {
            Box(modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${song.artist.name} - ${convertIntegerToString(song.views)} views",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

    @SuppressLint("DefaultLocale")
    private fun convertIntegerToString(number: Long): String {
        if (number < 1000) return number.toString()

        val suffixes = arrayOf("", "K", "M", "B")
        var value = number.toDouble()
        var index = 0

        while (value >= 1000 && index < suffixes.size - 1) {
            value /= 1000
            index++
        }

        return if (value >= 10) {
            "${value.toInt()}${suffixes[index]}"
        } else {
            String.format("%.1f%s", value, suffixes[index])
        }
    }

    @Composable
    fun SongInfoScreen(song: PlaySong, dominantColor: Color) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.7f),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = "Playing from",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FloatingActionButton(
                onClick = {},
                modifier = Modifier
                    .height(36.dp)
                    .align(Alignment.CenterEnd),
                backgroundColor = blendColors(color1 = dominantColor, color2 = Color.White, ratio1 = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save_to_playlist),
                        contentDescription = "Shuffle",
                        tint = Color.White
                    )
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    @Composable
    fun LyricsScreen(lyrics: String) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                lyrics.split("\n").forEach { line ->
                    Text(
                        text = line,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    @Composable
    fun RelatedScreen() {
        Text(
            text = "Related"
        )
    }

    private fun progressColor(color1: Color, color2: Color, progress: Float): Color {
        return lerp(
            start = color1,
            stop = color2,
            fraction = progress
        )
    }

    private fun blendColors(color1: Color, color2: Color, ratio1: Float): Color {
        val ratio2 = 1f - ratio1

        return Color(
            red = color1.red * ratio1 + color2.red * ratio2,
            green = color1.green * ratio1 + color2.green * ratio2,
            blue = color1.blue * ratio1 + color2.blue * ratio2,
            alpha = 1f
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
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)) {
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
                        val newProgress =
                            ((currentProgress.floatValue * width + dragAmount) / width).coerceIn(
                                0f,
                                1f
                            )
                        currentProgress.floatValue = newProgress
                        onProgressChanged(newProgress)
                    }
                }
        ) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)) {
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
        userManager: UserManager,
        viewModel: MainViewModel,
        onHideBottomSheet: () -> Unit
    ) {
        val showBottomSheet by viewModel.showBottomSheet.collectAsState()
        val currentBottomSheetSong by viewModel.currentBottomSheetSong.collectAsState()

        if (showBottomSheet && currentBottomSheetSong != null) {
            ModalBottomSheet(
                onDismissRequest = { onHideBottomSheet() },
                containerColor =  Color.Transparent,
                dragHandle = {},

                ) {
                BottomSheetContent(userManager, viewModel, currentBottomSheetSong!!)
            }
        }
    }


    @Composable
    private fun BottomSheetContent(
        userManager: UserManager,
        viewModel: MainViewModel,
        song: QuickPicksSong
    ) {
        val isLikedBottomSheetSong by viewModel.isLikedBottomSheetSong.collectAsState()
        val isDislikeBottomSheetSong by viewModel.isDislikedBottomSheetSong.collectAsState()

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
                            modifier = Modifier.fillMaxWidth(0.7f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                        ) {
                            Text(
                                text = song.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                .clickable { viewModel.updateDislikeStatusBottomSheet(song.id) }
                        ) {
                            Icon(
                                painter = painterResource(if(isDislikeBottomSheetSong) R.drawable.ic_like_filled else R.drawable.ic_like_outline),
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
                                .clickable {
                                    Log.d("BottomSheet", "Like button clicked")
                                    viewModel.updateLikeStatusBottomSheet(song.id)
                                }
                        ) {
                            Icon(
                                painter = painterResource(if(isLikedBottomSheetSong) R.drawable.ic_like_filled else R.drawable.ic_like_outline),
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

    fun reloadActivity() {
        viewModel.reset()
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun makeStatusBarTransparent() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color(0xFF101010).toArgb()
        window.navigationBarColor = Color(0xFF101010).toArgb()

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
    }
}
