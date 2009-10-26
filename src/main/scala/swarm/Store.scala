package swarm

import scala.collection.mutable._

object Store {
	var nextUid = 0;
	
	val map = new HashMap[Long, Any]();
	
	def get[T](t : Class[T], key : Long) : Option[T] = {
		map.get(key).asInstanceOf[Option[T]];
	}
	
	def save(value : Any) : Long = {
    println("storing value")
		val uid = nextUid;
		nextUid+=1;
		update(uid, value);
		return uid;
	}
	
	def update(key : Long, value : Any) : Unit = {
		map.put(key, value);
	}

	def updateWith[T](key : Long, f: Option[T] => T) : Unit = {
      map.put(key, f(Some(map.get(key).asInstanceOf[T])))
  }
}
