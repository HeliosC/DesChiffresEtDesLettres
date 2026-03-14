if (config.devServer) {
    config.devServer.allowedHosts = "all"
    config.devServer.hot = false
    config.devServer.liveReload = false
    config.devServer.webSocketServer = false
}
