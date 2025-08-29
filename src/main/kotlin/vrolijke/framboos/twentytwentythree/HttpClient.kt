package vrolijke.framboos.twentytwentythree.http

import com.google.gson.Gson
import vrolijke.framboos.twentytwentythree.model.Direction
import vrolijke.framboos.twentytwentythree.model.GameState
import vrolijke.framboos.twentytwentythree.model.PlayerRegistrationResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val client: HttpClient = HttpClient.newBuilder().build()
val uri = "https://raspberry-runaround-yjiibwucma-ez.a.run.app/raspberry-runaround"

// TODO fallback with user creation
var playerId = "5440de58-8d5d-4f07-801e-fcda8cce0c6c"
const val password = "tree"

inline fun registerUser() {
    val body = """
        {
          "name": "jacob",
          "password": "$password",
          "emojiAlias": "star-struck"
        }
    """.trimIndent()

    val request = HttpRequest.newBuilder()
        .uri(URI.create("$uri/player"))
        .header("accept" , "application/json")
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    val user = parseUser(response)
    playerId = user.id
}

inline fun getGameState(): GameState {
    registerUser()

    val response = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create("$uri/player/$playerId"))
            .header("Authorization", "Bearer $password")
            .build(),
        HttpResponse.BodyHandlers.ofString()
    )

    return parseGameState(response)
}

inline fun move(gameId: String, direction: Direction): GameState {
    val body = """
        {
          "gameId": "$gameId",
          "playerId": "$playerId",
          "direction": "$direction"
        }
    """.trimIndent()

    val request = HttpRequest.newBuilder()
        .uri(URI.create("$uri/game/move"))
        .header("accept" , "application/json")
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer $password")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return parseGameState(response)
}

/*inline fun parseGameState(response: HttpResponse<String>): GameState {
    val x = Gson().fromJson(response.body(), GameState::class.java)

    //println(x)


    return x;
}*/

inline fun parseUser(response: HttpResponse<String>) = Gson().fromJson(response.body(), PlayerRegistrationResponse::class.java)
inline fun parseGameState(response: HttpResponse<String>) = Gson().fromJson(response.body(), GameState::class.java)

