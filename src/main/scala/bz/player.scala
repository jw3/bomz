package bz

import bz.game.Entity
import bz.gui.sprites.MultiSpriteStream
import bz.gui.{sprites, Drawable}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
import zio.ZIO

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
        case api.Moved.Up(_, _)    =>
          spr = ss.get("n").iterator
          mover = up
        case api.Moved.Down(_, _)  =>
          spr = ss.get("s").iterator
          mover = down
        case api.Moved.Left(_, _)  =>
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

//    def move(dd: api.Move) = {
//      val now = System.currentTimeMillis()
//      if (now - last > 100) {
//        last = now
//        dd match {
//          case api.Up if d == api.Up =>
//            d.move(xy)
//            cur = spr.nextOption()
//
//          case api.Up =>
//            d = dd
//            d.move(xy)
//            spr = ss.get("n").iterator
//            cur = spr.nextOption()
//
//          case api.Down if d == api.Down =>
//            d.move(xy)
//            cur = spr.nextOption()
//
//          case api.Down =>
//            d = dd
//            d.move(xy)
//            spr = ss.get("s").iterator
//            cur = spr.nextOption()
//
//          case api.Right if d == api.Right =>
//            d.move(xy)
//            cur = spr.nextOption()
//
//          case api.Right =>
//            d = dd
//            d.move(xy)
//            spr = ss.get("e").iterator
//            cur = spr.nextOption()
//
//          case api.Left if d == api.Left =>
//            d.move(xy)
//            cur = spr.nextOption()
//
//          case api.Left =>
//            d = dd
//            d.move(xy)
//            spr = ss.get("w").iterator
//            cur = spr.nextOption()
//        }
//      }
//    }

    var spr: Iterator[sprites.Tile] = ss.get("s").iterator
    var cur: Option[sprites.Tile] = spr.nextOption()
    def draw(g2: Graphics2D): Unit =
      cur.foreach(_.draw(pt.x, pt.y, g2))
  }

  def humanFromConfig(id: String, sheet: String, pt: Point, cfg: Config): ZIO[Any, Unit, Human] =
    for {
      // todo;; the init call here is hack
      lib <- ZIO.fromOption(sprites.library.init(cfg).find(_.id == sheet))
      ss <- sprites.fromSheetConfig(lib, cfg)
    } yield new Human(id, pt, ss)

  def configFor(id: String, config: Config): Config =
    config
      .as[Config]("players")
      .withValue("sheet", ConfigValueFactory.fromAnyRef(id))
      // todo;; hacking the sheet in here, because the sheet init is hacked in above
      .withFallback(ConfigFactory.load("sheet.conf").getConfig("sheet"))
}
