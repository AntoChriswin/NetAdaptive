package com.simats.netadaptive.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.simats.netadaptive.R
import com.simats.netadaptive.core.Resource
import com.simats.netadaptive.data.repository.AuthRepository
import com.simats.netadaptive.ui.apps.AllAppsScreen
import com.simats.netadaptive.ui.apps.AppDetailScreen
import com.simats.netadaptive.ui.apps.PriorityRankingScreen
import com.simats.netadaptive.ui.analytics.AnalyticsScreen
import com.simats.netadaptive.ui.analytics.TotalDataUsageScreen
import com.simats.netadaptive.ui.analytics.PerAppDataReportScreen
import com.simats.netadaptive.ui.analytics.ForegroundBackgroundScreen
import com.simats.netadaptive.data.model.AppUsageData
import com.simats.netadaptive.ui.dashboard.DashboardActivity
import com.simats.netadaptive.ui.dashboard.DashboardScreen
import com.simats.netadaptive.ui.network.LatencyHistoryScreen
import com.simats.netadaptive.ui.network.LivePredictionScreen
import com.simats.netadaptive.ui.network.NetworkOptimizeScreen
import com.simats.netadaptive.ui.network.PacketLossHistoryScreen
import com.simats.netadaptive.ui.network.PredictionLogScreen
import com.simats.netadaptive.ui.network.PredictionConfidenceScreen
import com.simats.netadaptive.ui.settings.ProfileScreen
import com.simats.netadaptive.viewmodel.auth.AuthViewModel
import com.simats.netadaptive.viewmodel.auth.AuthViewModelFactory
import com.simats.netadaptive.viewmodel.NetworkPredictionViewModel
import com.simats.netadaptive.utils.PermissionUtils
import com.simats.netadaptive.viewmodel.vpn.VpnViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashScreenActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(application, AuthRepository(FirebaseAuth.getInstance())) }
    private val predictionViewModel: NetworkPredictionViewModel by viewModels()
    private val vpnViewModel: VpnViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                viewModel.signInWithGoogle(credential)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            android.util.Log.d("VPN_PERMISSION_GRANTED", "VPN permission granted by user")
            vpnViewModel.startVpn(this)
        } else {
            android.util.Log.d("VPN_PERMISSION_DENIED", "VPN permission denied by user")
            vpnViewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LaunchedEffect(Unit) {
                // Initialize in background to avoid blocking initial UI frames
                val hasPerms = PermissionUtils.hasPermissions(this@SplashScreenActivity)
                if (hasPerms) {
                    predictionViewModel.startMonitoring()
                } else {
                    PermissionUtils.requestPermissions(this@SplashScreenActivity, 1001)
                }
                setupGoogleSignIn()
            }

            var currentScreen by remember { mutableStateOf("splash") }
            var selectedApp by remember { mutableStateOf<AppUsageData?>(null) }
            var resetEmail by remember { mutableStateOf("") }
            val signInState by viewModel.authActionState.observeAsState()

            LaunchedEffect(signInState) {
                if (signInState is Resource.Success<*>) {
                    currentScreen = "vpn_permission"
                } else if (signInState is Resource.Error) {
                    Toast.makeText(this@SplashScreenActivity, (signInState as Resource.Error).message, Toast.LENGTH_SHORT).show()
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            "splash" -> SplashScreen(onLoadingComplete = {
                                if (viewModel.currentUser.value != null) {
                                    currentScreen = "dashboard"
                                } else {
                                    currentScreen = "predict"
                                }
                            })
                            "predict" -> OnboardingPredictScreen(
                                onNextClick = { currentScreen = "optimize" },
                                onSkipClick = { currentScreen = "monitor" }
                            )
                            "optimize" -> OnboardingOptimizeScreen(
                                onNextClick = { currentScreen = "monitor" },
                                onSkipClick = { currentScreen = "monitor" }
                            )
                            "monitor" -> OnboardingMonitorScreen(
                                onGetStartedClick = { currentScreen = "signup" },
                                onSkipClick = { currentScreen = "signup" }
                            )
                            "signup" -> SignUpScreen(
                                onSignUpClick = { email, password -> viewModel.signUpWithEmail(email, password) },
                                onLoginClick = { currentScreen = "login" },
                                onGoogleSignUpClick = { signIn() }
                            )
                            "login" -> LoginScreen(
                                onLoginClick = { email, password -> viewModel.signInWithEmail(email, password) },
                                onSignUpClick = { currentScreen = "signup" },
                                onForgotPasswordClick = { currentScreen = "forgot_password" },
                                onGoogleLoginClick = { signIn() }
                            )
                            "vpn_permission" -> VPNPermissionScreen(
                                onBackClick = { currentScreen = "login" },
                                onEnableClick = { currentScreen = "notification_permission" },
                                onWhyNeedClick = { /* Handle Why Need */ }
                            )
                            "notification_permission" -> NotificationPermissionScreen(
                                onAllowClick = { currentScreen = "setup_complete" },
                                onNotNowClick = { currentScreen = "setup_complete" },
                                onQuietHoursClick = { /* Handle Quiet Hours */ }
                            )
                            "setup_complete" -> SetupCompleteScreen(
                                onStartOptimizingClick = { currentScreen = "dashboard" }
                            )
                            "dashboard" -> DashboardScreen(
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
                                    currentScreen = "login"
                                },
                                onDeleteAccountClick = { /* Handle delete logic if needed */ },
                                onStartVpnClick = {
                                    val intent = vpnViewModel.prepareVpn(this@SplashScreenActivity)
                                    if (intent != null) {
                                        vpnPermissionLauncher.launch(intent)
                                    } else {
                                        vpnViewModel.startVpn(this@SplashScreenActivity)
                                    }
                                },
                                onStopVpnClick = {
                                    vpnViewModel.stopVpn(this@SplashScreenActivity)
                                },
                                vpnViewModel = vpnViewModel
                            )
                            "forgot_password" -> ForgotPasswordScreen(
                                onBackClick = { currentScreen = "login" },
                                onResendClick = { /* Handle Resend */ },
                                onSendClick = { email ->
                                    resetEmail = email
                                    viewModel.resetPassword(email)
                                    currentScreen = "forgot_password_success"
                                }
                            )
                            "forgot_password_success" -> ForgetPassSuccessScreen(
                                email = resetEmail,
                                onBackToLogin = { currentScreen = "login" },
                                onOpenEmailApp = {
                                    val intent = Intent(Intent.ACTION_MAIN)
                                    intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    try {
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        val gmailIntent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                                        if (gmailIntent != null) {
                                            startActivity(gmailIntent)
                                        } else {
                                            Toast.makeText(this@SplashScreenActivity, "No email app found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onResendClick = {
                                    if (resetEmail.isNotEmpty()) {
                                        viewModel.resetPassword(resetEmail)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && PermissionUtils.hasPermissions(this)) {
            predictionViewModel.startMonitoring()
        }
    }
}

@Composable
fun SplashScreen(onLoadingComplete: () -> Unit = {}) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 4000),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) {
        progress = 1f
        delay(4200) // Slightly longer than animation to let it settle
        onLoadingComplete()
    }

    // Root container with dark blue background (#063A5D)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF063A5D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon Container (Rounded square with shadow)
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF063A5D).copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFE9237) // primary-container color
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "NetAdaptive Icon",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFD150) // Icon color
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Name
            Text(
                text = "NetAdaptive",
                color = Color(0xFFF4F5F7),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif, // Should be Space Grotesk
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Predict. Optimize. Adapt.",
                color = Color(0xFFA8DBDE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif // Should be Manrope
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Loading Indicator (Animated)
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2364A0).copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(Color(0xFFFEB913))
                        .clip(CircleShape)
                )
            }
        }

        // Version Label
        Text(
            text = "v 1.0.0",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            color = Color(0xFFA8DBDE).copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF063A5D)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
