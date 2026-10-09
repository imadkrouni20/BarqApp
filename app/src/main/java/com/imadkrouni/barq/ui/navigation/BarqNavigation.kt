package com.imadkrouni.barq.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imadkrouni.barq.ui.screens.*

object Routes {
    const val HOME = "home"
    const val FLOOR_PLAN = "floor/{projectId}"
    const val THREE_D = "3d/{projectId}"
    const val PANEL = "panel/{projectId}"
    const val BOM = "bom/{projectId}"
    
    fun floorPlan(id: Long) = "floor/$id"
    fun threeD(id: Long) = "3d/$id"
    fun panel(id: Long) = "panel/$id"
    fun bom(id: Long) = "bom/$id"
}

@Composable
fun BarqNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onOpenProject = { id -> nav.navigate(Routes.floorPlan(id)) })
        }
        composable(Routes.FLOOR_PLAN, arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("projectId") ?: 0L
            FloorPlanScreen(
                projectId = id,
                onBack = { nav.popBackStack() },
                onOpen3D = { nav.navigate(Routes.threeD(id)) },
                onOpenPanel = { nav.navigate(Routes.panel(id)) },
                onOpenBOM = { nav.navigate(Routes.bom(id)) }
            )
        }
        composable(Routes.THREE_D, arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("projectId") ?: 0L
            ThreeDViewScreen(projectId = id, onBack = { nav.popBackStack() })
        }
        composable(Routes.PANEL, arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("projectId") ?: 0L
            PanelScreen(projectId = id, onBack = { nav.popBackStack() })
        }
        composable(Routes.BOM, arguments = listOf(navArgument("projectId") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("projectId") ?: 0L
            BOMScreen(projectId = id, onBack = { nav.popBackStack() })
        }
    }
}
