package controllers.api

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

  def newGameWithName(gameName: String) = Action(BodyParsers.parse.json) {
    request =>
      val gameModel = new GameModel

      gameForm.bindFromRequest().fold(
        errors => {
          BadRequest(Json.obj("status" -> "KO"))
        },
        game => {
          newGame(gameName, gameModel)
        }
      )
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
}
