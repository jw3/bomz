package bz

import bz.api.MoveCommand
import bz.sprites.MultiSpriteStream
import com.typesafe.config.Config
import zio.ZIO

import scala.swing.Graphics2D

object player {
  trait Player
  class Scripted()
  class Ai()

  class Human(ss: MultiSpriteStream) extends Player with Drawable {
    var d: MoveCommand = api.Down
    var x = 0
    var y = 0

    def move(dd: api.MoveCommand) = dd match {
      case api.Up    => y -= 8
      case api.Down  => y += 8
      case api.Right => x += 8
      case api.Left  => x -= 8
    }

    var spr: Iterator[sprites.Tile] = ss.get("s").iterator
    def draw(g2: Graphics2D): Unit =
      spr.next().draw(x, y, g2)
  }

  def humanFromConfig(cfg: Config): ZIO[Any, Unit, Human] =
    for {
      ss <- sprites.fromPlayerConfig(cfg)
    } yield new Human(ss)
}
