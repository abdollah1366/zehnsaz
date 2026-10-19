package com.example.zehnchin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.repository.ChapterRepository
import com.example.zehnchin.domain.repository.LevelRepository
import com.example.zehnchin.domain.repository.PlayerRepository
import com.example.zehnchin.ui.achievements.AchievementScreen
import com.example.zehnchin.ui.assessment.AssessmentScreen
import com.example.zehnchin.ui.assessment.AssessmentViewModel
import com.example.zehnchin.ui.attention.SelectiveAttentionScreen
import com.example.zehnchin.ui.attention.SelectiveAttentionViewModel
import com.example.zehnchin.ui.common.ActivityProvider
import com.example.zehnchin.ui.common.CognitiveResultScreen
import com.example.zehnchin.ui.common.drawGameBackdrop
import com.example.zehnchin.ui.control.InhibitoryControlScreen
import com.example.zehnchin.ui.control.InhibitoryControlViewModel
import com.example.zehnchin.ui.flexibility.FlexibilityScreen
import com.example.zehnchin.ui.flexibility.FlexibilityViewModel
import com.example.zehnchin.ui.home.*
import com.example.zehnchin.ui.memory.WorkingMemoryScreen
import com.example.zehnchin.ui.memory.WorkingMemoryViewModel
import com.example.zehnchin.ui.problemsolving.ProblemSolvingScreen
import com.example.zehnchin.ui.problemsolving.ProblemSolvingViewModel
import com.example.zehnchin.ui.profile.ProfileScreen
import com.example.zehnchin.ui.profile.ProfileViewModel
import com.example.zehnchin.ui.rewards.DailyRewardScreen
import com.example.zehnchin.ui.settings.SettingsScreen
import com.example.zehnchin.ui.settings.SettingsViewModel
import com.example.zehnchin.ui.shop.ShopScreen
import com.example.zehnchin.ui.shop.ShopViewModel
import com.example.zehnchin.ui.speed.ProcessingSpeedScreen
import com.example.zehnchin.ui.speed.ProcessingSpeedViewModel
import com.example.zehnchin.ui.theme.DarkColorScheme
import com.example.zehnchin.ui.theme.DarkFeedbackColors
import com.example.zehnchin.ui.theme.LocalFeedbackColors
import com.example.zehnchin.ui.theme.ZehnChinTheme
import com.example.zehnchin.ui.theme.domainColor
import com.example.zehnchin.ui.training.LevelSelectionScreen
import com.example.zehnchin.ui.training.LevelSelectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var chapterRepository: ChapterRepository

    @Inject
    lateinit var playerRepository: PlayerRepository

    @Inject
    lateinit var levelRepository: LevelRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("fa", "IR")
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Persian Locale globally (extra safety)
        val locale = Locale("fa", "IR")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        activityProvider.setActivity(this)
        enableEdgeToEdge()
        setContent {
            ZehnChinTheme {
                ZehnchinApp(chapterRepository, levelRepository, playerRepository)
            }
        }
    }
}

// Navigation graph route -> chapter whose accent colour the game uses.
private val gameGraphChapters = mapOf(
    "attention_graph" to 1,
    "memory_graph" to 2,
    "speed_graph" to 3,
    "control_graph" to 4,
    "flexibility_graph" to 5,
    "problem_solving_graph" to 6
)

/** The chapter of the game this destination belongs to, found by walking up to its navigation graph. */
private fun NavDestination.gameChapter(): Int? {
    var current: NavDestination? = this
    while (current != null) {
        current.route?.let { gameGraphChapters[it] }?.let { return it }
        current = current.parent
    }
    return null
}

/**
 * Switches to the dark, game-like look while a training game (or its result) is on screen.
 * Every other screen keeps the light brand theme.
 */
