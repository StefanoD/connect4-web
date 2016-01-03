package controllers

import play.api._
import play.api.mvc._
import connectfour.model.Connect4Player

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Connect 4 Pl(us/ay)"))
  }

}
