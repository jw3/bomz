package bz.gui

import java.awt.image.BufferedImage
import java.awt.{Color, Image}
import java.io.IOException
import java.net.URL

import bz.api.Move
import bz.{api, game}
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

  def withKeyboard(c: Canvas): ZIO[Any, Nothing, (Canvas, Queue[Move])] =
    for {
      q <- Queue.bounded[Move](10)
      _ = keyboardToQueue(c, q)
    } yield (c, q)

  trait Canvas extends Component {
    def updateAndRepaint(): Unit
  }

  class DefaultCanvas(gb: game.Board, w: Int, h: Int, bg: Option[Image] = None) extends Component with Canvas {
    val dbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2 = dbi.createGraphics()

    background = Color.RED

    def drawables: Seq[Drawable] =
      gb.entities.flatMap {
        case (_, e: Drawable) => Some(e)
        case _                => None
      }.toSeq

    def updateAndRepaint(): Unit = {
      bg.foreach(g2.drawImage(_, 0, 0, null))
      drawables.foreach(_.draw(g2))
      val g = peer.getGraphics
      g.drawImage(dbi, 0, 0, null)
      g.dispose()
      g2.clearRect(0, 0, w, h)
    }
  }

  def keyboardToQueue(c: Canvas, q: Queue[Move]): Unit = {
    c.listenTo(c.keys)
    c.focusable = true

    c.reactions += {
      // todo;; hack p1
      case event.KeyPressed(_, Key.W, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Move.Up("p1")))
      case event.KeyPressed(_, Key.S, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Move.Down("p1")))
      case event.KeyPressed(_, Key.A, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Move.Left("p1")))
      case event.KeyPressed(_, Key.D, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Move.Right("p1")))
      // todo;; hack p2
      case event.KeyPressed(_, Key.Up, _, _)    => zio.Runtime.default.unsafeRun(q.offer(api.Move.Up("p2")))
      case event.KeyPressed(_, Key.Down, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Move.Down("p2")))
      case event.KeyPressed(_, Key.Left, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Move.Left("p2")))
      case event.KeyPressed(_, Key.Right, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Move.Right("p2")))
    }
  }
}
