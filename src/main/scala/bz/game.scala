package bz

import bz.api.{Command, Event, Move}
import bz.items.Item

import scala.collection.mutable
import scala.swing.Point

// a board requires a grid
// a board optionally can be drawn
object game {
  type GridCells = List[List[mutable.Stack[Item]]]

  trait Entity {
    def id: String
    def pt: Point // todo;; this will need abstracted or rect
//    def mv(pt: Point): Unit
    //def handle(cmd: Command): Unit
    def handle(e: Event): Unit
  }

  class Grid(cells: GridCells)
  object Grid {
    def apply(): Grid = apply(8, 8)
    def apply(w: Int, h: Int): Grid = {
      val cells = List.fill(w * h)(mutable.Stack.empty[Item]).grouped(w).toList
      new Grid(cells)
    }
  }

  trait Board {
    def entities: Map[String, Entity]
    def add(p: Entity): Unit
  }
  object board {
    class Default() extends Board {
      var e = Map.empty[String, Entity]

      def entities: Map[String, Entity] = e

      def add(p: Entity): Unit =
        e += p.id -> p

      def exec(c: Command): Unit = c match {
        case m: Move =>
          e.get(m.eid).map(m.exec).foreach(println)
      }
    }
  }
}
