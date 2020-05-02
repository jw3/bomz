package bz
import bz.game.Entity
import bz.gui.{Drawable, sprites}
import bz.gui.sprites.SpriteStream

import scala.swing.{Graphics2D, Point}

object items {
  trait Item extends Entity

  case class Bomb(id: String, pt: Point, ss: SpriteStream) extends Item with Drawable {
    var spr: Iterator[sprites.Tile] = ss.get().iterator
    def draw(g2: Graphics2D): Unit =
      spr.next().draw(pt.x, pt.y, g2)

    def mv(pt: Point): Unit = ???
    def handle(e: api.Event): Unit = ???
  }
}
