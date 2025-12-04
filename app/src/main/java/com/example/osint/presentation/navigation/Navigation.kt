package com.example.osint.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.osint.data.db.ThreatDatabase
import com.example.osint.data.feeds.FeedDownloader
import com.example.osint.data.repository.ThreatRepository
import com.example.osint.domain.usecase.*
import com.example.osint.presentation.screens.*
import com.example.osint.presentation.viewmodel.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object UrlScanner : Screen("url_scanner")
    object IpScanner : Screen("ip_scanner")
    object HashScanner : Screen("hash_scanner")
    object QRScanner : Screen("qr_scanner")
    object FeedStatus : Screen("feed_status")
    object NetworkScanner : Screen("network_scanner")
    object MetadataInspector : Screen("metadata_inspector")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize dependencies
    val database = ThreatDatabase.getDatabase(context)
    val repository = ThreatRepository(database)
    val feedDownloader = FeedDownloader(context)
    val networkScannerRepository = com.example.osint.data.repository.NetworkScannerRepository(context)
    val metadataRepository = com.example.osint.data.repository.MetadataRepository(context)

    // Initialize use cases
    val computeRiskScoreUseCase = ComputeRiskScoreUseCase()
    val scanUrlUseCase = ScanUrlUseCase(repository, computeRiskScoreUseCase)
    val scanIpUseCase = ScanIpUseCase(repository, computeRiskScoreUseCase)
    val scanHashUseCase = ScanHashUseCase(repository, computeRiskScoreUseCase)
    val parseFeedUseCase = ParseFeedUseCase()
    val refreshFeedsUseCase = RefreshFeedsUseCase(feedDownloader, repository, parseFeedUseCase)
    val getFeedStatusUseCase = GetFeedStatusUseCase(repository)
    val probeHostUseCase = ProbeHostUseCase(networkScannerRepository)
    val scanLocalNetworkUseCase = ScanLocalNetworkUseCase(networkScannerRepository, probeHostUseCase)
    val parseImageMetadataUseCase = ParseImageMetadataUseCase(metadataRepository)
    val stripImageExifUseCase = StripImageExifUseCase(metadataRepository)

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToUrlScanner = { navController.navigate(Screen.UrlScanner.route) },
                onNavigateToIpScanner = { navController.navigate(Screen.IpScanner.route) },
                onNavigateToHashScanner = { navController.navigate(Screen.HashScanner.route) },
                onNavigateToQRScanner = { navController.navigate(Screen.QRScanner.route) },
                onNavigateToFeedStatus = { navController.navigate(Screen.FeedStatus.route) },
                onNavigateToNetworkScanner = { navController.navigate(Screen.NetworkScanner.route) },
                onNavigateToMetadataInspector = { navController.navigate(Screen.MetadataInspector.route) }
            )
        }

        composable(Screen.UrlScanner.route) {
            val viewModel = viewModel<UrlScannerViewModel>(
                factory = UrlScannerViewModelFactory(scanUrlUseCase)
            )
            UrlScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.IpScanner.route) {
            val viewModel = viewModel<IpScannerViewModel>(
                factory = IpScannerViewModelFactory(scanIpUseCase)
            )
            IpScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HashScanner.route) {
            val viewModel = viewModel<HashScannerViewModel>(
                factory = HashScannerViewModelFactory(scanHashUseCase)
            )
            HashScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QRScanner.route) {
            val viewModel = viewModel<QRScannerViewModel>(
                factory = QRScannerViewModelFactory(scanUrlUseCase)
            )
            QRScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FeedStatus.route) {
            val viewModel = viewModel<FeedStatusViewModel>(
                factory = FeedStatusViewModelFactory(getFeedStatusUseCase, refreshFeedsUseCase)
            )
            FeedStatusScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NetworkScanner.route) {
            val viewModel = viewModel<NetworkScannerViewModel>(
                factory = NetworkScannerViewModelFactory(scanLocalNetworkUseCase, networkScannerRepository)
            )
            NetworkScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MetadataInspector.route) {
            val viewModel = viewModel<MetadataInspectorViewModel>(
                factory = MetadataInspectorViewModelFactory(parseImageMetadataUseCase, stripImageExifUseCase)
            )
            MetadataInspectorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
