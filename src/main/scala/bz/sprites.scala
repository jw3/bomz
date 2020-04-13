package bz

import java.net.URL

import com.typesafe.config.Config
import javax.imageio.ImageIO
import net.ceedubs.ficus.Ficus._
import zio.{IO, ZIO}

import scala.swing.Graphics2D

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
}
