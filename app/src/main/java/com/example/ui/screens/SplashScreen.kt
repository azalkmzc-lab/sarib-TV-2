package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.tr
import com.example.ui.components.SaribLoadingIndicator
import com.example.ui.theme.SaribCardBg
import com.example.ui.theme.SaribCardBorderSubtle
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribDarkBackground
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribLiveRed
import com.example.ui.theme.SaribTextMuted
import com.example.ui.theme.SaribTextPrimary
import com.example.ui.theme.SaribTextSecondary

@Composable
fun SplashScreen(
    isConnecting: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaribDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Clean, Simple Logo Container
            Surface(
                modifier = Modifier.size(88.dp),
                shape = RoundedCornerShape(24.dp),
                color = SaribCardBg,
                tonalElevation = 2.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sarib_logo),
                    contentDescription = "SARIB TV",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clean App Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SARIB",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SaribTextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TV",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaribCyanAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "منصة البث المباشر والترفيه",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SaribTextSecondary,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Minimalist Loading or Error State
            AnimatedVisibility(
                visible = errorMessage == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SaribCardBg)
                        .border(1.dp, SaribCardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SaribLoadingIndicator(size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tr("connecting"),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SaribTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SaribCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SaribCardBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SaribTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SaribElectricBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("retry_button")
                        ) {
                            Text(
                                text = tr("retry"),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
