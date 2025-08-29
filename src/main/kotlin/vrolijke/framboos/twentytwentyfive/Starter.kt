package vrolijke.framboos.twentytwentyfive

import vrolijke.framboos.twentytwentyfive.http.*
import vrolijke.framboos.twentytwentyfive.model.*
import java.util.UUID

fun main() {
    val playerName = "Jacob-Just-Claude"
    val playerPassword = "111111111111111111"
    val emojiAlias = "robot"

    //println("Registering player: $playerName")
    val registrationResponse = registerPlayer(playerName, playerPassword, emojiAlias)

    if (registrationResponse.status == PlayerStatus.Registered && registrationResponse.id != null) {
        //println("Successfully registered with ID: ${registrationResponse.id}")
        playerId = registrationResponse.id
        password = playerPassword

        startGameLoop(registrationResponse.id, playerPassword)
    } else {
        //println("Failed to register: ${registrationResponse.message}")
    }
}

// Track visited nodes to avoid ping-ponging
val visitedNodes = mutableSetOf<String>()
val recentPath = mutableListOf<String>() // Keep track of recent path to prevent cycles
const val RECENT_PATH_SIZE = 5 // Don't go back to last 5 nodes

fun startGameLoop(pid: UUID, pass: String) {
    //println("Starting game loop for player: $pid")

    while (true) {
        try {
            val gameState = getCurrentGameState(pid, pass)
            //println("Game state: ${gameState.state}, Location: ${gameState.location}, Points: ${gameState.points}")

            when (gameState.state) {
                GameState.Waiting -> {
                    //println("Waiting for game to start...")
                    // Clear visited nodes when waiting for a new game
                    visitedNodes.clear()
                    recentPath.clear()
                    Thread.sleep(2000)
                }

                GameState.Playing -> {
                    // Track current location in recent path
                    gameState.location?.let {
                        // Only add if it's different from the last location
                        if (recentPath.isEmpty() || recentPath.last() != it) {
                            recentPath.add(it)
                            visitedNodes.add(it)

                            // Keep recent path limited to prevent memory issues
                            if (recentPath.size > RECENT_PATH_SIZE) {
                                recentPath.removeAt(0)
                            }

                            //println("Visited nodes: ${visitedNodes.size}, Recent path: ${recentPath.joinToString(" -> ")}")
                        }
                    }
                    playGame(pid, pass, gameState)
                }
            }
        } catch (e: Exception) {
            //println("Error in game loop: ${e.message}")
            e.printStackTrace()
            Thread.sleep(5000)
        }
    }
}

fun playGame(pid: UUID, pass: String, gameState: GameStateDTO) {
    //println("Playing game at location: ${gameState.location}, Action: ${gameState.action}, Points: ${gameState.points}")

    when (gameState.action) {
        GameAction.Idle -> {
            // Get network topology to understand where we can move
            val network = getCurrentNetwork(pid, pass)

            // Print network topology on first run (when points are 0)
            if (gameState.points == 0) {
                printNetworkTopology(network, gameState.location)
            }

            // Scan for data sources if available (do this frequently to find new sources)
            // Scan when: no known data sources OR every 10 visited nodes OR special action available
            val shouldScan = gameState.specialActions?.contains(SpecialAction.ScanNetwork) == true &&
                    (gameState.dataSources.isNullOrEmpty() || visitedNodes.size % 10 == 0)

            if (shouldScan) {
                //println("Scanning network for data sources...")
                val scanResult = scanNetwork(pid, pass)
                //println("Found ${scanResult.dataSources?.size ?: 0} data sources: ${scanResult.dataSources?.keys}")

                // If scan found data sources, update our knowledge
                if (!scanResult.dataSources.isNullOrEmpty()) {
                    // Continue with the scan result's data sources
                    gameState.dataSources?.plus(scanResult.dataSources)
                }
                return // Let next iteration handle the new state
            }

            // Check if there's a data source at current location
            if (!gameState.dataSources.isNullOrEmpty()) {
                val dataSource = gameState.dataSources[gameState.location]
                if (dataSource != null) {
                    //println("Found data source at ${gameState.location}: ${dataSource.dataPoints} points")
                    // Start downloading - just call once, let the game handle the download state
                    download(pid, pass)
                    return
                }
            }

            // Consider placing honeypot if we have the ability and good location
            if (gameState.specialActions?.contains(SpecialAction.PlaceHoneypot) == true) {
                // Place honeypot at high-traffic nodes (nodes with many edges)
                val edgeCount = network.edges.count { it.from == gameState.location || it.to == gameState.location }
                if (edgeCount >= 3 && gameState.placedHoneypot == null) {
                    //println("Placing honeypot at high-traffic location with $edgeCount connections")
                    placeHoneypot(pid, pass)
                    return
                }
            }

            // Find next location to move to
            val nextLocation = findNextLocation(gameState.location, network, gameState.dataSources)
            if (nextLocation != null) {
                //println("Moving from ${gameState.location} to $nextLocation")
                move(pid, pass, nextLocation)
            } else {
                //println("No valid moves available, waiting...")
                Thread.sleep(1000)
            }
        }

        GameAction.Move -> {
            //println("Currently moving...")
            // Wait for movement to complete by polling game state
            waitForActionToComplete(pid, pass, GameAction.Move)
        }

        GameAction.Download -> {
            //println("Currently downloading...")
            // Keep calling download while in download state
            val result = download(pid, pass)
            //println("Download in progress - Current points: ${result.points}")
            Thread.sleep(100) // Small delay between downloads
        }

        GameAction.ScanNetwork -> {
            //println("Currently scanning network...")
            // Wait for scan to complete
            waitForActionToComplete(pid, pass, GameAction.ScanNetwork)
        }

        GameAction.PlaceHoneypot -> {
            //println("Placing honeypot...")
            // Wait for honeypot placement to complete
            waitForActionToComplete(pid, pass, GameAction.PlaceHoneypot)
        }

        else -> {
            Thread.sleep(500)
        }
    }
}

