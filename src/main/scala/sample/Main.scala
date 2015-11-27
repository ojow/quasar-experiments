package sample

import co.paralleluniverse.fibers.Fiber

object Main {
  def main(args: Array[String]): Unit = {
    new SimpleNettyServer(req => {
      Fiber.sleep(5000)
      "hello!"
    }).start()
  }
}
