package bz

import scala.swing.Point

object api {

  sealed trait MoveCommand {
    def move(p: Point): Unit
  }
  case object Up extends MoveCommand {
    def move(p: Point) = p.translate(0, -8)
  }
  case object Down extends MoveCommand {
    def move(p: Point) = p.translate( 0, 8)
  }
  case object Left extends MoveCommand {
    def move(p: Point) = p.translate(-8, 0)
  }
  case object Right extends MoveCommand {
    def move(p: Point) = p.translate(8, 0)
  }
}
