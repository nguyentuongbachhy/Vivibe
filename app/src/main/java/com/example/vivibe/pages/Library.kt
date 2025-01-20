package com.example.vivibe.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vivibe.R


class Library {
    @Composable
    fun LibraryScreen(navController: NavController) { // Thêm tham số navController
        val context = LocalContext.current
        var isListView by remember { mutableStateOf(true) } // Trạng thái chuyển đổi List/Grid

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("search") // Điều hướng đến SearchScreen
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Thư viện",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Account clicked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_account),
                        contentDescription = "Account",
                        tint = Color.White
                    )
                }
            }

            // Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { TabItem(title = "Playlist") }
                item { TabItem(title = "Songs") }
            }

            // Toggle List/Grid Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(if (isListView) R.drawable.ic_grid_view else R.drawable.ic_list_view),
                    contentDescription = if (isListView) "Switch to Grid View" else "Switch to List View",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            isListView = !isListView
                        }
                )
            }

            // Content Section
            if (isListView) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { LibraryItem(title = "Liked Music", subtitle = "Auto playlist", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Twenty One Pilots", subtitle = "Artist - 13.6M subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Charlie Puth", subtitle = "Artist - 22.7 Tr subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Đen", subtitle = "Artist - 5.12 Tr subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Saved Music", subtitle = "Song you save for later", icon = R.drawable.save, isListView = isListView) }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { LibraryItem(title = "Liked Music", subtitle = "Auto playlist", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Twenty One Pilots", subtitle = "Artist - 13.6M subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Charlie Puth", subtitle = "Artist - 22,7 Tr subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Đen", subtitle = "Artist - 5,12 Tr subscibers", icon = R.drawable.like, isListView = isListView) }
                    item { LibraryItem(title = "Saved Music", subtitle = "Song you save for later", icon = R.drawable.save, isListView = isListView) }
                }
            }

            // Add Button
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        Toast.makeText(context, "Add button clicked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF202020)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }
        }
    }

    @Composable
    fun TabItem(title: String) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(8.dp)
                .background(Color(0xFF202020), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    @Composable
    fun LibraryItem(title: String, subtitle: String, icon: Int, isListView: Boolean) {
        val iconSize = if (isListView) 40.dp else 64.dp
        val context = LocalContext.current

        if (isListView) {
            // ListView: Horizontal layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF202020), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Item Icon",
                        modifier = Modifier.size(iconSize)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Chỉ hiển thị ic_pin ở các mục cụ thể
                        if (title == "Liked Music" || title == "Saved Music") {
                            Icon(
                                painter = painterResource(R.drawable.ic_pin),
                                contentDescription = "Tag Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = subtitle,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f) // Đẩy text sang trái
                        )
                        IconButton(
                            onClick = {
                                // Hiển thị Toast khi nhấn vào biểu tượng
                                Toast.makeText(context, "More options clicked for $title", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = "More Options",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            // GridView: Vertical layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF202020), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Item Icon",
                        modifier = Modifier.size(iconSize)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Chỉ hiển thị ic_pin ở các mục cụ thể
                    if (title == "Liked Music" || title == "Saved Music") {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = "Tag Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f) // Đẩy text sang trái
                    )
                    IconButton(
                        onClick = {
                            // Hiển thị Toast khi nhấn vào biểu tượng
                            Toast.makeText(context, "More options clicked for $title", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more),
                            contentDescription = "More Options",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }


}