fun printNetworkTopology(network: NetworkDTO, currentLocation: String?) {
    //println("\n=== NETWORK TOPOLOGY ===")
    //println("Total nodes: ${network.nodes.size}")
    //println("Total edges: ${network.edges.size}")
    //println("Current location: $currentLocation")

    //println("\nNodes:")
    network.nodes.take(10).forEach {
        //println("  - $it ${if (it == currentLocation) "(YOU ARE HERE)" else ""}")
    }
    if (network.nodes.size > 10) {
        //println("  ... and ${network.nodes.size - 10} more nodes")
    }

    //println("\nEdges from current location:")
    val edgesFromCurrent = network.edges.filter {
        it.from == currentLocation || it.to == currentLocation
    }
    edgesFromCurrent.forEach {
        if (it.from == currentLocation) {
            //println("  -> ${it.to} (latency: ${it.latency})")
        } else {
            //println("  <- ${it.from} (latency: ${it.latency})")
        }
    }
    //println("========================\n")
}

fun findNextLocation(currentLocation: String?, network: NetworkDTO, dataSources: Map<String, DataSourceDTO>?): String? {
    if (currentLocation == null) return null

    // Find all edges from current location (edges might be bidirectional)
    val allPossibleMoves = network.edges
        .filter { it.from == currentLocation || it.to == currentLocation }
        .map {
            // Create a normalized edge where we can move to the other node
            if (it.from == currentLocation) {
                it
            } else {
                // Reverse the edge if we're at the 'to' node
                EdgeDTO(from = currentLocation, to = it.from, latency = it.latency)
            }
        }

    // STRICT: Filter out ANY node in recent path to prevent cycles
    val movesNotInRecentPath = allPossibleMoves
        .filter { !recentPath.contains(it.to) }

    // First try moves that are not in recent path
    val possibleMoves = if (movesNotInRecentPath.isNotEmpty()) {
        //println("Found ${movesNotInRecentPath.size} moves outside recent path")
        movesNotInRecentPath
    } else {
        // If ALL moves are in recent path, we're stuck - only allow moves NOT in last 2 positions
        //println("WARNING: All adjacent nodes are in recent path, trying to avoid immediate backtrack")
        val lastTwo = recentPath.takeLast(2).toSet()
        val emergencyMoves = allPossibleMoves.filter { edge ->
            !lastTwo.contains(edge.to)
        }

        emergencyMoves.ifEmpty {
            //println("CRITICAL: Cannot move without immediate backtracking - allowing oldest visited node")
            // Last resort: move to the oldest node in recent path
            allPossibleMoves.filter { edge ->
                edge.to != recentPath.lastOrNull() // Never go to immediately previous node
            }
        }
    }

    if (possibleMoves.isEmpty()) {
        //println("ERROR: No valid moves available - completely stuck!")
        //println("Current location: $currentLocation")
        //println("Recent path: ${recentPath.joinToString(" -> ")}")
        //println("All possible destinations: ${allPossibleMoves.map { it.to }}")
        return null
    }

    //println("Possible moves from $currentLocation:")
    possibleMoves.forEach {
        val inPath = if (recentPath.contains(it.to)) " (IN RECENT PATH!)" else ""
        val visited = if (visitedNodes.contains(it.to)) " (visited)" else " (new)"
        //println("  - ${it.to} (latency: ${it.latency})$visited$inPath")
    }

    // Prioritize data sources based on efficiency (blockSize/latency ratio)
    if (!dataSources.isNullOrEmpty()) {
        // Calculate efficiency score for each data source (higher is better)
        val dataSourcesWithScore = possibleMoves
            .filter { edge -> dataSources.containsKey(edge.to) }
            .map { edge ->
                val ds = dataSources[edge.to]!!
                // Efficiency = blockSize / latency * dataPoints
                // We want high blockSize, low latency, and high dataPoints
                val efficiency = (ds.blockSize.toDouble() / ds.latency.toDouble()) * ds.dataPoints
                Triple(edge, ds, efficiency)
            }
            .sortedByDescending { it.third } // Sort by efficiency score
        
        // First try data sources NOT in recent path, sorted by efficiency
        val bestUnvisitedDataSource = dataSourcesWithScore
            .find { (edge, _, _) -> !recentPath.contains(edge.to) }
        
        if (bestUnvisitedDataSource != null) {
            val (edge, ds, efficiency) = bestUnvisitedDataSource
            println("Found efficient data source: ${edge.to} (efficiency: ${String.format("%.2f", efficiency)}, blockSize: ${ds.blockSize}, latency: ${ds.latency}ms, points: ${ds.dataPoints})")
            return edge.to
        }

        // If desperate, go to most efficient data source even if in recent path (but not last node)
        val bestAnyDataSource = dataSourcesWithScore.firstOrNull()
        if (bestAnyDataSource != null) {
            val (edge, ds, efficiency) = bestAnyDataSource
            println("Going to visited data source: ${edge.to} (efficiency: ${String.format("%.2f", efficiency)})")
            return edge.to
        }
    }

    // Prefer completely unvisited nodes
    val completelyNew = possibleMoves
        .filter { !visitedNodes.contains(it.to) && !recentPath.contains(it.to) }
        .minByOrNull { it.latency }

    if (completelyNew != null) {
        //println("Choosing completely new node: ${completelyNew.to}")
        return completelyNew.to
    }

    // Then try visited but not in recent path
    val visitedButNotRecent = possibleMoves
        .filter { !recentPath.contains(it.to) }
        .minByOrNull { it.latency }

    if (visitedButNotRecent != null) {
        //println("Choosing visited but not recent: ${visitedButNotRecent.to}")
        return visitedButNotRecent.to
    }

    // Last resort: pick anything with lowest latency
    val bestMove = possibleMoves.minByOrNull { it.latency }
    if (bestMove != null) {
        //println("FORCED BACKTRACK to: ${bestMove.to}")
    }
    return bestMove?.to
}

