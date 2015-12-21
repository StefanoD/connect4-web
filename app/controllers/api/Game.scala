package controllers.api

import models.GameModel
import play.api.mvc.Result
import play.mvc.Controller

import scala.collection.mutable

import play.api.data.Form
import play.api.data.Forms._

/**
* Created with IntelliJ IDEA.
* User: jakub, stefano
* Date: 10/21/13
* Time: 11:04 PM
*/
class Game extends Controller {
  private val gamesMap = new mutable.HashMap[String, GameModel]()

  def newGameWithName(gameName: String): Result = {
    val gameModel = new GameModel

    newGame(gameName, gameModel)
  }

  def newGame(gameName: String, gameModel: GameModel): Result = {
   ???
  }
}