@Composable
private fun GameAwareTheme(gameChapter: Int?, content: @Composable (inGame: Boolean) -> Unit) {
    val inGame = gameChapter != null
    MaterialTheme(
        colorScheme = if (inGame) DarkColorScheme else MaterialTheme.colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        CompositionLocalProvider(
            LocalFeedbackColors provides (if (inGame) DarkFeedbackColors else LocalFeedbackColors.current)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { if (inGame) drawGameBackdrop(domainColor(gameChapter ?: 0)) }
            ) {
                content(inGame)
            }
        }
    }
}

@Composable
fun ZehnchinApp(
    chapterRepository: ChapterRepository,
    levelRepository: LevelRepository,
    playerRepository: PlayerRepository
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val gameChapter = backStackEntry?.destination?.gameChapter()

    GameAwareTheme(gameChapter = gameChapter) { inGame ->
        ZehnchinNavHost(navController, inGame, chapterRepository, levelRepository, playerRepository)
    }
}

@Composable
private fun ZehnchinNavHost(
    navController: NavHostController,
    inGame: Boolean,
    chapterRepository: ChapterRepository,
    levelRepository: LevelRepository,
    playerRepository: PlayerRepository
) {
    val homeViewModel: HomeViewModel = hiltViewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (inGame) Color.Transparent else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToTraining = { navController.navigate("chapter_list") },
                    onNavigateToAchievements = { navController.navigate("achievements") },
                    onNavigateToShop = { navController.navigate("shop") },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAssessment = { navController.navigate("assessment") }
                )
            }

            composable("settings") {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable("chapter_list") {
                ChapterListScreen(
                    repository = chapterRepository,
                    onChapterSelected = { chapter ->
                        navController.navigate("level_selection/${chapter.id}")
                    }
                )
            }

            composable(
                route = "level_selection/{chapterId}",
                arguments = listOf(navArgument("chapterId") { type = NavType.IntType })
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
                val viewModel: LevelSelectionViewModel = hiltViewModel()
                LevelSelectionScreen(
                    chapterId = chapterId,
                    viewModel = viewModel,
                    onLevelSelected = { level ->
                        val gameRoute = when (chapterId) {
                            0 -> "selective_attention/${level.levelNumber}/0"
                            1 -> "selective_attention/${level.levelNumber}/1"
                            2 -> "working_memory/${level.levelNumber}"
                            3 -> "processing_speed/${level.levelNumber}"
                            4 -> "inhibitory_control/${level.levelNumber}"
                            5 -> "cognitive_flexibility/${level.levelNumber}"
                            6 -> "problem_solving/${level.levelNumber}"
                            else -> "selective_attention/${level.levelNumber}/$chapterId"
                        }
                        navController.navigate(gameRoute)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // --- ATTENTION & TUTORIAL ---
            navigation(
                startDestination = "selective_attention/{levelNum}/{chapterId}",
                route = "attention_graph"
            ) {
                composable(
                    route = "selective_attention/{levelNum}/{chapterId}",
                    arguments = listOf(
                        navArgument("levelNum") { type = NavType.IntType },
                        navArgument("chapterId") { type = NavType.IntType }
                    )
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val chapterId = entry.arguments?.getInt("chapterId") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("attention_graph") }
                    val viewModel: SelectiveAttentionViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum, chapterId) {
                        levelRepository.getLevel(chapterId, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    SelectiveAttentionScreen(
                        viewModel = viewModel,
                        onNextLevel = { nextNum ->
                            navController.navigate("selective_attention/$nextNum/$chapterId") {
                                popUpTo("attention_graph") { inclusive = false }
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/$chapterId", false) }
                    )
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("attention_graph") }
                    val viewModel: SelectiveAttentionViewModel = hiltViewModel(graphEntry)
                    
                    CognitiveResultScreen(
                        titleRes = R.string.attention_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val prevEntry = navController.previousBackStackEntry
                            val currentLevelNum = prevEntry?.arguments?.getInt("levelNum") ?: 1
                            val chapterId = prevEntry?.arguments?.getInt("chapterId") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            
                            if (levelRepository.getLevel(chapterId, nextLevelNum) != null) {
                                navController.navigate("selective_attention/$nextLevelNum/$chapterId") {
                                    popUpTo("attention_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/$chapterId", false)
                            }
                        },
                        onBackToLevels = {
                            val chapterId = navController.previousBackStackEntry?.arguments?.getInt("chapterId") ?: 1
                            navController.popBackStack("level_selection/$chapterId", false)
                        },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            // --- MEMORY ---
            navigation(
                startDestination = "working_memory/{levelNum}",
                route = "memory_graph"
            ) {
                composable(
                    route = "working_memory/{levelNum}",
                    arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("memory_graph") }
                    val viewModel: WorkingMemoryViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum) {
                        levelRepository.getLevel(2, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    WorkingMemoryScreen(
                        viewModel = viewModel,
                        onNextLevel = { nextNum ->
                            navController.navigate("working_memory/$nextNum") {
                                popUpTo("memory_graph") { inclusive = false }
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/2", false) }
                    )
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("memory_graph") }
                    val viewModel: WorkingMemoryViewModel = hiltViewModel(graphEntry)
                    CognitiveResultScreen(
                        titleRes = R.string.memory_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val currentLevelNum = navController.previousBackStackEntry?.arguments?.getInt("levelNum") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            if (levelRepository.getLevel(2, nextLevelNum) != null) {
                                navController.navigate("working_memory/$nextLevelNum") {
                                    popUpTo("memory_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/2", false)
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/2", false) },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            // --- SPEED ---
            navigation(
                startDestination = "processing_speed/{levelNum}",
                route = "speed_graph"
            ) {
                composable(
                    route = "processing_speed/{levelNum}",
                    arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("speed_graph") }
                    val viewModel: ProcessingSpeedViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum) {
                        levelRepository.getLevel(3, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    ProcessingSpeedScreen(
                        viewModel = viewModel,
                        onNextLevel = { nextNum ->
                            navController.navigate("processing_speed/$nextNum") {
                                popUpTo("speed_graph") { inclusive = false }
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/3", false) }
                    )
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("speed_graph") }
                    val viewModel: ProcessingSpeedViewModel = hiltViewModel(graphEntry)
                    CognitiveResultScreen(
                        titleRes = R.string.speed_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val currentLevelNum = navController.previousBackStackEntry?.arguments?.getInt("levelNum") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            if (levelRepository.getLevel(3, nextLevelNum) != null) {
                                navController.navigate("processing_speed/$nextLevelNum") {
                                    popUpTo("speed_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/3", false)
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/3", false) },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            // --- CONTROL ---
            navigation(
                startDestination = "inhibitory_control/{levelNum}",
                route = "control_graph"
            ) {
                composable(
                    route = "inhibitory_control/{levelNum}",
                    arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("control_graph") }
                    val viewModel: InhibitoryControlViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum) {
                        levelRepository.getLevel(4, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    InhibitoryControlScreen(viewModel = viewModel, onSessionComplete = { navController.navigate("result") })
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("control_graph") }
                    val viewModel: InhibitoryControlViewModel = hiltViewModel(graphEntry)
                    CognitiveResultScreen(
                        titleRes = R.string.control_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val currentLevelNum = navController.previousBackStackEntry?.arguments?.getInt("levelNum") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            if (levelRepository.getLevel(4, nextLevelNum) != null) {
                                navController.navigate("inhibitory_control/$nextLevelNum") {
                                    popUpTo("control_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/4", false)
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/4", false) },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            // --- FLEXIBILITY ---
            navigation(
                startDestination = "cognitive_flexibility/{levelNum}",
                route = "flexibility_graph"
            ) {
                composable(
                    route = "cognitive_flexibility/{levelNum}",
                    arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("flexibility_graph") }
                    val viewModel: FlexibilityViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum) {
                        levelRepository.getLevel(5, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    FlexibilityScreen(viewModel = viewModel, onSessionComplete = { navController.navigate("result") })
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("flexibility_graph") }
                    val viewModel: FlexibilityViewModel = hiltViewModel(graphEntry)
                    CognitiveResultScreen(
                        titleRes = R.string.flexibility_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val currentLevelNum = navController.previousBackStackEntry?.arguments?.getInt("levelNum") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            if (levelRepository.getLevel(5, nextLevelNum) != null) {
                                navController.navigate("cognitive_flexibility/$nextLevelNum") {
                                    popUpTo("flexibility_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/5", false)
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/5", false) },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            // --- PROBLEM SOLVING ---
            navigation(
                startDestination = "problem_solving/{levelNum}",
                route = "problem_solving_graph"
            ) {
                composable(
                    route = "problem_solving/{levelNum}",
                    arguments = listOf(navArgument("levelNum") { type = NavType.IntType })
                ) { entry ->
                    val levelNum = entry.arguments?.getInt("levelNum") ?: 1
                    val graphEntry = remember(entry) { navController.getBackStackEntry("problem_solving_graph") }
                    val viewModel: ProblemSolvingViewModel = hiltViewModel(graphEntry)
                    
                    LaunchedEffect(levelNum) {
                        levelRepository.getLevel(6, levelNum)?.let { viewModel.loadLevel(it) }
                    }

                    ProblemSolvingScreen(viewModel = viewModel, onLevelSolved = { navController.navigate("result") })
                }
                composable("result") { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry("problem_solving_graph") }
                    val viewModel: ProblemSolvingViewModel = hiltViewModel(graphEntry)
                    CognitiveResultScreen(
                        titleRes = R.string.problem_solving_result_title,
                        result = viewModel.calculateResults(),
                        onNextLevel = {
                            val currentLevelNum = navController.previousBackStackEntry?.arguments?.getInt("levelNum") ?: 1
                            val nextLevelNum = currentLevelNum + 1
                            if (levelRepository.getLevel(6, nextLevelNum) != null) {
                                navController.navigate("problem_solving/$nextLevelNum") {
                                    popUpTo("problem_solving_graph") { inclusive = false }
                                }
                            } else {
                                navController.popBackStack("level_selection/6", false)
                            }
                        },
                        onBackToLevels = { navController.popBackStack("level_selection/6", false) },
                        onExit = { navController.popBackStack("home", false) }
                    )
                }
            }

            composable("assessment") {
                val viewModel: AssessmentViewModel = hiltViewModel()
                AssessmentScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable("profile") {
                val profile by playerRepository.getPlayerProfile().collectAsState(null)
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val domains by profileViewModel.domains.collectAsState()
                val assessmentTrends by profileViewModel.assessmentTrends.collectAsState()
                val weeklyPlan by profileViewModel.weeklyPlan.collectAsState()
                ProfileScreen(
                    profile = profile,
                    domains = domains,
                    assessmentTrends = assessmentTrends,
                    weeklyPlan = weeklyPlan,
                    onStartSession = { chapterId -> navController.navigate("level_selection/$chapterId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("achievements") {
                val achievements by playerRepository.getAllAchievements().collectAsState(emptyList())
                AchievementScreen(achievements = achievements, onBack = { navController.popBackStack() })
            }

            composable("daily_reward") {
                val profile by playerRepository.getPlayerProfile().collectAsState(null)
                val isClaimed by homeViewModel.isDailyRewardClaimed.collectAsState()
                DailyRewardScreen(
                    currentDay = ((profile?.currentStreak ?: 0) % 7) + 1,
                    isClaimed = isClaimed,
                    onClaim = { homeViewModel.claimDailyReward() }
                )
            }
            
            composable("shop") {
                val viewModel: ShopViewModel = hiltViewModel()
                ShopScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
