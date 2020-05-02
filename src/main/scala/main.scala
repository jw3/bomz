import bz.gui.{canvas, sprites}
import bz.{items, _}
import com.typesafe.config.ConfigFactory
import zio._
import zio.duration._

import scala.swing.Point

object main extends scala.App {
  val config = ConfigFactory.load()
  val playerCfg = config.getConfig("players")
  val bombCfg = config
    .getConfig("bomb")
    // todo;; hacking the sheet in here
    .withFallback(ConfigFactory.load("sheet.conf").getConfig("sheet"))

  val app = for {
    gb <- ZIO.succeed(new game.board.Default)
    canvasInit = for {
      bg <- canvas.fromBackground(gb, resources.get("/bg.png"))
      (c, q) <- canvas.withKeyboard(bg)
    } yield (c, q)
    (c, q) <- canvasInit

    p <- player.humanFromConfig("p1", "player1", new Point(0, 0), player.configFor("player1", config))
    _ = gb.add(p)

    p2 <- player.humanFromConfig("p2", "player7", new Point(200, 200), player.configFor("player7", config))
    _ = gb.add(p2)

    _ = bz.gui.frame(c)

    lib <- ZIO.fromOption(sprites.library.init(bombCfg).find(_.id == "bomb"))
    ss <- sprites.spriteStream(lib, bombCfg)
    _ = gb.add(items.Bomb("b1", new Point(50, 50), ss))
    _ = gb.add(items.Bomb("b2", new Point(150, 150), ss))

    update = for {
      i <- q.take
      _ = gb.exec(i)
    } yield ()
    _ <- update.forever.fork

    repaint = for {
      _ <- ZIO.effect(c.updateAndRepaint())
    } yield ()
    ff <- repaint.repeat(Schedule.spaced(150.millis)).forever.fork
    _ <- ff.join
  } yield ()

  Runtime.default.unsafeRun(app)
}
