package com.galaxy.rpc.client;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.common.codec.RpcDecoder;
import com.galaxy.rpc.common.codec.RpcEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>
{
	private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
	final CountDownLatch latch = new CountDownLatch(1);
	private String host;
	private int port;
	private RpcResponse resp;
	public RpcClient(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public RpcResponse send(RpcRequest req)
	{
		EventLoopGroup group = new NioEventLoopGroup();
		try
		{
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception
				{
					ch.pipeline()
					.addLast(new RpcEncoder(RpcRequest.class))
					.addLast(new RpcDecoder(RpcResponse.class))
					.addLast(RpcClient.this);
				}

			}).option(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = bootstrap.connect(host, port).sync();
			f.channel().writeAndFlush(req).sync();

			latch.await();
			if (resp != null)
			{
				f.channel().closeFuture().sync();
			}
			return resp;
		}
		catch (Exception e)
		{

		}
		finally
		{
			group.shutdownGracefully();
		}
		return null;

	}


	protected void messageReceived(ChannelHandlerContext ctx, RpcResponse msg) throws Exception
	{
		resp = msg;
		latch.countDown();

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		logger.error("client caught error", cause);
		ctx.close();
	}
	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public RpcResponse getResp()
	{
		return resp;
	}


}
