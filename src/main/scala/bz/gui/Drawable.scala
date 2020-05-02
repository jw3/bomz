package bz.gui

import scala.swing.Graphics2D

trait Drawable {
  def draw(g2: Graphics2D): Unit
}
