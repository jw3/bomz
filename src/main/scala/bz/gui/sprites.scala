package bz.gui

import bz.gui.sprites.library.SheetBlock
import bz.resources
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import zio.IO

import java.awt.Toolkit
import java.awt.image.{BufferedImage, FilteredImageSource, RGBImageFilter}
import java.net.URL
import java.nio.file.Paths
import javax.imageio.ImageIO
import scala.swing.{Color, Graphics2D, Image}

object sprites {
  trait Tile {
    def w: Int
    def h: Int
    def draw(x: Int, y: Int, g2: Graphics2D): Unit
    def draw(x: Int, y: Int, w: Int, h: Int, g2: Graphics2D): Unit
  }
  object Tile {
    def apply(im: Image): Tile = new Tile {
      def w: Int = im.getWidth(null)
      def h: Int = im.getHeight(null)
      def draw(x: Int, y: Int, g2: Graphics2D): Unit = g2.drawImage(im, x, y, null)
      def draw(x: Int, y: Int, w: Int, h: Int, g2: Graphics2D): Unit = g2.drawImage(im, x, y, w, h, null)
    }

    def hflip(tile: Tile): Tile = {
      val bi = new BufferedImage(tile.w, tile.h, BufferedImage.TYPE_INT_ARGB)
      val g2 = bi.getGraphics.asInstanceOf[Graphics2D]
      tile.draw(tile.w, 0, -tile.w, tile.h, g2)
      g2.dispose()
      Tile(bi)
    }
  }

  def tilesFrom(images: Seq[URL]) = IO.foreach(images)(i => IO(ImageIO.read(i)).map(Tile.apply))

  class Player()

  // single frame - special item
  class SpriteMap

  // multi frame - bomb pulsing
  class SpriteStream(seq: Seq[Tile]) {
    def get(): Seq[Tile] = LazyList.continually(seq).flatten
  }

  // multi frame oriented - player walking
  class MultiSpriteStream(map: Map[String, SpriteStream]) {
    def get(k: String): Seq[Tile] = map(k).get()
  }

  def fromPlayerConfig(config: Config) =
    for {
      n <- tilesFrom(config.as[List[String]]("up").map(resources.get))
      s <- tilesFrom(config.as[List[String]]("down").map(resources.get))
      e <- tilesFrom(config.as[List[String]]("left").map(resources.get))
      w <- tilesFrom(config.as[List[String]]("right").map(resources.get))
    } yield new MultiSpriteStream(Map(
      "n" -> new SpriteStream(n),
      "s" -> new SpriteStream(s),
      "e" -> new SpriteStream(e),
      "w" -> new SpriteStream(w)))

  type TileF = Tile => Tile
  def spriteStream(path: String, sheet: SheetBlock, config: Config, tx: TileF = identity) =
    for {
      s <- IO(config.as[List[Int]](path))
      z = sheet.tiles.zipWithIndex
      r = z.filter(k => s.contains(k._2)).map(_._1).map(Tile.apply)
    } yield new SpriteStream(r.map(tx))

  def fromSheetConfig(path: String, sheet: SheetBlock, config: Config) =
    for {
      n <- spriteStream("up", sheet, config)
      s <- spriteStream("down", sheet, config)
      e <- spriteStream("right", sheet, config)
      w <- spriteStream("right", sheet, config, Tile.hflip)
    } yield new MultiSpriteStream(Map("n" -> n, "s" -> s, "e" -> e, "w" -> w))

  object library {
    case class SheetBlock(id: String, tiles: Seq[BufferedImage])

    def init(sc: Config): List[SheetBlock] = {
      import scala.jdk.CollectionConverters._
      val sheetPath = sc.as[String]("path")

      val sheet: BufferedImage = Toolkit.getDefaultToolkit.createImage(
        new FilteredImageSource(
          ImageIO.read(Paths.get(sheetPath).toFile).getSource,
          new RgbF
        )
      )

      sc.getConfigList("blocks")
        .asScala
        .map { cfg =>
          val b = block(
            sheet,
            cfg.getInt("x"),
            cfg.getInt("y"),
            cfg.getInt("w"),
            cfg.getInt("h"),
            cfg.getInt("rows"),
            cfg.getInt("cols")
          )
          SheetBlock(cfg.getString("id"), b)
        }
        .toList
    }

    private def isEmpty(bi: BufferedImage): Boolean = {
      val w = bi.getWidth
      val h = bi.getHeight
      !bi.getRGB(0, 0, w, h, null, 0, w).exists(_ != 0)
    }

    private def block(im: BufferedImage, x: Int, y: Int, w: Int, h: Int, rows: Int, cols: Int): Seq[BufferedImage] = {
      val tiles = for {
        r <- 0 until rows
        c <- 0 until cols
      } yield {
        val bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g2 = bi.getGraphics
        val xx = x + c * w
        val yy = y + r * h
        g2.drawImage(im, 0, 0, w, h, xx, yy, xx + w, yy + h, null)
        g2.dispose()
        bi
      }
      tiles.filterNot(isEmpty)
    }

    private implicit def ToBufferedImage(im: Image): BufferedImage = {
      val w = im.getWidth(null)
      val h = im.getHeight(null)
      val bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
      val g2 = bi.getGraphics
      g2.drawImage(im, 0, 0, w, h, null)
      g2.dispose()
      bi
    }

    private class RgbF extends RGBImageFilter {
      // todo;; transparency colors should be in sheet config
      private val markerRGB =
        List((186, 254, 202), (204, 255, 204), (64, 105, 149), (32, 96, 0), (32, 64, 0))
          .map(rgb => new Color(rgb._1, rgb._2, rgb._3))
          .map(_.getRGB)

      def filterRGB(x: Int, y: Int, rgb: Int): Int =
        if (markerRGB.contains(rgb | 0xFF000000)) 0x00FFFFFF & rgb
        else rgb
    }
  }
}
