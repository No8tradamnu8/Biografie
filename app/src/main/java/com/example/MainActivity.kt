package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BookCoverScreen
import com.example.ui.screens.DiaryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DiaryViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val diaryViewModel: DiaryViewModel = viewModel()

    NavHost(navController = navController, startDestination = "book_cover") {
        composable("book_cover") {
            BookCoverScreen(onUnlock = {
                navController.navigate("diary") {
                    popUpTo("book_cover") { inclusive = true }
                }
            })
        }
        composable("diary") {
            DiaryScreen(
                viewModel = diaryViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