fun waitForActionToComplete(pid: UUID, pass: String, currentAction: GameAction) {
    var stillInAction = true
    var pollCount = 0

    while (stillInAction) {
        Thread.sleep(200) // Poll every 200ms
        pollCount++

        try {
            val state = getCurrentGameState(pid, pass)

            if (state.action != currentAction) {
                //println("Action complete after $pollCount polls. New state: ${state.action}")
                stillInAction = false

                // If we've arrived at a location with data, start downloading immediately
                if (state.action == GameAction.Idle && !state.dataSources.isNullOrEmpty()) {
                    val dataSource = state.dataSources[state.location]
                    if (dataSource != null) {
                        //println("Arrived at data source! Starting download...")
                        downloadData(pid, pass)
                    }
                }
            } else if (pollCount % 10 == 0) {
                // Log progress every 10 polls (2 seconds)
                //println("Still ${currentAction}... (poll #$pollCount)")
            }
        } catch (e: Exception) {
            //println("Error polling state: ${e.message}")
            stillInAction = false
        }
    }
}

fun downloadData(pid: UUID, pass: String) {
    //println("Starting download at current location...")
    var downloading = true
    var downloadCount = 0

    while (downloading) {
        try {
            val downloadState = download(pid, pass)
            downloadCount++

            //println("Download #$downloadCount - Current points: ${downloadState.points}")

            // Check if we're still downloading or if the data source is empty
            if (downloadState.action != GameAction.Download) {
                //println("Download complete or data source empty")
                downloading = false
            } else if (downloadState.dataSources?.get(downloadState.location) == null) {
                //println("No more data at this location")
                downloading = false
            }

            Thread.sleep(100) // Small delay between download attempts
        } catch (e: Exception) {
            //println("Download error: ${e.message}")
            downloading = false
        }
    }

    //println("Downloaded $downloadCount blocks")
}
