package bz

import java.awt.Dimension

import bz.canvas.Canvas

import scala.swing.BorderPanel.Position
import scala.swing._

object gui {
  def apply(canvas: Canvas): MainFrame =
    new MainFrame {
      title = "bomz"

      contents = new BorderPanel {
        add(canvas, Position.Center)
      }

      preferredSize = new Dimension(480, 480)
      pack()
      centerOnScreen()
      open()
    }
}
