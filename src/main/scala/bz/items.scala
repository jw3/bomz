package bz
import bz.sprites.SpriteStream

import scala.swing.Graphics2D

object items {
  trait Item {
    def x: Int
    def y: Int
  }

  case class Bomb(x: Int, y: Int, ss: SpriteStream) extends Item with Drawable {
    var spr: Iterator[sprites.Tile] = ss.get().iterator
    var cur: Option[sprites.Tile] = spr.nextOption()

    var last: Long = 0
    def draw(g2: Graphics2D): Unit = {
      val now = System.currentTimeMillis()
      if (now - last > 200) {
        cur = spr.nextOption()
        last = now
      }
      cur.foreach(_.draw(x, y, g2))
    }
  }
}
