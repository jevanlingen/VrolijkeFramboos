package vrolijke.framboos.twentytwentythree

import optimizePoints
import vrolijke.framboos.twentytwentythree.http.getGameState
import vrolijke.framboos.twentytwentythree.http.move
import vrolijke.framboos.twentytwentythree.model.Direction
import vrolijke.framboos.twentytwentythree.model.Direction.*
import vrolijke.framboos.twentytwentythree.model.GamePhase
import vrolijke.framboos.twentytwentythree.model.GameState
import vrolijke.framboos.twentytwentythree.model.Point
import vrolijke.framboos.twentytwentythree.model.State
import vrolijke.framboos.twentytwentythree.model.State.Finished

fun getStartingDirection(walls: List<Direction>): Direction {
    if (Left !in walls) {
        return Left
    } else if (Right !in walls) {
        return Right
    } else if (Up !in walls) {
        return Up
    } else if (Down !in walls) {
        return Down
    }

    throw RuntimeException("Not possible!")
}

fun main() {
    startGameWithErrorsCatch();
}

fun startGameWithErrorsCatch() {
    try {
        startGame()
    } catch (e: Exception) {
        startGameWithErrorsCatch()
    }
}

// ugly
var optimizedPoints = mutableListOf<Point>()

tailrec fun startGame() {
    val firstState = getGameState()

    if (firstState.state == State.Playing) {
        if (firstState.gamePhase == GamePhase.Exploration) {
            //println("Start exploration")

            val startingDirection = getStartingDirection(firstState.walls)
            val state = move(firstState.gameId!!, startingDirection)
            val points = moveTillRaspberry(state, startingDirection)

            optimizedPoints = optimizePoints(points)
        } else if (firstState.gamePhase == GamePhase.SpeedRunning) {
            if (optimizedPoints.isNotEmpty()) {
                //println("Speed running with optimized ${optimizedPoints.size} steps");
                for ((index, point) in optimizedPoints.withIndex()) {
                    val nextPoint = optimizedPoints.getOrNull(index + 1) ?: continue

                    if (point.x.compareTo(nextPoint.x) == 0) {
                        //println("LEFT")
                        move(firstState.gameId!!, Left)
                    } else if (point.x.compareTo(nextPoint.x) == 0) {
                        //println("RIGHT")
                        move(firstState.gameId!!, Right)
                    } else if (point.y.compareTo(nextPoint.y) == 0) {
                        //println("UP")
                        move(firstState.gameId!!, Up)
                    } else if (point.y.compareTo(nextPoint.y) == 0) {
                        //println("DOWN")
                        move(firstState.gameId!!, Down)
                    }
                }
                optimizedPoints.clear()
            } else {
                //println("Start speed running (fallback)")

                // fallback
                val startingDirection = getStartingDirection(firstState.walls)
                val state = move(firstState.gameId!!, startingDirection)
                val points = moveTillRaspberry(state, startingDirection)

                //println("Speed running completed with ${points.size} steps");
            }
        }
    }

    Thread.sleep(1000)
    startGame()
}

tailrec fun moveTillRaspberry(state: GameState, direction: Direction, result: MutableList<Point> = mutableListOf()): MutableList<Point> {
    val newDirection = geLeftIfPossible(direction, state)

    val nextState = move(state.gameId!!, newDirection)

    result.add(nextState.position!!)

    return if (nextState.state == Finished) result else {
        moveTillRaspberry(nextState, newDirection, result)
    }
}

inline fun geLeftIfPossible(direction: Direction, state: GameState): Direction {
    val a = goLeftFromAbove(direction)
    if (a !in state.walls) return a
    val b = goForwardFromAbove(direction)
    if (b !in state.walls) return b
    val c = goRightFromAbove(direction)
    if (c !in state.walls) return c
    return goBackwardsFromAbove(direction)
}

inline fun goForwardFromAbove(direction: Direction) = direction
inline fun goRightFromAbove(direction: Direction) =
    when (direction) {
        Left -> Up
        Right -> Down
        Up -> Right
        Down -> Left
        else -> throw NotImplementedError("Scheef werkt nog niet...")
    }
inline fun goLeftFromAbove(direction: Direction) =
    when (direction) {
        Left -> Down
        Right -> Up
        Up -> Left
        Down -> Right
        else -> throw NotImplementedError("Scheef werkt nog niet...")
    }
inline fun goBackwardsFromAbove(direction: Direction) =
    when (direction) {
        Left -> Right
        Right -> Left
        Up -> Down
        Down -> Up
        else -> throw NotImplementedError("Scheef werkt nog niet...")
    }
