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
    object NetworkMap : Screen("network_map")
    object DeviceDetail : Screen("device_detail/{ipAddress}") {
        fun createRoute(ipAddress: String) = "device_detail/$ipAddress"
    }
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
    val deviceRepository = com.example.osint.data.repository.DeviceRepository(context)

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
    val getScannedDevicesUseCase = GetScannedDevicesUseCase(deviceRepository)
    val getDeviceDetailsUseCase = GetDeviceDetailsUseCase(deviceRepository)
    val loadOUIDatabaseUseCase = LoadOUIDatabaseUseCase(deviceRepository)

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            MainScreen(
                onNavigateToUrlScanner = { navController.navigate(Screen.UrlScanner.route) },
                onNavigateToIpScanner = { navController.navigate(Screen.IpScanner.route) },
                onNavigateToHashScanner = { navController.navigate(Screen.HashScanner.route) },
                onNavigateToQRScanner = { navController.navigate(Screen.QRScanner.route) },
                onNavigateToNetworkScanner = { navController.navigate(Screen.NetworkScanner.route) },
                onNavigateToNetworkMap = { navController.navigate(Screen.NetworkMap.route) },
                onNavigateToMetadataInspector = { navController.navigate(Screen.MetadataInspector.route) },
                onNavigateToFeedStatus = { navController.navigate(Screen.FeedStatus.route) }
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
                factory = NetworkScannerViewModelFactory(scanLocalNetworkUseCase, networkScannerRepository, deviceRepository)
            )
            NetworkScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNetworkMap = { navController.navigate(Screen.NetworkMap.route) }
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

        composable(Screen.NetworkMap.route) {
            val viewModel = viewModel<NetworkMapViewModel>(
                factory = NetworkMapViewModelFactory(getScannedDevicesUseCase, loadOUIDatabaseUseCase)
            )
            NetworkMapScreen(
                viewModel = viewModel,
                onNavigateToDeviceDetail = { ipAddress ->
                    navController.navigate(Screen.DeviceDetail.createRoute(ipAddress))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DeviceDetail.route) { backStackEntry ->
            val ipAddress = backStackEntry.arguments?.getString("ipAddress") ?: ""
            val viewModel = viewModel<DeviceDetailViewModel>(
                factory = DeviceDetailViewModelFactory(getDeviceDetailsUseCase, ipAddress)
            )
            DeviceDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
