package com.example.vivibe.pages.explore

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.vivibe.MainActivity
import com.example.vivibe.R
import com.example.vivibe.manager.UserManager
import com.example.vivibe.model.User
import com.example.vivibe.router.AccountRouter
import com.example.vivibe.router.CommonRouter
import com.example.vivibe.router.SearchRouter
import com.example.vivibe.router.TopAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Explore(private val appContext: Context) {
    private val userManager = UserManager.getInstance(appContext)
    private val user = userManager.userState

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun ExploreScreen(exploreViewModel: ExploreViewModel, navController: NavController, onPlayMusicNavigate: (Int) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scope = rememberCoroutineScope()

            TopAppBar { ExploreActions(appContext, scope, navController, user.value, exploreViewModel) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SquareButton("New releases", R.drawable.ic_new_release) {
                        scope.launch {
                            navController.navigate("${CommonRouter.route}/0")
                        }
                    }
                }
                item {
                    SquareButton("Chart", R.drawable.ic_rank) {
                        scope.launch {
                            navController.navigate("${CommonRouter.route}/1")
                        }
                    }
                }
                item {
                    SquareButton("Moods & genres", R.drawable.ic_happy) {
                        scope.launch {
                            navController.navigate("${CommonRouter.route}/2")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SquareButton(text: String, iconResId: Int, onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable{ onClick() }
                .background(Color(0xFF1F1F1F), RoundedCornerShape(12.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = text,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun ExploreActions(
        context: Context,
        scope: CoroutineScope,
        navController: NavController,
        user: User?,
        exploreViewModel: ExploreViewModel
    ) {
        Row(
            modifier = Modifier.width(100.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

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

            if (user == null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val success = exploreViewModel.signIn(context)
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
                            scope.launch {
                                navController.navigate(AccountRouter.route)
                            }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = user.profilePictureUri),
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