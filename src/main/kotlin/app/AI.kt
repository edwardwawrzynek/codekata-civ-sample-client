package app

import kotlin.math.floor

// EDIT THIS FILE

data class Treasury(var production: Int, var food: Int, var trade: Int)

enum class Direction { Up, Down, Left, Right }

fun doMoveUnit(move: (srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>) -> Boolean, direction: Direction, unit: BoardUnit): Boolean {
    val srcPos = unit.position
    val dst = when (direction) {
        Direction.Up -> Pair(srcPos.first, srcPos.second - 1)
        Direction.Down -> Pair(srcPos.first, srcPos.second + 1)
        Direction.Right -> Pair(srcPos.first + 1, srcPos.second)
        Direction.Left -> Pair(srcPos.first - 1, srcPos.second)
    }
    if (!unit.isValidMove(dst) || dst == srcPos) {
        return false
    }

    move(unit.position, dst)
    return true
}

class SmartWorker(val master: AI, position: Pair<Int, Int>): BoardUnit(position) {
    val parent = position
    var dead = false

    fun moveTo(doMoveWorker: (srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>) -> Boolean, target: Pair<Int, Int>) {
        if (doMoveWorker(position, target)) position = target
        else if (distanceTo(target) <= 1) dead = true
    }

    fun resolve(doMoveWorker: (srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>) -> Boolean, workers: Iterable<Worker>) {
        val dummy = City(parent)

        // move away from workers
        if (master.workers.any { it != this && it.position == position } ) {
            val moves = master.getSurroundingTiles(this, master.cachedTerrainMap, 1).filter { move -> !master.workers.any { worker -> worker.position == move } && dummy.distanceTo(move) in listOf(1, 2) }
            if (moves.size > 0) {
                val bestMove = moves.maxBy { master.scoreTile(master.cachedTerrainMap, it) } ?: moves.first()
                doMoveWorker(position, bestMove)
                position = bestMove.copy()
                return
            }
        }

        // move away from city
        if (dummy.distanceTo(this) == 1) {
            val moves = master.getSurroundingTiles(this, master.cachedTerrainMap, 1).filter { move -> !master.workers.any { worker -> worker.position == move } && dummy.distanceTo(move) > 1 }
            if (moves.size > 0) {
                val bestMove = moves.maxBy { master.scoreTile(master.cachedTerrainMap, it) } ?: moves.first()
                doMoveWorker(position, bestMove)
                position = bestMove.copy()
                return
            }
        }

        if (parent == position) {
            val moves = master.getSurroundingTiles(this, master.cachedTerrainMap, 1).filter { move -> !master.workers.any { worker -> worker.position == move } && dummy.distanceTo(move) == 1 }
            if (moves.size > 0) {
                val bestMove = moves.maxBy { master.scoreTile(master.cachedTerrainMap, it) } ?: moves.first()
                doMoveWorker(position, bestMove)
                position = bestMove.copy()
                return
            }
        }


    }
}

class AI() {
    // Change the name that shows up on the graphical display
    // optional, but HIGHLY RECOMMENDED
    val treasury = Treasury(0, 0, 0)
    var lastProduction = 0
    var productionSavings = 0
    val unreplicatedCities = mutableSetOf<Pair<Int, Int>>()
    var workers = mutableSetOf<SmartWorker>()
    var cachedTerrainMap = GameMap(32, Array(32) { Array(32) { TileType.Fogged } })

    val newCities = mutableListOf<Pair<Int, Int>>()


    fun getName() = "Westfail"


    fun doResearch(doTechnology: (type: TechnologyType) -> Boolean, us: Player, them: List<Player>) {
        var maxOffensiveTech = them.map({player -> player.offensiveStrength}).max() ?: 1.0
        while (us.defensiveStrength < maxOffensiveTech && treasury.trade >= 20) {
            if (doTechnology(TechnologyType.Defense)) treasury.trade -= 20
        }
        while (treasury.trade >= 20) {
            if (doTechnology(TechnologyType.Offense)) treasury.trade -= 20
        }
    }

