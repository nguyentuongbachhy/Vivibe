package com.example.vivibe.router

import com.example.vivibe.R

interface Destinations {
    val route: String
    val title: String
    val iconFilled: Int
    val iconOutline: Int
}


object HomeRouter: Destinations {
    override val route: String = "Home"
    override val title: String = "Home"
    override val iconFilled: Int = R.drawable.ic_home_filled
    override val iconOutline: Int = R.drawable.ic_home_outline
}

object ExploreRouter: Destinations {
    override val route: String = "Explore"
    override val title: String = "Explore"
    override val iconFilled: Int = R.drawable.ic_explore_filled
    override val iconOutline: Int = R.drawable.ic_explore_outline
}

object LibraryRouter: Destinations {
    override val route: String = "Library"
    override val title: String = "Library"
    override val iconFilled: Int = R.drawable.ic_library_filled
    override val iconOutline: Int = R.drawable.ic_library_outline
}

object SamplesRouter: Destinations {
    override val route: String = "Samples"
    override val title: String = "Samples"
    override val iconFilled: Int = R.drawable.ic_samples_filled
    override val iconOutline: Int = R.drawable.ic_samples_outline
}

object NotificationsRouter: Destinations {
    override val route: String = "Notifications"
    override val title: String = "Notifications"
    override val iconFilled: Int = R.drawable.ic_notifications
    override val iconOutline: Int = R.drawable.ic_notifications
}

object SearchRouter: Destinations {
    override val route: String = "Search"
    override val title: String = "Search"
    override val iconFilled: Int = R.drawable.ic_search
    override val iconOutline: Int = R.drawable.ic_search

}