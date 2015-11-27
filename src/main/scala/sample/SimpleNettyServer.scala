package sample

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders.Values
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpHeaders, HttpRequest, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import Fibers._

class SimpleNettyServer(reqHandler: HttpRequest => String) {
  def start(): Unit = {
    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup()
    try {
      val b = new ServerBootstrap()
      b.option(ChannelOption.SO_BACKLOG, Integer.valueOf(10240))
      b.group(bossGroup, workerGroup).
        channel(classOf[NioServerSocketChannel]).
        //handler(new LoggingHandler(LogLevel.INFO)).
        childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            val p = ch.pipeline()
            p.addLast(new HttpServerCodec())
            p.addLast(new ChannelInboundHandlerAdapter  {
              override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
                cause.printStackTrace()
                ctx.close()
              }
              override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = msg match {
                case req: HttpRequest =>
                  if (HttpHeaders.is100ContinueExpected(req)) {
                    ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE))
                  }
                  fiber {
                    val keepAlive = HttpHeaders.isKeepAlive(req)
                    val response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                      Unpooled.wrappedBuffer(reqHandler(req).getBytes))
                    response.headers().set(CONTENT_TYPE, "text/plain")
                    response.headers().set(CONTENT_LENGTH, response.content().readableBytes())
                    if (!keepAlive) {
                      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
                    }
                    else {
                      response.headers().set(CONNECTION, Values.KEEP_ALIVE)
                      ctx.writeAndFlush(response)
                    }
                  }
                case _ =>
              }
            })
          }
        })

      val ch = b.bind(8080).sync().channel()
      ch.closeFuture().sync()
    }
    finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }

  }
}
