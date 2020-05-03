package app

// EDIT THIS FILE

class AI() {
    // Change the name that shows up on the graphical display
    // optional, but HIGHLY RECOMMENCED
    fun getName() = "Sample AI Template"

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
        // make moves

        // for example, produce a worker on one of our cities if we have enough production
        if(players[playerIndex].resources[ResourceType.Production]!! >= 8) {
            doProduce(ProductionType.Worker, players[playerIndex].cities[0].position)
        }

        // another example: move all armies to the right one unit(why not?)
        for(army in players[playerIndex].armies) {
            doMoveArmy(army.position, Pair(army.position.first + 1, army.position.second))
        }
    }
}