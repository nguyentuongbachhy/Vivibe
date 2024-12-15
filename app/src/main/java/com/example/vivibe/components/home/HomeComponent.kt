package com.example.vivibe.components.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vivibe.Genre
import com.example.vivibe.QuickPicksSong
import com.example.vivibe.R
import com.example.vivibe.User

class HomeComponent(private val viewModel: HomeComponentViewModel) {
    @Composable
    fun GenresScreen() {
        val genres = viewModel.genreList.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            genres.value.forEach { genre ->
                GenreItem(genre)
            }
        }
    }

    @Composable
    fun QuickPicksScreen(onSongMoreClick: (QuickPicksSong) -> Unit) {

        val quickPicksSong = viewModel.quickPicks.collectAsState()

        val itemsPerColumn = 4

        val pages = quickPicksSong.value.chunked(itemsPerColumn)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quick Picks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 8.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color(0xFF1A191C))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF202020),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .shadow(6.dp, RoundedCornerShape(8.dp))
                        .clickable {}
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Play all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth().height(250.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                pages.forEach { songs ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 8.dp)
                    ) {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(4),
                            modifier = Modifier.widthIn(max = 280.dp).heightIn(max = 250.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(songs) { song ->
                                QuickPickSongItem(song, onSongMoreClick)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun QuickPickSongItem(song: QuickPicksSong, onSongMoreClick: (QuickPicksSong) -> Unit) {
        Row(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .heightIn(max = 56.dp)
                .padding(0.dp)
                .clip(RoundedCornerShape(4.dp))
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

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

            Spacer(modifier = Modifier.weight(0.1f))

            FloatingActionButton(
                onClick = {onSongMoreClick(song)},
                modifier = Modifier.size(24.dp),
                containerColor = Color(0xFF101010),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        }
    }

    @Composable
    fun SpeedDialScreen(user: User?) {
        val speedDialSongs = viewModel.speedDial.collectAsState()
        val itemsPerPage = 9
        val pages = speedDialSongs.value.chunked(itemsPerPage)
        val pagerState = rememberPagerState(initialPage = 0) { pages.size }

        UserInfo(user)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) { pageIndex ->
            val currentPageItems = pages[pageIndex]
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 0.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Top
            ) {
                items(currentPageItems) {song ->
                    SpeedDialSongItem(
                        thumbnail = song.thumbnailUrl,
                        title = song.title
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) {pageIndex ->
                val isSelected = pagerState.currentPage == pageIndex
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.DarkGray)
                        .padding(8.dp)
                )
            }
        }
    }

    @Composable
    private fun GenreItem(genre: Genre) {
        Box(
            modifier = Modifier
                .padding(start = 0.dp, end = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A191C))
                .border(
                    width = 1.dp,
                    color = Color(0xFF202020),
                    shape = RoundedCornerShape(8.dp)
                )
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clickable {}
                .padding(12.dp)
        ) {
            Text(
                text = genre.name.replaceFirstChar { it.uppercaseChar() },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }


    @SuppressLint("DefaultLocale")
    private fun convertIntegerToString(number: Int): String {
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
    fun UserInfo(user: User?) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user?.profilePictureUri,
                contentDescription = user?.name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
            )

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user?.name ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.LightGray
                )
                Text(
                    text = "Speed dial",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    @Composable
    private fun SpeedDialSongItem(
        thumbnail: String,
        title: String
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {

            AsyncImage(
                model = thumbnail,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = 100f
                        )
                    )
            )

            Text(
                text = title.replaceFirstChar { it.uppercaseChar() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

