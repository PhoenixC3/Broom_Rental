package com.example.hogwartshoppers.screens

sealed class Screens (val route: String) {
    object HomeScreen : Screens("home_screen")
    object Login : Screens("login_screen")
    object Register : Screens("register_screen")
    object Profile : Screens("profile_screen/{email}")
    object BroomDetails : Screens("broom_details_screen/{broom}")
    object TripHistory: Screens("trip_history_screen")
    object CustomizeBroom : Screens("customize_broom_screen/{broom}")
    object Forum : Screens("forum_screen")
    object ForumPost : Screens("forum_post_screen/{postEmail}/{postTitle}")
    object Friends : Screens("friends_screen/{acceptedRequest}")
    object Settings : Screens("settings_screen")
    object RaceConditions : Screens("race_conditions_screen/{friendEmail}")
    object Race : Screens("race_screen/{friendEmail}")
    object Camera: Screens("camera")
    object About: Screens("about")
}