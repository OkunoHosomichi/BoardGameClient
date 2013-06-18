package gmi.boardgame.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientApp {
  private final String host;
  private final int port;

  public ClientApp(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {

        @Override
        protected void initChannel(Channel ch) throws Exception {
          ChannelPipeline pipeline = ch.pipeline();

          // Add the text line codec combination first,
          pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
          pipeline.addLast("decoder", new StringDecoder());
          pipeline.addLast("encoder", new StringEncoder());

          // and then business logic.
          pipeline.addLast("handler", new ClientHandler());
        }
      });

      // Start the connection attempt.
      Channel ch = b.connect(host, port).sync().channel();

      // Read commands from the stdin.
      ChannelFuture lastWriteFuture = null;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      for (;;) {
        String line = in.readLine();
        if (line == null) {
          break;
        }

        // Sends the received line to the server.
        lastWriteFuture = ch.write(line + "\r\n");

        // If user typed the 'bye' command, wait until the server closes
        // the connection.
        if ("bye".equals(line.toLowerCase())) {
          ch.closeFuture().sync();
          break;
        }
      }

      // Wait until all messages are flushed before closing the channel.
      if (lastWriteFuture != null) {
        lastWriteFuture.sync();
      }
    } finally {
      // The connection is closed automatically on shutdown.
      group.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws Exception {
    String host;
    int port;

    if (args.length == 2) {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } else {
      host = "localhost";
      port = 8443;
    }

    new ClientApp(host, port).run();
  }
}
