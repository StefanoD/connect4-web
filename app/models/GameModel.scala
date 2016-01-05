package models

import connectfour.controller.Connect4GameController
import connectfour.model.{Connect4Computer, Connect4Player}
import controller.GameController


class GameModel() {
  val controller = Connect4GameController.getNewInstance("You", "Computer")
  val (player, opponent) = controller.getPlayers

  var waitingForOpponent = false
  var started = false

  def startGame: Boolean = {
    if (!waitingForOpponent) {
      Connect4GameController.reset()
      started = true
      return true
    }
    return false
  }

  def playerOnTurn = controller.getPlayerOnTurn
}
