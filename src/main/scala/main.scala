import bz.{items, _}
import com.typesafe.config.ConfigFactory
import zio._
import zio.duration._

object main extends scala.App {
  val config = ConfigFactory.load()
  val playerCfg = config.getConfig("players")
  val bombCfg = config
    .getConfig("bomb")
    // todo;; hacking the sheet in here
    .withFallback(ConfigFactory.load("sheet.conf").getConfig("sheet"))

  val app = for {
    b <- game.board.withBackground(resources.get("/bg.png"))
    (c, q) <- canvas.withInput(b)

    p <- player.humanFromConfig(player.configFor("player1", config))
    _ = b.add(p)

    _ = bz.gui(c)

    lib <- ZIO.fromOption(sprites.library.init(bombCfg).find(_.id == "bomb"))
    ss <- sprites.spriteStream(lib, bombCfg)
    _ = b.add(items.Bomb(50, 50, ss))
    _ = b.add(items.Bomb(150, 150, ss))

    update = for {
      i <- q.take
      _ = p.move(i)
      _ <- ZIO.effect(c.repaint())
    } yield ()
    _ <- update.forever.fork

    repaint = for {
      _ <- ZIO.effect(c.repaint())
    } yield ()
    ff <- repaint.repeat(Schedule.spaced(10.millis)).forever.fork
    _ <- ff.join
  } yield ()

  Runtime.default.unsafeRun(app)
}
