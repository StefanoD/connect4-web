package models

import connectfour.controller.Connect4GameController
import connectfour.model.{Connect4Computer, Connect4Player}
import controller.GameController


class GameModel() {
  val player = new Connect4Player("You")
  val opponent = new Connect4Computer("Computer", Connect4GameController.getCurrentInstance)

  var waitingForOpponent = false
  var started = false

  def startGame: Boolean = {
    if (!waitingForOpponent) {
      Connect4GameController.reset()
      return true
    }
    return false
  }
}
