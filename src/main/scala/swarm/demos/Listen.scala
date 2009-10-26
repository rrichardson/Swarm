package swarm.demos;

import swarm._;
import swarm.Swarm;

object Listen {
	def main(args : Array[String]) = {
		Swarm.listen(java.lang.Short.parseShort(args(0)));
		while(true) {
			Thread.sleep(1000);
		}
	}
}