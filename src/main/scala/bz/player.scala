package bz

import bz.api.MoveCommand
import bz.sprites.MultiSpriteStream
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
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
