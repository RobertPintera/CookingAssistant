package com.cookingassistant

import android.content.pm.PackageManager
import com.cookingassistant.ui.screens.home.HomeScreen
import com.cookingassistant.ui.screens.login.LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import android.Manifest
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cookingassistant.data.repositories.TokenRepository
import com.cookingassistant.data.network.RetrofitClient
import com.cookingassistant.services.RecipeService
import com.cookingassistant.services.AuthService
import com.cookingassistant.ui.screens.home.LoginViewModel
import com.cookingassistant.ui.screens.registration.RegistrationScreen
import com.cookingassistant.ui.screens.registration.RegistrationViewModel
import com.cookingassistant.compose.AppTheme
import com.cookingassistant.data.ShoppingProducts
import com.cookingassistant.services.ReviewService
import com.cookingassistant.services.UserService
import com.cookingassistant.ui.composables.ShoppingList.ShoppingList
import com.cookingassistant.ui.composables.topappbar.TopAppBar
import com.cookingassistant.ui.composables.topappbar.TopAppBarViewModel
import com.cookingassistant.ui.screens.home.HomeScreenViewModel
import com.cookingassistant.ui.screens.recipescreen.RecipeScreen
import com.cookingassistant.ui.screens.recipescreen.RecipeScreenViewModel


class MainActivity : ComponentActivity() {
    // Declare the permission request contract
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, proceed with your logic
            println("Permission granted")
        } else {
            // Permission denied, handle accordingly
            println("Permission denied")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request permission if not already granted
        checkAndRequestPermission()

        //Retrofit HTTP Client creation (singleton)
        val tokenRepository = TokenRepository(applicationContext)
        val apiRepository = RetrofitClient(tokenRepository).retrofit
        // Create services
        val authService = AuthService(apiRepository)
        val userService = UserService(apiRepository)
        val reviewService = ReviewService(apiRepository)
        val recipeService = RecipeService(apiRepository)
        setContent {
            AppTheme {
                AppNavigator(authService,userService,reviewService,recipeService ,tokenRepository) // inject services here
            }

        }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) ==
                        PackageManager.PERMISSION_GRANTED -> {

                    println("Permission already granted")
                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) -> {

                    println("Need permission to access images")
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
                else -> {

                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {

            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, proceed with file access
                    println("Permission already granted")

                }
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // Show rationale if necessary and request permission
                    println("Need permission to access storage")
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                else -> {
                    // Request the permission
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }


    }
}

@Composable
// modify this code to inject services
fun AppNavigator(authService: AuthService,userService: UserService,reviewService: ReviewService, recipeService: RecipeService, tokenRepository: TokenRepository){
    val navController = rememberNavController()
    NavGraph(navController = navController,
        authService = authService,
        userService = userService,
        reviewService = reviewService,
        recipeService = recipeService,
        tokenRepository = tokenRepository)
}

@Composable
fun NavGraph(navController: NavHostController, authService: AuthService, userService: UserService,reviewService: ReviewService,recipeService: RecipeService, tokenRepository: TokenRepository) {
    AppTheme {
        val rsvm = RecipeScreenViewModel(recipeService,userService, reviewService)
        val topBarViewModel = TopAppBarViewModel(recipeService, rsvm, navController)
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                // create viewModel and inject service
                // TODO: Implement factories later
                //val loginViewModel: LoginViewModel = ViewModelProvider(LoginViewModelFactory(userService))
                val loginViewModel = LoginViewModel(authService, tokenRepository)
                LoginScreen(navController, loginViewModel)
            }
            composable("home") {
                val homeViewModel = HomeScreenViewModel(recipeService, userService)
                TopAppBar(topAppBarviewModel = topBarViewModel) {
                    HomeScreen(navController, homeViewModel)
                }
            }
            composable("test") {//For testing purposes

//                ShoppingProducts.loadProducts(LocalContext.current)
//                ShoppingList()
            }
            composable("registration") {
                val registrationViewModel = RegistrationViewModel(authService)
                RegistrationScreen(navController, registrationViewModel)
            }
            composable("recipeScreen") {
                TopAppBar(topAppBarviewModel = topBarViewModel) {
                    RecipeScreen(rsvm)
                }
            }
        }
    }
}

