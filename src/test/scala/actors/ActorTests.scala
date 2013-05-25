package actors

import org.scalatest._

class ActorTests extends FunSpec {
  describe("An actor function") {
    it("Should correctly process a message") {
      def af: actors.ActorFunc[String] = {
	case (_, "test") => actors.ActorResult("test", af)
      }

      assert(af(null, "test").state == "test")
    }
  }

  describe("An actor address") {
    it("Should correctly queue a message and process it in the actor") {
      def af: actors.ActorFunc[String] = {
	case (_, "test") => actors.ActorResult("passed", af)
      }
      
      val actor = new Address(af, "test")
      actor send "test"
      actor.processOne()
      assert(actor.state == "passed")
    }
  }

  describe("A pair of ping-pong actors") {
    it("Should ping-pong correctly") {
      import actors._
      def ping: ActorFunc[String] = {
	case (ActorState(s, _), ("pong", sender: Address[_])) =>
	  sender send ("ping", s)
	  ActorResult("pinged", ping)
      }

      def pong: ActorFunc[String] = {
	case (ActorState(self, _), ("ping", sender: Address[_])) =>
	  sender send ("pong", self)
	  ActorResult("", pong)
      }
      
      val pinger = new Address(ping, "")
      val ponger = new Address(pong, "")
      
      ponger send ("ping", pinger)
      ponger.processOne()
      pinger.processOne()

      assert(pinger.state == "pinged")
    }
  }
}
