package bz

import bz.api.MoveCommand
import bz.game.Entity
import bz.gui.{Drawable, sprites}
import bz.gui.sprites.MultiSpriteStream
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
import zio.ZIO

import scala.swing.{Graphics2D, Point}

object player {
  trait Player extends Entity
  class Scripted()
  class Ai()

  class Human(ss: MultiSpriteStream) extends Player with Drawable {
    var d: MoveCommand = api.Down
    var xy: Point = new Point(0, 0)
    var last: Long = 0

    def move(dd: api.MoveCommand) = {
      val now = System.currentTimeMillis()
      if (now - last > 100) {
        last = now
        dd match {
          case api.Up if d == api.Up =>
            d.move(xy)
            cur = spr.nextOption()

          case api.Up =>
            d = dd
            d.move(xy)
            spr = ss.get("n").iterator
            cur = spr.nextOption()

          case api.Down if d == api.Down =>
            d.move(xy)
            cur = spr.nextOption()

          case api.Down =>
            d = dd
            d.move(xy)
            spr = ss.get("s").iterator
            cur = spr.nextOption()

          case api.Right if d == api.Right =>
            d.move(xy)
            cur = spr.nextOption()

          case api.Right =>
            d = dd
            d.move(xy)
            spr = ss.get("e").iterator
            cur = spr.nextOption()

          case api.Left if d == api.Left =>
            d.move(xy)
            cur = spr.nextOption()

          case api.Left =>
            d = dd
            d.move(xy)
            spr = ss.get("w").iterator
            cur = spr.nextOption()
        }
      }
    }

    var spr: Iterator[sprites.Tile] = ss.get("s").iterator
    var cur: Option[sprites.Tile] = spr.nextOption()
    def draw(g2: Graphics2D): Unit =
      cur.foreach(_.draw(xy.x, xy.y, g2))
  }

  def humanFromConfig(cfg: Config): ZIO[Any, Unit, Human] =
    for {
      // todo;; the init call here is hack
      lib <- ZIO.fromOption(sprites.library.init(cfg).find(_.id == "player1"))
      ss <- sprites.fromSheetConfig(lib, cfg)
    } yield new Human(ss)

  def configFor(id: String, config: Config): Config =
    config
      .as[Config]("players")
      .withValue("sheet", ConfigValueFactory.fromAnyRef(id))
      // todo;; hacking the sheet in here, because the sheet init is hacked in above
      .withFallback(ConfigFactory.load("sheet.conf").getConfig("sheet"))
}
