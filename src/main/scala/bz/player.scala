package bz

import bz.game.Entity
import bz.gui.sprites.MultiSpriteStream
import bz.gui.{sprites, Drawable}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
import zio.IO

import scala.swing.{Graphics2D, Point}

object player {
  trait Player extends Entity
  class Scripted()
  class Ai()

  class Human(val id: String, val pt: Point, ss: MultiSpriteStream) extends Player with Drawable {
    var last: Long = 0

    def mv(pt: Point): Unit = ???

    var mover: api.Moved => Unit = become
    def handle(cmd: api.Event): Unit = cmd match {
      case e: api.Moved => mover(e)
      case _            => println("unhandled event")
    }

    def become(c: api.Moved): Unit = {
      c match {
        case api.Moved.Up(_, _) =>
          spr = ss.get("n").iterator
          mover = up
        case api.Moved.Down(_, _) =>
          spr = ss.get("s").iterator
          mover = down
        case api.Moved.Left(_, _) =>
          spr = ss.get("w").iterator
          mover = left
        case api.Moved.Right(_, _) =>
          spr = ss.get("e").iterator
          mover = right
      }
      cur = spr.nextOption()
      mover(c)
    }

    def up(c: api.Moved): Unit = c match {
      case api.Moved.Up(_, p) =>
        cur = spr.nextOption()
      case _ => become(c)
    }

    def down(c: api.Moved): Unit = c match {
      case api.Moved.Down(_, p) =>
        cur = spr.nextOption()
      case _ => become(c)
    }

    def left(c: api.Moved): Unit = c match {
      case api.Moved.Left(_, p) =>
        cur = spr.nextOption()
      case _ => become(c)
    }

    def right(c: api.Moved): Unit = c match {
      case api.Moved.Right(_, p) =>
        cur = spr.nextOption()
      case _ => become(c)
    }

    var spr: Iterator[sprites.Tile] = ss.get("s").iterator
    var cur: Option[sprites.Tile] = spr.nextOption()
    def draw(g2: Graphics2D): Unit =
      cur.foreach(_.draw(pt.x, pt.y, g2))
  }

  case class HumanConfigNotFound()
  def humanFromConfig(id: String, sheet: String, pt: Point, cfg: Config) =
    for {
      sheet <- IO(sprites.library.init(cfg)).map(_.find(_.id == sheet)).someOrFail(HumanConfigNotFound())
      ss <- sprites.fromSheetConfig(id, sheet, cfg)
    } yield new Human(id, pt, ss)

  def configFor(id: String, config: Config): Config =
    config
      .as[Config]("players")
      .withValue("sheet", ConfigValueFactory.fromAnyRef(id))
      // todo;; hacking the sheet in here, because the sheet init is hacked in above
      .withFallback(ConfigFactory.load("sheet.conf").getConfig("sheet"))
}
