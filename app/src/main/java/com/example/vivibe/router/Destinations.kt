package com.example.vivibe.router

import com.example.vivibe.R

interface Destinations {
    val route: String
    val title: String
    val iconFilled: Int
    val iconOutline: Int
}

object UpgradeRouter: Destinations {
    override val route: String = "upgrade"
    override val title: String = "Upgrade"
    override val iconFilled: Int = R.drawable.ic_premium_filled
    override val iconOutline: Int = R.drawable.ic_premium_outline
}
object HomeRouter: Destinations {
    override val route: String = "home"
    override val title: String = "Home"
    override val iconFilled: Int = R.drawable.ic_home_filled
    override val iconOutline: Int = R.drawable.ic_home_outline
}

object ExploreRouter: Destinations {
    override val route: String = "explore"
    override val title: String = "Explore"
    override val iconFilled: Int = R.drawable.ic_explore_filled
    override val iconOutline: Int = R.drawable.ic_explore_outline
}

object LibraryRouter: Destinations {
    override val route: String = "library"
    override val title: String = "Library"
    override val iconFilled: Int = R.drawable.ic_library_filled
    override val iconOutline: Int = R.drawable.ic_library_outline
}

object SamplesRouter: Destinations {
    override val route: String = "samples"
    override val title: String = "Samples"
    override val iconFilled: Int = R.drawable.ic_samples_filled
    override val iconOutline: Int = R.drawable.ic_samples_outline
}

object NotificationsRouter: Destinations {
    override val route: String = "notifications"
    override val title: String = "Notifications"
    override val iconFilled: Int = R.drawable.ic_notifications
    override val iconOutline: Int = R.drawable.ic_notifications
}

object SearchRouter: Destinations {
    override val route: String = "search"
    override val title: String = "Search"
    override val iconFilled: Int = R.drawable.ic_search
    override val iconOutline: Int = R.drawable.ic_search
}

object PlayMusicRouter: Destinations {
    const val songIdArg = "song_id"
    override val route: String = "play_music"
    override val title: String = ""
    override val iconFilled: Int = 0
    override val iconOutline: Int = 0
}
object PaymentRouter: Destinations {
    override val route: String = "payment"
    override val title: String = "Payment"
    override val iconFilled: Int = 0
    override val iconOutline: Int = 0
}
