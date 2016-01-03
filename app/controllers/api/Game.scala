package controllers.api

import java.util.UUID

import modelinterfaces.Player
import models.GameModel
import org.apache.commons.lang3.ArrayUtils
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc._

import scala.collection.mutable

import play.api._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json._
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

  def gameFields = Action { request =>
      val sessionPlayer = request.session.get("player").getOrElse("")
      val gameFields = JsArray(
        (for {
          (gameName, gameModel) <- gamesMap
          gameFieldJs = gameFieldToJsonNode(gameName, gameModel, sessionPlayer)
        } yield gameFieldJs).toSeq)

      val node: JsValue = Json.obj(
        "games" -> gameFields
      )
      Ok(node)
  }

  def gameField(gameName: String) = Action {
    request =>
      val model: GameModel = gamesMap.get(gameName).getOrElse(null)

      if (model == null) {
        BadRequest
      } else {
        val node: JsValue = Json.obj(
          "game" -> gameFieldToJsonNode(gameName, model, request.session.get("player").getOrElse(""))
        )
        Ok(node)
      }
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

  def undo(gameName: String) = {
    if (gamesMap.contains(gameName)) {
      val node = Json.obj("undone" -> true)
      val model = gamesMap.get(gameName).get

      model.controller.undoLastMove

      Ok(node)
    } else {
      BadRequest
    }
  }

  //Evtl total unnötig, da wir kein Save und Load machen!
  private def gameFieldToJsonNode(gameName: String, gameModel: GameModel, sessionPlayer: String): JsValue = {
    val gameField: Array[Array[Player]] = gameModel.controller.getCopyOfGameField.gameField.reverse
    val playerOnTurn = if (gameModel.playerOnTurn.hashCode.toString == sessionPlayer)
      "you"
    else "opponent"

    val gameArrayNode: Seq[JsArray] =
      for {
        rows: Array[Player] <- gameField

        rowsNode: JsArray = JsArray(
          (for {
            p <- rows
            player: JsValue = if (p != null) {
              if (p eq gameModel.opponent) JsString("opponent")
              else JsString("you")
            }
            else {
              JsNull
            }
          } yield player).toSeq)
      } yield rowsNode

    val node: JsValue = Json.obj(
      "id" -> gameName,
      "isWaitingForOpponent" -> gameModel.waitingForOpponent,
      "gameStarted" -> gameModel.started,
      "playerOnTurn" -> playerOnTurn,
      "game_field" -> gameArrayNode
    )

    node
  }
}
