package com.simats.netadaptive.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.simats.netadaptive.data.PredictionRepository
import com.simats.netadaptive.data.firebase.FirestoreManager
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.data.repository.AppUsageRepository
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.data.repository.ProfileRepository
import com.simats.netadaptive.ui.auth.LoginActivity
import com.simats.netadaptive.ui.apps.AllAppsScreen
import com.simats.netadaptive.ui.apps.AppDetailScreen
import com.simats.netadaptive.ui.apps.PriorityRankingScreen
import com.simats.netadaptive.ui.analytics.AnalyticsScreen
import com.simats.netadaptive.ui.analytics.TotalDataUsageScreen
import com.simats.netadaptive.ui.analytics.PerAppDataReportScreen
import com.simats.netadaptive.ui.analytics.ForegroundBackgroundScreen
import com.simats.netadaptive.ui.network.LatencyHistoryScreen
import com.simats.netadaptive.ui.network.LivePredictionScreen
import com.simats.netadaptive.ui.network.NetworkOptimizeScreen
import com.simats.netadaptive.ui.network.PacketLossHistoryScreen
import com.simats.netadaptive.ui.network.PredictionLogScreen
import com.simats.netadaptive.ui.network.PredictionConfidenceScreen
import com.simats.netadaptive.ui.settings.ProfileScreen
import com.simats.netadaptive.utils.PermissionUtils
import com.simats.netadaptive.viewmodel.NetworkPredictionViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory
import com.simats.netadaptive.viewmodel.vpn.VpnViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(application, AuthRepository(FirebaseAuth.getInstance())) }
    private val predictionViewModel: NetworkPredictionViewModel by viewModels()
    private val vpnViewModel: VpnViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d("VPN_PERMISSION_GRANTED", "VPN permission granted by user")
            vpnViewModel.startVpn(this)
        } else {
            Log.d("VPN_PERMISSION_DENIED", "VPN permission denied by user")
            vpnViewModel.onPermissionDenied()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.hasPermissions(this)) {
            predictionViewModel.startMonitoring()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions and start prediction service
        if (PermissionUtils.hasPermissions(this)) {
            predictionViewModel.startMonitoring()
        } else {
            PermissionUtils.requestPermissions(this, 1001)
        }

        setContent {
            var currentScreen by remember { mutableStateOf("dashboard") }
            var selectedApp by remember { mutableStateOf<AppUsageData?>(null) }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val user by viewModel.currentUser.observeAsState()
                    val metrics by predictionViewModel.latestMetrics.collectAsState()
                    val prediction by predictionViewModel.latestPrediction.collectAsState()
                    val appsUsage by AppUsageRepository.appsUsage.collectAsState()

                    // Crucial: Re-trigger monitoring if it's not running
                    LaunchedEffect(Unit) {
                        if (PermissionUtils.hasPermissions(this@DashboardActivity)) {
                            Log.d("DashboardActivity", "Permissions granted, starting monitoring...")
                            predictionViewModel.startMonitoring()
                        } else {
                            Log.d("DashboardActivity", "Permissions missing, requesting...")
                            PermissionUtils.requestPermissions(this@DashboardActivity, 1001)
                        }
                    }

                    LaunchedEffect(user) {
                        if (user == null) {
                            navigateToLogin()
                        } else {
                            Log.d("DashboardActivity", "User logged in: ${user?.email}. Triggering Firestore test...")
                            FirestoreManager().testFirestoreConnection()
                            
                            // Synchronize user profile on every dashboard launch
                            FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
                                ProfileRepository.syncProfile(firebaseUser)
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            "dashboard" -> DashboardScreen(
                                user = user,
                                metrics = metrics,
                                prediction = prediction,
                                appsUsage = appsUsage,
                                onProfileClick = { currentScreen = "profile" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "network" -> NetworkOptimizeScreen(
                                onHomeClick = { currentScreen = "dashboard" },
                                onAppsClick = { currentScreen = "apps" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" },
                                onPredictionClick = { currentScreen = "live_prediction" },
                                onLatencyClick = { currentScreen = "latency_history" },
                                onPacketLossClick = { currentScreen = "packet_loss_history" }
                            )
                            "apps" -> AllAppsScreen(
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" },
                                onPriorityRankingClick = { currentScreen = "priority_ranking" },
                                onAppClick = { app ->
                                    selectedApp = app
                                    currentScreen = "app_detail"
                                }
                            )
                            "app_detail" -> {
                                selectedApp?.let { app ->
                                    AppDetailScreen(
                                        app = app,
                                        onBackClick = { currentScreen = "apps" },
                                        onHomeClick = { currentScreen = "dashboard" },
                                        onNetworkClick = { currentScreen = "network" },
                                        onAnalyticsClick = { currentScreen = "analytics" },
                                        onSettingsClick = { currentScreen = "profile" }
                                    )
                                }
                            }
                            "priority_ranking" -> PriorityRankingScreen(
                                onBackClick = { currentScreen = "apps" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "latency_history" -> LatencyHistoryScreen(
                                onBackClick = { currentScreen = "network" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "packet_loss_history" -> PacketLossHistoryScreen(
                                onBackClick = { currentScreen = "network" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "live_prediction" -> LivePredictionScreen(
                                onBackClick = { currentScreen = "network" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" },
                                onConfidenceClick = { currentScreen = "prediction_confidence" },
                                onPredictionLogClick = { currentScreen = "prediction_log" }
                            )
                            "analytics" -> AnalyticsScreen(
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onSettingsClick = { currentScreen = "profile" },
                                onTotalDataUsageClick = { currentScreen = "total_data_usage" },
                                onPerAppDataClick = { currentScreen = "per_app_data" },
                                onForegroundBackgroundClick = { currentScreen = "foreground_background" }
                            )
                            "total_data_usage" -> TotalDataUsageScreen(
                                onBackClick = { currentScreen = "analytics" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "per_app_data" -> PerAppDataReportScreen(
                                onBackClick = { currentScreen = "analytics" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "foreground_background" -> ForegroundBackgroundScreen(
                                onBackClick = { currentScreen = "analytics" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "prediction_log" -> PredictionLogScreen(
                                onBackClick = { currentScreen = "live_prediction" },
                                onHomeClick = { currentScreen = "dashboard" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onSettingsClick = { currentScreen = "profile" }
                            )
                            "prediction_confidence" -> PredictionConfidenceScreen(
                                onBackClick = { currentScreen = "live_prediction" },
                                onNetworkClick = { currentScreen = "network" }
                            )
                            "profile" -> ProfileScreen(
                                onHomeClick = { currentScreen = "dashboard" },
                                onNetworkClick = { currentScreen = "network" },
                                onAppsClick = { currentScreen = "apps" },
                                onAnalyticsClick = { currentScreen = "analytics" },
                                onBackClick = { currentScreen = "dashboard" },
                                onLogoutClick = {
                                    viewModel.logout()
                                },
                                onDeleteAccountClick = {
                                    // Handle account deletion
                                },
                                onStartVpnClick = {
                                    val intent = vpnViewModel.prepareVpn(this@DashboardActivity)
                                    if (intent != null) {
                                        vpnPermissionLauncher.launch(intent)
                                    } else {
                                        vpnViewModel.startVpn(this@DashboardActivity)
                                    }
                                },
                                onStopVpnClick = {
                                    vpnViewModel.stopVpn(this@DashboardActivity)
                                },
                                viewModel = viewModelProvider(),
                                vpnViewModel = vpnViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun viewModelProvider(): com.simats.netadaptive.viewmodel.settings.ProfileViewModel {
        return androidx.lifecycle.viewmodel.compose.viewModel()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
            // Request permissions and start prediction service
        if (PermissionUtils.hasPermissions(this)) {
            predictionViewModel.startMonitoring()
        } else {
            PermissionUtils.requestPermissions(this, 1001)
        }
        }
    }
}
