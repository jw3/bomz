package bz

import java.awt.image.BufferedImage
import java.awt.{Color, Image}
import java.io.IOException
import java.net.URL

import bz.api.MoveCommand
import javax.imageio.ImageIO
import zio.{IO, Queue, ZIO}

import scala.swing.event.Key
import scala.swing.{event, Component}

object canvas {
  def fromBackground(gb: game.Board, image: URL): IO[IOException, Canvas] =
    IO.fromFunction(_ => ImageIO.read(image)).map { bg =>
      println(s"${bg.getWidth}x${bg.getHeight}")
      new DefaultCanvas(gb, bg.getWidth, bg.getHeight, Some(bg))
    }

  def withKeyboard(c: Canvas): ZIO[Any, Nothing, (Canvas, Queue[MoveCommand])] =
    for {
      q <- Queue.bounded[MoveCommand](10)
      _ = keyboardToQueue(c, q)
    } yield (c, q)

  trait Canvas extends Component {
    def updateAndRepaint(): Unit
  }

  class DefaultCanvas(gb: game.Board, w: Int, h: Int, bg: Option[Image] = None) extends Component with Canvas {
    val dbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = dbi.createGraphics()

    background = Color.RED

    def drawables: Seq[Drawable] = gb.entities.map {
      case e: Drawable => e
    }

    def updateAndRepaint(): Unit = {
      bg.foreach(g2.drawImage(_, 0, 0, null))
      drawables.foreach(_.draw(g2))
      val g = peer.getGraphics
      g.drawImage(dbi, 0, 0, null)
      g.dispose()
      g2.clearRect(0, 0, w, h)
    }
  }

  def keyboardToQueue(c: Canvas, q: Queue[MoveCommand]): Unit = {
    c.listenTo(c.keys)
    c.focusable = true

    c.reactions += {
      case event.KeyPressed(_, Key.W | Key.Up, _, _)    => zio.Runtime.default.unsafeRun(q.offer(api.Up))
      case event.KeyPressed(_, Key.S | Key.Down, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Down))
      case event.KeyPressed(_, Key.A | Key.Left, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Left))
      case event.KeyPressed(_, Key.D | Key.Right, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Right))
    }
  }
}
