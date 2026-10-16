@Composable
private fun CompactIslandPill(
    activeActivity: LiveActivityState,
    settings: IslandSettings,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Cover Art / Icon
        CompactLeftContent(activity = activeActivity, settings = settings, accentColor = accentColor)

        // Center Spacer for Hardware Cutout (No extra dynamic width)
        Spacer(modifier = Modifier.width(4.dp))

        // Right: Edge-aligned Waveform Visualizer
        Box(
            modifier = Modifier
                .wrapContentWidth(Alignment.End)
                .padding(end = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            CompactRightContent(activity = activeActivity, settings = settings, accentColor = accentColor)
        }
    }
}
