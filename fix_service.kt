                    val activeActivity = viewModel.activeActivity.collectAsState().value
                    val secondaryActivity = viewModel.secondaryActivity.collectAsState().value
                    val expansionMode = viewModel.expansionMode.collectAsState().value
                    val settings = viewModel.settings.collectAsState().value
                    val activeNotification = viewModel.activeNotification.collectAsState().value

                    LaunchedEffect(activeActivity, activeNotification) {
                        if ((activeActivity !is com.example.model.LiveActivityState.Idle || activeNotification != null) && expansionMode == com.example.model.IslandExpansionMode.HIDDEN) {
                            viewModel.setExpansionMode(com.example.model.IslandExpansionMode.COMPACT)
                        }
                    }
