package actors

import java.util.concurrent.ConcurrentLinkedQueue

class Address[T](func: actors.ActorFunc[T],
	       initial: T) {
  private [actors] val mailbox = 
    new ConcurrentLinkedQueue[Any]()

  private var nextFunc = func
  private [actors] var state = initial

  def send(msg: Any) = mailbox.add(msg)

  def processOne() {
    val actors.ActorResult(s, f) = 
      nextFunc(actors.ActorState(this, state), mailbox.poll())
    nextFunc = f
    state = s
  }
}

object actors {
  type ActorFunc[T] = 
    (ActorState[T], Any) => ActorResult[T]

  case class ActorResult[T](state: T, nextFunc: ActorFunc[T])
  case class ActorState[T](self: Address[T], state: T)
}
