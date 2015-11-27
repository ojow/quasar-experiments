package sample

import co.paralleluniverse.fibers.Fiber
import Fibers._

object Main {
  def main(args: Array[String]): Unit = {
    new SimpleNettyServer(req => {
      //Fiber.sleep(500)
      "hello!"
    }).start()
    // val x = (1 to 10000).map { x => fiber { Fiber.sleep(5000); x } }.map(_.get()).sum
  }
}
