package com.inspiredandroid.betabase.ui.screens

// maplibre-compose 0.13.0 does not yet implement programmatic sources/layers
// on desktop JVM (see roadmap "Desktop Parity"). Mobile actuals return true.
internal expect val supportsMaplibreProgrammaticLayers: Boolean
