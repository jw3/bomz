package bz

import bz.game.Entity

import scala.swing.Point

object api {
  sealed trait Command {
    def eid: String
    def exec(e: Entity): Event
  }
  sealed trait Event {
    def eid: String
  }

  sealed trait Move extends Command
  object Move {
    case class Up(eid: String) extends Move {
      def exec(e: Entity): Moved = {
        e.pt.translate(0, -8)
        Moved.Up(e.id, e.pt)
      }
    }
    case class Down(eid: String) extends Move {
      def exec(e: Entity): Moved = {
        e.pt.translate(0, 8)
        Moved.Down(e.id, e.pt)
      }
    }
    case class Left(eid: String) extends Move {
      def exec(e: Entity): Moved = {
        e.pt.translate(-8, 0)
        Moved.Left(e.id, e.pt)
      }
    }
    case class Right(eid: String) extends Move {
      def exec(e: Entity): Moved = {
        e.pt.translate(8, 0)
        Moved.Right(e.id, e.pt)
      }
    }
  }

  sealed trait Moved extends Event {
    def p: Point
  }

  object Moved {
    case class Up(eid: String, p: Point) extends Moved
    case class Down(eid: String, p: Point) extends Moved
    case class Left(eid: String, p: Point) extends Moved
    case class Right(eid: String, p: Point) extends Moved
  }
}
