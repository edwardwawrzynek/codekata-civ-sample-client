package app

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson

// manages api interactions

// json structs expected to be returned

// response to /api/info
data class InfoResponse(val error: String?, val version: String, val observeRefreshRate: Int, val playerRefreshRate: Int)

// response to all post requests. error will be non null if failure
data class PostResponse(val error: String?)

data class PlayerIndexResponse(val error: String?, val player: Int)

data class BoardResponse(val error: String?, val size: Int, val board: Array<Array<Int>>)

data class CurrentPlayerResponse(val error: String?, val turn: Int)

data class CityResponse(val x: Int, val y: Int)
data class WorkerResponse(val x: Int, val y: Int)
data class ArmyResponse(val x: Int, val y: Int, val hitpoints: Int)

data class CitiesResponse(val error: String?, val cities: Array<Array<CityResponse>>)
data class WorkersResponse(val error: String?, val workers: Array<Array<WorkerResponse>>)
data class ArmiesResponse(val error: String?, val armies: Array<Array<ArmyResponse>>)

data class ResourceResponse(val food: Int, val production: Int, val trade: Int)
data class ResourcesResponse(val error: String?, val resources: Array<ResourceResponse>)

data class PlayerResponse(val name: String, val offense: Double, val defense: Double)
data class PlayersResponse(val error: String?, val players: Array<PlayerResponse>)

class API(val url: String, val key: String) {
    val gson = Gson()

    // GET /api/info
    fun getInfo(): InfoResponse {
        val (_, response, _) = "${url}/api/info".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), InfoResponse::class.java)
    }

    // POST /api/set_name
    fun setName(name: String): PostResponse {
        val (_, response, _) = "${url}/api/set_name".httpPost(
                listOf(Pair("key", key), Pair("name", name))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // GET /api/player_index
    fun getPlayerIndex(): PlayerIndexResponse {
        val (_, response, _) = "${url}/api/player_index".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PlayerIndexResponse::class.java)
    }


    // GET /api/board
    fun getBoard(): BoardResponse {
        val (_, response, _) = "${url}/api/board".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), BoardResponse::class.java)
    }

    // GET /api/current_player
    fun getCurrentPlayer(): CurrentPlayerResponse {
        val (_, response, _) = "${url}/api/current_player".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), CurrentPlayerResponse::class.java)
    }

    // POST /api/end_turn
    fun endTurn(): PostResponse {
        val (_, response, _) = "${url}/api/end_turn".httpPost(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // GET /api/cities
    fun getCities(): CitiesResponse {
        val (_, response, _) = "${url}/api/cities".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), CitiesResponse::class.java)
    }

    // GET /api/armies
    fun getArmies(): ArmiesResponse {
        val (_, response, _) = "${url}/api/armies".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), ArmiesResponse::class.java)
    }

    // GET /api/workers
    fun getWorkers(): WorkersResponse {
        val (_, response, _) = "${url}/api/workers".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), WorkersResponse::class.java)
    }

    // GET /api/resources
    fun getResources(): ResourcesResponse {
        val (_, response, _) = "${url}/api/resources".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), ResourcesResponse::class.java)
    }

    // GET /api/players
    fun getPlayers(): PlayersResponse {
        val (_, response, _) = "${url}/api/players".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PlayersResponse::class.java)
    }

    // POST /api/produce
    fun doProduce(type: ProductionType, location: Pair<Int, Int>): PostResponse {
        val (_, response, _) = "${url}/api/produce".httpPost(
                listOf(Pair("key", key), Pair("type", type.apiIndex), Pair("x", location.first), Pair("y", location.second))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // POST /api/technology
    fun doTechnology(type: TechnologyType): PostResponse {
        val (_, response, _) = "${url}/api/technology".httpPost(
                listOf(Pair("key", key), Pair("type", type.apiIndex))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // POST /api/move_army
    fun doMoveArmy(srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>): PostResponse {
        val (_, response, _) = "${url}/api/move_army".httpPost(
                listOf(
                        Pair("key", key),
                        Pair("srcX", srcPos.first),
                        Pair("srcY", srcPos.second),
                        Pair("dstX", dstPos.first),
                        Pair("dstY", dstPos.second)
                )
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // POST /api/move_worker
    fun doMoveWorker(srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>): PostResponse {
        val (_, response, _) = "${url}/api/move_worker".httpPost(
                listOf(
                        Pair("key", key),
                        Pair("srcX", srcPos.first),
                        Pair("srcY", srcPos.second),
                        Pair("dstX", dstPos.first),
                        Pair("dstY", dstPos.second)
                )
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }
}