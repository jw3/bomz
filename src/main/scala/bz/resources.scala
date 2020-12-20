package bz

import java.net.URL

object resources {
  def get(path: String): URL = getClass.getResource(path)

  // need a named set of resources, hacking like so for now
  // todo;; make this a zio service
//  def load()
}
