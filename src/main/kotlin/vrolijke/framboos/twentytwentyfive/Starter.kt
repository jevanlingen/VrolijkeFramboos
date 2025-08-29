package vrolijke.framboos.twentytwentyfive

fun main() {
    startGameLoop();
}

fun startGameLoop() {
    while (true) {
        try {
            startGame()
        } catch (e: Exception) {
            println("Caught exception: ${e.message}")
        }
    }
}

fun startGame() {
    println("Starting game")
    Thread.sleep(2000) // simulate processing
}
