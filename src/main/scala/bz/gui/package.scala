package bz

import bz.gui.canvas.Canvas

import scala.swing.BorderPanel.Position
import scala.swing.{BorderPanel, Dimension, MainFrame}

package object gui {
  def frame(canvas: Canvas): MainFrame =
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
