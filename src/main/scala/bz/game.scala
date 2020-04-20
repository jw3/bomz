package bz

import bz.items.Item

import scala.collection.mutable

// a board requires a grid
// a board optionally can be drawn
object game {
  type GridCells = List[List[mutable.Stack[Item]]]

  trait Entity {
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
    def entities: Seq[Entity]
    def add(p: Entity): Unit
  }
  object board {
    class Default() extends Board {
      var e = List.empty[Entity]

      def entities: Seq[Entity] = e

      def add(p: Entity): Unit =
        e +:= p
    }
  }
}
