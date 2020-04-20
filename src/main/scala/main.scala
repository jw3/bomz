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
    gb <- ZIO.succeed(new game.board.Default)
    canvasInit = for {
      bg <- canvas.fromBackground(gb, resources.get("/bg.png"))
      (c, q) <- canvas.withKeyboard(bg)
    } yield (c, q)
    (c, q) <- canvasInit

    p <- player.humanFromConfig(player.configFor("player1", config))
    _ = gb.add(p)

    _ = bz.gui(c)

    lib <- ZIO.fromOption(sprites.library.init(bombCfg).find(_.id == "bomb"))
    ss <- sprites.spriteStream(lib, bombCfg)
    _ = gb.add(items.Bomb(50, 50, ss))
    _ = gb.add(items.Bomb(150, 150, ss))

    update = for {
      i <- q.take
      _ = p.move(i)
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
