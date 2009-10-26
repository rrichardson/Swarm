package swarm

import scala.actors.remote.Node

@serializable abstract class Bee
@serializable case class NoBee() extends Bee
@serializable case class IsBee(contFunc : (Unit => Bee), location : Node) extends Bee
