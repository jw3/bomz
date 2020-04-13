package bz

import bz.api.MoveCommand
import zio.{Queue, ZIO}

import scala.swing.event.Key
import scala.swing.{event, Component, Graphics2D}

object canvas {

  def withInput(board: game.DrawableBoard): ZIO[Any, Nothing, (KeyboardCanvas, Queue[MoveCommand])] =
    for {
      q <- Queue.bounded[MoveCommand](10)
      c = new KeyboardCanvas(board, q)
    } yield (c, q)

  trait Canvas extends Component

  class KeyboardCanvas(board: game.DrawableBoard, q: Queue[MoveCommand]) extends Component with Canvas {
    listenTo(keys)
    focusable = true

    reactions += {
      case event.KeyPressed(_, Key.W | Key.Up, _, _)    => zio.Runtime.default.unsafeRun(q.offer(api.Up))
      case event.KeyPressed(_, Key.S | Key.Down, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Down))
      case event.KeyPressed(_, Key.A | Key.Left, _, _)  => zio.Runtime.default.unsafeRun(q.offer(api.Left))
      case event.KeyPressed(_, Key.D | Key.Right, _, _) => zio.Runtime.default.unsafeRun(q.offer(api.Right))
    }
    override def paintComponent(g2: Graphics2D): Unit = board.draw(g2)
  }
}