    fun getSurroundingTiles(unit: BoardObject, map: GameMap, distance: Int = 2): Set<Pair<Int, Int>> {
        val tiles = mutableSetOf<Pair<Int, Int>>()
        for (x in unit.position.first - distance..unit.position.first + distance) {
            if (x < 0 || x >= map.size) continue
            for (y in unit.position.second - distance..unit.position.second + distance) {
                if (y < 0 || y >= map.size) continue

                val tile = Pair(x, y)
                if (unit.distanceTo(tile) <= distance) {
                    tiles.add(tile)
                }
            }
        }

        return tiles.toSet()
    }

    fun getSurroundingTiles(pos: Pair<Int, Int>, map: GameMap, distance: Int = 2): Set<Pair<Int, Int>> {
        val dummy = Worker(pos)
        return getSurroundingTiles(dummy, map, distance)
    }

    fun productionAt(pos: Pair<Int, Int>, map: GameMap): Int {
        return map.getHarvestAmounts(pos)?.get(ResourceType.Production) ?: 0
    }

    fun productionAt(unit: BoardObject, map: GameMap): Int {
        return map.getHarvestAmounts(unit.position)?.get(ResourceType.Production) ?: 0
    }

    fun foodAt(pos: Pair<Int, Int>, map: GameMap): Int {
        return map.getHarvestAmounts(pos)?.get(ResourceType.Food) ?: 0
    }

    fun foodAt(unit: BoardObject, map: GameMap): Int {
        return map.getHarvestAmounts(unit.position)?.get(ResourceType.Food) ?: 0
    }

    fun tradeAt(pos: Pair<Int, Int>, map: GameMap): Int {
        return map.getHarvestAmounts(pos)?.get(ResourceType.Trade) ?: 0
    }

    fun tradeAt(unit: BoardObject, map: GameMap): Int {
        return map.getHarvestAmounts(unit.position)?.get(ResourceType.Trade) ?: 0
    }

    fun getBestCitySite(map: GameMap, us: Player, them: List<Player>): Pair<Int, Int> {
        // get all cities
        var allCities = MutableList(newCities.size) { index -> newCities[index] }
        for (player in them) {
            for (city in player.cities) {
                allCities.add(city.position)
            }
        }
        for (city in us.cities) {
            allCities.add(city.position)
        }

        var bestSite = Pair(0, 0)
        var bestScore = -100.0
        for (x in 0 until map.size) {
            for (y in 0 until map.size) {
                if (map.contents[x][y] == TileType.Fogged) continue

                val site = Pair(x, y)
                val siteTiles = getSurroundingTiles(site, map)
                var taken = false
                var score = 0.0

                val takenTiles = mutableSetOf<Pair<Int, Int>>()
                for (city in allCities) {
                    if (city == site) {
                        taken = true
                        break
                    }

                    val dummy = City(city)

                    for (tile in siteTiles) {
                        if (dummy.distanceTo(tile) <= 2) takenTiles.add(tile)
                    }
                }
                if (taken) continue

                for (tile in siteTiles) {
                    if (tile in takenTiles) continue
                    score += productionAt(tile, map).toDouble()
                    score += foodAt(tile, map).toDouble() * 0.75
                    score += tradeAt(tile, map).toDouble() * 0.5
                }

                // update record
                if (score > bestScore) {
                    bestScore = score
                    bestSite = site
                }
            }
        }

        return bestSite
    }

    fun scoreTile(map: GameMap, tile: Pair<Int, Int>): Double {
        var score = 0.0
        score += productionAt(tile, map).toDouble()
        score += foodAt(tile, map).toDouble() * 0.75
        score += tradeAt(tile, map).toDouble() * 0.5
        return score
    }

