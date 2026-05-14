package com.inspiredandroid.betabase.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

enum class Tab { Comps, Gyms }

@Composable
fun BetabaseBottomNav(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navColors = NavigationBarItemDefaults.colors(
        selectedIconColor = BetabaseTheme.colors.ink,
        selectedTextColor = BetabaseTheme.colors.ink,
        unselectedIconColor = BetabaseTheme.colors.inkMuted,
        unselectedTextColor = BetabaseTheme.colors.inkMuted,
        indicatorColor = BetabaseTheme.colors.surfaceMuted,
    )
    NavigationBar(
        modifier = modifier,
        containerColor = BetabaseTheme.colors.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = selected == Tab.Comps,
            onClick = { onSelect(Tab.Comps) },
            icon = { CompsIcon() },
            label = {
                BetaText(
                    text = "COMPS",
                    style = BetabaseTheme.typography.labelSmall,
                )
            },
            colors = navColors,
        )
        NavigationBarItem(
            selected = selected == Tab.Gyms,
            onClick = { onSelect(Tab.Gyms) },
            icon = { GymsIcon() },
            label = {
                BetaText(
                    text = "GYMS",
                    style = BetabaseTheme.typography.labelSmall,
                )
            },
            colors = navColors,
        )
    }
}
