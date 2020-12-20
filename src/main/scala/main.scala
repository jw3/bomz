import bz.gui.{canvas, sprites}
import bz.{items, _}
import com.typesafe.config.ConfigFactory
import zio._
import zio.clock.Clock
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
    inputbus <- Queue.bounded[api.Event](10)

    gb <- ZIO.succeed(new game.board.Default(inputbus))
    canvasInit = for {
      bg <- canvas.fromBackground(gb, resources.get("/bg.png"))
      (c, q) <- canvas.withKeyboard(bg)
    } yield (c, q)
    (c, commandQueue) <- canvasInit

    p <- player.humanFromConfig("p1", "player1", new Point(0, 0), player.configFor("player1", config))
    _ = gb.add(p)

    p2 <- player.humanFromConfig("p2", "player7", new Point(200, 200), player.configFor("player7", config))
    _ = gb.add(p2)

    _ = bz.gui.frame(c)

    lib <- ZIO.fromOption(sprites.library.init(bombCfg).find(_.id == "bomb"))
    ss <- sprites.spriteStream("tick", lib, bombCfg)
    _ = gb.add(items.Bomb("b1", new Point(50, 50), ss))
    _ = gb.add(items.Bomb("b2", new Point(150, 150), ss))

    update = for {
      Some(i) <- commandQueue.take.timeoutTo(None)(Some(_))(100.milli)
      e <- gb.exec(i)
    } yield (println(e))

    loop = for {
      _ <- IO.effect(c.updateAndRepaint()).absorb
      _ <- update.repeat(Schedule.duration(1.millis)).ignore
    } yield ()

    ff <- loop.forever.fork
    _ <- ff.join
  } yield ()

  val deps = zio.system.System.live >+> Clock.live >+> zio.console.Console.live
  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