    fun getThreatAt(pos: Pair<Int, Int>, us: Player, them: List<Player>, builtArmies: Int = 0): Double {
        var armies = 0.0
        for (player in them) {
            for (army in player.armies) {
                if (army.distanceTo(pos) <= 3) {
                    armies += army.hitpoints.toDouble() * player.offensiveStrength
                }
            }
        }
        var defenders = 0.0
        for (army in us.armies) {
            if (army.distanceTo(pos) <= 2) {
                defenders += army.hitpoints.toDouble() * us.defensiveStrength
            }
        }
        for (i in 0 until builtArmies) defenders += 100.0 * us.defensiveStrength
        
        return armies / defenders
    }

    // make a move
    // map is the game map
    // players is the list of all the players, containing their resources, cities, armies, workers
    // playerIndex is the index of you player in the players list

    // api functions:
    // doProduce is a function to call to produce something (city, army, worker)
    // doTechnology is a function to call to increase offesive or defensive strength
    // doMoveArmy moves an army from srcPos to dstPos
    // doMoveWorker moves a worker from srcPos to dstPos
    fun doMove(
            map: GameMap,
            players: List<Player>,
            playerIndex: Int,
            doProduce: (type: ProductionType, location: Pair<Int, Int>) -> Boolean,
            doTechnology: (type: TechnologyType) -> Boolean,
            doMoveArmy: (srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>) -> Boolean,
            doMoveWorker: (srcPos: Pair<Int, Int>, dstPos: Pair<Int, Int>) -> Boolean
    ) {
        // determine who we are and who they are
        val us = players[playerIndex]
        val them = players.filter {player -> player != us}

        // update treasury
        treasury.production = us.resources.get(ResourceType.Production) ?: 0
        treasury.food = us.resources.get(ResourceType.Food) ?: 0
        treasury.trade = us.resources.get(ResourceType.Trade) ?: 0

        // determine production savings based on city count, production change, and production gap
        // gap / 2 is the maximum savings
        // don't save until we have 5 cities (4 = 5 - 1)
        val cityCount = us.cities.size
        val productionChange = treasury.production - lastProduction
        lastProduction = treasury.production
        val productionGap = them.map { player -> treasury.production - (player.resources.get(ResourceType.Production) ?: 0) }.min() ?: 0
        try {
            productionSavings = minOf(productionGap / 2, ((cityCount - 4) * 100) / ((productionChange + 1) / 5))
        }
        catch (e: java.lang.ArithmeticException) {
            productionSavings = 0
        }
        
        //productionSavings = minOf(productionSavings.toDouble(), treasury.production.toDouble() * 0.5).toInt()

        if (productionSavings > 0) treasury.production -= productionSavings

        // rebuild cached terrain map
        val contents = MutableList(map.size) { MutableList(map.size) { TileType.Fogged } }
        for (x in 0 until map.size) {
            for (y in 0 until map.size) {
                if (cachedTerrainMap.contents[x][y] == TileType.Fogged) contents[x][y] = map.contents[x][y]
            }
        }
        val contentsAsArray = Array(map.size) { x -> Array(map.size) { y -> contents[x][y] } }

        cachedTerrainMap = GameMap(map.size, contentsAsArray)

        // research
        doResearch(doTechnology, us, them)

        // build armies at threatened cities (use savings)
        val totalPossibleArmies = floor(((treasury.production + productionSavings) / 8).toDouble())
        val threats = mutableMapOf<City, Double>()
        val threatenedCities = us.cities.sortedByDescending { city -> 
            val threat = getThreatAt(city.position, us, them)
            threat
        }

        var defending = true
        var totalBuilt = 0
        var builtInfo = mutableMapOf<City, Int>()
        while (defending && totalBuilt < totalPossibleArmies && treasury.production >= 8) {
            var unthreatened = 0
            for (city in threatenedCities) {
                if (getThreatAt(city.position, us, them) > 1.0) {
                    if (doProduce(ProductionType.Army, city.position)) {
                        totalBuilt++
                        builtInfo[city] = 1 + (builtInfo.get(city) ?: 0)
                        threats[city] = getThreatAt(city.position, us, them, builtInfo[city]!!)
                        treasury.production -= 8
                    }
                }
                else unthreatened++
            }

            defending = !(unthreatened >= threatenedCities.size)
        }

        var enemyCities = mutableSetOf<City>()
        var enemyWorkers = mutableSetOf<Worker>()
        var enemyArmies = mutableSetOf<Army>()

        for (player in them) {
            enemyCities = enemyCities.union(player.cities.toMutableSet()).toMutableSet()
            enemyWorkers = enemyWorkers.union(player.workers.toMutableSet()).toMutableSet()
            enemyArmies = enemyArmies.union(player.armies.toMutableSet()).toMutableSet()
        }

        val enemyUnits = enemyCities.union(enemyWorkers).union(enemyArmies)

        // move all armies towards enemies
        for (army in us.armies) {
            var nearest = enemyUnits.first()
            var distance = 100 // big
            for (unit in enemyUnits) {
                if (army.distanceTo(unit) < distance) {
                    nearest = unit
                    distance = army.distanceTo(unit)
                }
            }

            val movedHorz = when {
                nearest.position.first < army.position.first -> doMoveUnit(doMoveArmy, Direction.Left, army)
                nearest.position.first > army.position.first -> doMoveUnit(doMoveArmy, Direction.Right, army)
                else -> false
            }
            if (!movedHorz) {
                when {
                    nearest.position.second < army.position.second -> doMoveUnit(doMoveArmy, Direction.Up, army)
                    nearest.position.second > army.position.second -> doMoveUnit(doMoveArmy, Direction.Down, army)
                }
            }
        }


        // in each replicated city, build workers if there is room
        val saturated = mutableSetOf<City>()
        for (city in us.cities) {
            val surroundingTiles = getSurroundingTiles(city, map)

            val nearbyWorkers = mutableSetOf<Worker>()
            for (worker in us.workers) {
                if (worker.position in surroundingTiles) nearbyWorkers.add(worker)
            }

            var built = 0
            while (nearbyWorkers.size + built < surroundingTiles.size && treasury.production >= 8) {
                if (doProduce(ProductionType.Worker, city.position)) {
                    val newWorker = SmartWorker(this, city.position)
                    workers.add(newWorker)
                    built++
                    treasury.production -= 8
                }
                else break
            }

            if (nearbyWorkers.size + built >= surroundingTiles.size) saturated.add(city)
        }

        // make sure all workers are alive
        for (worker in workers) {
            var found = false
            for (dumbWorker in us.workers) {
                if (dumbWorker.position == worker.position) {
                    found = true
                    break
                }
            }
            worker.dead = !found
        }

        // handle debug restarts
        for (dumbWorker in us.workers) {
            var found = false
            for (worker in workers) {
                if (worker.position == dumbWorker.position) {
                    found = true
                    break
                }
            }
            if (!found) {
                var closest = us.cities.first()
                var distance = 100
                for (city in us.cities) {
                    val d = city.distanceTo(dumbWorker)
                    if (d < distance) {
                        distance = d
                        closest = city
                    }
                }
                val worker = SmartWorker(this, closest.position)
                worker.position = dumbWorker.position
                workers.add(worker)
            }
        }
        workers = workers.filter { worker -> !worker.dead }.toMutableSet() // remove dead workers

        // move all workers
        for (worker in workers) { // these are smart workers
            worker.resolve(doMoveWorker, us.workers)
        }

        // build cities (down to budget)
        newCities.clear()
        while (treasury.production >= 24) {
            val bestCitySite = getBestCitySite(map, us, them)
            if (doProduce(ProductionType.City, bestCitySite)) {
                newCities.add(bestCitySite)
                treasury.production -= 24
            }
        }
        
    }
}