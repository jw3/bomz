package bz

import java.awt.Toolkit
import java.awt.image.{BufferedImage, FilteredImageSource, RGBImageFilter}
import java.net.URL
import java.nio.file.Paths

import com.typesafe.config.Config
import javax.imageio.ImageIO
import net.ceedubs.ficus.Ficus._
import zio.{IO, ZIO}

import scala.swing.{Color, Graphics2D, Image}

object sprites {
  trait Tile {
    def w: Int
    def h: Int
    def draw(x: Int, y: Int, g2: Graphics2D): Unit
  }

  def tileFor(image: URL): ZIO[Any, Nothing, Tile] = IO.fromFunction(_ => ImageIO.read(image)).map { im =>
    new Tile {
      def w: Int = im.getWidth
      def h: Int = im.getHeight
      def draw(x: Int, y: Int, g2: Graphics2D): Unit = g2.drawImage(im, x, y, null)
    }
  }

  def tilesFor(images: Seq[URL]): ZIO[Any, Nothing, List[Tile]] = ZIO.foreach(images)(tileFor)

  class Player()

  // single frame - special item
  class SpriteMap

  // multi frame - bomb pulsing
  class SpriteStream

  // multi frame oriented - player walking
  class MultiSpriteStream(map: Map[String, List[Tile]]) {
    def get(k: String): Seq[Tile] =
      LazyList.continually(map(k)).flatten
  }

  def fromPlayerConfig(config: Config): ZIO[Any, Unit, MultiSpriteStream] =
    for {
      n <- ZIO.fromOption(config.getAs[List[String]]("up")).map(x => x.map(resources.get)).flatMap(tilesFor)
      s <- ZIO.fromOption(config.getAs[List[String]]("down")).map(x => x.map(resources.get)).flatMap(tilesFor)
      e <- ZIO.fromOption(config.getAs[List[String]]("left")).map(x => x.map(resources.get)).flatMap(tilesFor)
      w <- ZIO.fromOption(config.getAs[List[String]]("right")).map(x => x.map(resources.get)).flatMap(tilesFor)
      m = Map("n" -> n, "s" -> s, "e" -> e, "w" -> w)
    } yield new MultiSpriteStream(m)

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
