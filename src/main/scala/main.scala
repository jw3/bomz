import bz.{canvas, game, player, resources}
import com.typesafe.config.ConfigFactory
import zio._
import zio.duration._

object main extends scala.App {
  val config = ConfigFactory.load()
  val playerCfg = config.getConfig("players")

  val app = for {
    b <- game.board.withBackground(resources.get("/bg.png"))
    (c, q) <- canvas.withInput(b)

    p <- player.humanFromConfig(player.configFor("player1", config))
    _ = b.add(p)
    _ = bz.gui(c)

    update = for {
      i <- q.take
      _ = p.move(i)
    } yield ()

    repaint = for {
      _ <- update
      f <- ZIO.effect(c.repaint())
    } yield f
    _ <- repaint.repeat(Schedule.spaced(50.millis))
  } yield ()

  Runtime.default.unsafeRun(app)
}
