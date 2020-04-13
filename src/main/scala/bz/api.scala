package bz

object api {

  sealed trait MoveCommand
  case object Up extends MoveCommand
  case object Down extends MoveCommand
  case object Left extends MoveCommand
  case object Right extends MoveCommand
}
