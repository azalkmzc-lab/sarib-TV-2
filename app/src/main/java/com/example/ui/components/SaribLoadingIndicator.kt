package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SaribCyanAccent
import com.example.ui.theme.SaribElectricBlue
import com.example.ui.theme.SaribTextMuted

@Composable
fun SaribLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    ringColor1: Color = SaribCyanAccent,
    ringColor2: Color = SaribElectricBlue,
    label: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = ringColor1,
            trackColor = ringColor2.copy(alpha = 0.15f),
            strokeWidth = 2.5.dp,
            strokeCap = StrokeCap.Round
        )

        if (!label.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = SaribTextMuted)
            )
        }
    }
}
