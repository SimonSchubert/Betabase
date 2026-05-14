package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.data.Gym
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
actual fun GymMap(
    gyms: List<Gym>,
    selectedGym: Gym?,
    onGymSelected: (Gym?) -> Unit,
    modifier: Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(BetabaseTheme.colors.background)) {
        if (gyms.isEmpty()) {
            BetaText(
                text = "No gyms loaded",
                modifier = Modifier.align(Alignment.Center),
                style = BetabaseTheme.typography.bodyMedium,
                color = BetabaseTheme.colors.inkMuted,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(gyms, key = { it.id }) { gym ->
                    BetaCard(
                        bordered = true,
                        onClick = { onGymSelected(gym) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BetaText(
                                text = gym.name,
                                style = BetabaseTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(4.dp))
                            BetaText(
                                text = gym.address,
                                style = BetabaseTheme.typography.bodySmall,
                                color = BetabaseTheme.colors.inkMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}
