import bz.{canvas, game, player, resources}
import com.typesafe.config.ConfigFactory
import zio._

object main extends scala.App {
  val config = ConfigFactory.load()
  val playerCfg = config.getConfig("player.default")

  val app = for {
    b <- game.board.withBackground(resources.get("plainbg.png"))
    (c, q) <- canvas.withInput(b)
    p <- player.humanFromConfig(playerCfg)
    _ = b.add(p)
    _ = bz.gui(c)

    loop = for {
      i <- q.take
      _ = p.move(i)
      f <- ZIO.effect(c.repaint())
    } yield ()

    _ <- loop.forever

//    repaint = for {
//      f <- ZIO.effect(c.repaint())
//    } yield f
//    _ <- repaint.repeat(Schedule.spaced(100.millis))
  } yield ()

  Runtime.default.unsafeRun(app)
}
