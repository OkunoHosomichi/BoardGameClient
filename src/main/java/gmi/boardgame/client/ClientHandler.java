package gmi.boardgame.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;

public class ClientHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {
    for (int i = 0; i < msgs.size(); i++) {
      System.out.println(msgs.get(i));
    }
    msgs.releaseAllAndRecycle();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.err.println("Unexpected exception from downstream.");
    ctx.close();
  }

}
