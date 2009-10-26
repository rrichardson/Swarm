package swarm

import scala.continuations._ 
import scala.continuations.ControlContext._ 
import scala.continuations.Loops._

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.Exit

import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.remote.CustomObjectInputStream

import java.net._
import java.io._
import java.lang.System;

case class Exec(bee: Unit => Bee);
case class Success(id : Int, result : Ref[Any]);
case class Failure(id : Int, reason : String);

object Swarm extends Actor {
	type swarm = cps[Bee, Bee];
  trapExit = true // (1)
	var myLocation : Node = null;
	val shouldLog = true;

  def listen(port : int)
  {
    myLocation = new Node("localhost", port)
    this.start
  }
  
  def act() {
    RemoteActor.classLoader = getClass().getClassLoader()
    alive(myLocation.port)
    register('Swarm, self)

    while (true) {
			log("Waiting for message");
      receive {
        case Exec(bee) => 
          log("Executing continuation");
          Swarm.run((Unit) => shiftUnit(bee()));
        case Success(id, result) =>
          log("request succeeded");
        case Failure(id, reason) =>
          log("request failed");
			}
	  }
	}
	
	def run(toRun : Unit => Bee @swarm) = {
		execute(reset {
			log("Running task");
			toRun();
      //log("Done with run");
		})
	}
	
	/**
	 * Start a new Swarm task (will return immediately as
	 * task is started in a new thread)
	 */
	def spawn(toRun : Unit => Bee @swarm) = {
		val thread = new Thread {
			override def run() = {
				execute(reset {
					log("Spawning task");
					toRun();
					log("Done with Spawn");
					NoBee()
				})
			}
		};
		thread.start();
	}
	
	def moveTo(location : Node) = shift {
		c: (Unit => Bee) => {
			log("Move to")
			if (Swarm.isLocal(location)) {
				log("Is local")
				c()
			} else {
				log("Moving task to "+location.port);
				IsBee(c, location)
			}
		}
	}
	
	def execute(bee : Bee) = {
		bee match {
			case IsBee(contFunc, location) => {
				log("Transmitting task to "+location.port);
        val rs = select(location, 'Swarm);
        rs ! Exec(contFunc);
				log("Transmission complete");
			}
			case NoBee() => {
				log("No more continuations to execute");
			}
		}
	}

  def isLocal(loc : Node) = {
		loc.equals(myLocation);
	}

	def log(message : String) = {
		if (shouldLog) printf("%d-%d:%s \n", System.currentTimeMillis(), myLocation.port, message);
	}

}
