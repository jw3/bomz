package bz

import java.io.IOException
import java.net.URL

import bz.items.Item
import javax.imageio.ImageIO
import zio.IO

import scala.collection.mutable
import scala.swing.Graphics2D

// a board requires a grid
// a board optionally can be drawn
object game {
  type GridCells = List[List[mutable.Stack[Item]]]
  type DrawableBoard = Board with Drawable

  class Grid(cells: GridCells)
  object Grid {
    def apply(): Grid = apply(8, 8)
    def apply(w: Int, h: Int): Grid = {
      val cells = List.fill(w * h)(mutable.Stack.empty[Item]).grouped(w).toList
      new Grid(cells)
    }
  }

  trait Board {
    def add(p: Drawable): Unit
  }
  object board {
    class Default() extends Board {
      var drawables = List.empty[Drawable]

      def add(p: Drawable): Unit =
        drawables +:= p
    }

    def withBackground(image: URL): IO[IOException, DrawableBoard] =
      IO.fromFunction(_ => ImageIO.read(image)).map { bg =>
        println(s"${bg.getWidth}x${bg.getHeight}")
        new Default() with Drawable {
          def draw(g2: Graphics2D): Unit = {
            g2.drawImage(bg, 0, 0, null)
            drawables.foreach(_.draw(g2))
          }
        }
      }
  }
}
