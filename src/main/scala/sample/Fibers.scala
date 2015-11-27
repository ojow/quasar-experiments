package sample

import co.paralleluniverse.fibers.{SuspendExecution, Fiber}

object Fibers {
  def fiber[T](body: => T) = new Fiber[T]() {
    @throws[SuspendExecution] override def run() = body
  }.start()
}
