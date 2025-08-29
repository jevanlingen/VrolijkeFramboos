package vrolijke.framboos.twentytwentythree.model

import java.util.ArrayList

data class GameState(
    val state: State,
    val gameId: String?, // Actually a UUID, but don't care
    val gamePhase: GamePhase?,
    val position: Point?,
    val nrOfMoves: Int?, // TODO?
    val walls: ArrayList<Direction>,
    val score: GameScore?, // TODO?
)

data class Point(val x: Int, val y: Int)

data class GameScore(val exploration: Int?, val speedRunning: Int?)

data class PlayerRegistrationResponse(val status: RegisterStatus, val id: String, val message: String)

enum class Direction {
    Up, Down, Left, LeftUp, LeftDown, Right, RightUp, RightDown
}

enum class GamePhase {
    Exploration, SpeedRunning, Finished
}

enum class State {
    Waiting, Playing, Finished
}

enum class RegisterStatus {
    Registered, Invalid
}

