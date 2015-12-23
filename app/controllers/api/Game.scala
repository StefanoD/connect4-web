package controllers.api

import java.util.UUID

import models.GameModel
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.{BodyParsers, Action, Result}

import scala.collection.mutable

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.{JsArray, JsValue, JsError, Json}
import models._

case class GameData(id: Option[String])

class Game extends Controller {
  private val gamesMap = new mutable.HashMap[String, GameModel]()
  private val gameForm = Form(
    mapping(
      "id" -> optional(text)
    )(GameData.apply)(GameData.unapply)
  )

  def newGameWithoutName() = {
    newGameWithName(UUID.randomUUID.toString)
  }

  def newGameWithName(gameName: String) = {
    newGame(gameName, new GameModel)
  }

  def newGame(gameName: String, gameModel: GameModel) = Action(BodyParsers.parse.json) {
    request =>
      val model: GameModel = gamesMap.get(gameName).getOrElse(gameModel)

      gamesMap.put(gameName, model)

      val sessionPlayer = request.session.get("player").getOrElse("")
      val playerOnTurn = model.playerOnTurn.hashCode.toString

      val playerOnTurnJS: (String, JsValueWrapper) =
        if (playerOnTurn == sessionPlayer) {
          "playerOnTurn" -> "you"
        } else {
          "playerOnTurn" -> "opponent"
        }

      val node: JsValue = Json.obj(
        "game" -> Json.obj("id" -> gameName,
          "isWaitingForOpponent" -> model.waitingForOpponent,
          "gameStarted" -> model.startGame,
          playerOnTurnJS,
          "game_field" -> JsArray())
      )

      Ok(node).withSession(("player", model.player.hashCode.toString))
  }

  def dropCoin(gameName: String, column: Int) = Action {
    request =>
      val sessionPlayer = request.session.get("player").getOrElse("")

      val dropped: Boolean = if (gamesMap.contains(gameName)) {
        val model: GameModel = gamesMap.get(gameName).get
        val hashcode = model.playerOnTurn.hashCode.toString

        model.started && sessionPlayer == hashcode && model.controller.dropCoin(column)
      } else {
        false
      }

      val node: JsValue = Json.obj(
        "dropped" -> dropped
      )

      Ok(node)
  }
}
