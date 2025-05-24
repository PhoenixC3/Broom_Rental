package com.example.hogwartshoppers.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hogwartshoppers.Login

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Screens.Login.route)
    {
        composable(route = Screens.Login.route){
            Login(navController = navController)
        }

        composable(route = Screens.Camera.route) {
            CameraScreen(navController = navController)
        }

        composable(route = Screens.Register.route){
            RegisterScreen(navController = navController)
        }

        composable(route = Screens.HomeScreen.route){
            MapScreen(navController = navController)
        }

        composable(route = Screens.CustomizeBroom.route + "?broom={broom}"){ navBackStack ->
            val broom: String = navBackStack.arguments?.getString("broom").toString()
            CustomizeBroomScreen(navController = navController, selectedBroomName = broom)
        }

        composable(route = Screens.Settings.route){
            SettingsScreen(navController = navController)
        }

        composable(route = Screens.BroomDetails.route + "?broom={broom}"){ navBackStack ->
            val broom: String = navBackStack.arguments?.getString("broom").toString()
            BroomDetailsScreen(navController = navController, selectedBroomName = broom)
        }

        composable(route = Screens.Profile.route + "?email={email}"){ navBackStack ->
            val email: String = navBackStack.arguments?.getString("email").toString()
            ProfileScreen(navController = navController, userMail = email)
        }

        composable(route = Screens.Friends.route + "?acceptedRequest={acceptedRequest}"){ navBackStack ->
            val acceptedRequest = navBackStack.arguments?.getString("acceptedRequest").toBoolean()
            FriendsScreen(navController = navController, acceptedRequest = acceptedRequest)
        }

        composable(route = Screens.TripHistory.route){
            TripHistoryScreen(navController = navController)
        }

        composable(route = Screens.RaceConditions.route + "?friendEmail={friendEmail}"){ navBackStack ->
            val friendEmail: String = navBackStack.arguments?.getString("friendEmail").toString()
            RaceConditions(navController = navController, friendEmail = friendEmail)
        }

        composable(route = Screens.Race.route + "?friendEmail={friendEmail}") { navBackStack ->
            val friendEmail: String = navBackStack.arguments?.getString("friendEmail").toString()
            Race(navController = navController, friendEmail = friendEmail)
        }

        composable(route = Screens.Forum.route){
            ForumScreen(navController = navController)
        }

        composable(route = Screens.ForumPost.route + "?postEmail={postEmail}" + "&postTitle={postTitle}"){ navBackStack ->
            val postEmail: String = navBackStack.arguments?.getString("postEmail").toString()
            val postTitle: String = navBackStack.arguments?.getString("postTitle").toString()
            ForumPostScreen(navController = navController, postEmail = postEmail, postTitle = postTitle)
        }

        composable(route = Screens.About.route){
            About(navController = navController)
        }
    }
}