package bz
import bz.game.Entity
import bz.gui.{Drawable, sprites}
import bz.gui.sprites.SpriteStream

import scala.swing.Graphics2D

object items {
  trait Item extends Entity {
    def x: Int
    def y: Int
  }

  case class Bomb(x: Int, y: Int, ss: SpriteStream) extends Item with Drawable {
    var spr: Iterator[sprites.Tile] = ss.get().iterator
    def draw(g2: Graphics2D): Unit =
      spr.next().draw(x, y, g2)
  }
}
