package com.galaxy.rpc.server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.common.codec.RpcDecoder;
import com.galaxy.rpc.common.codec.RpcEncoder;
import com.galaxy.rpc.registry.ServiceRegistry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcServer implements ApplicationContextAware, InitializingBean
{

	private Map<String,Object> handlerMap = new HashMap<>();
	private String serverAddress;
	private ServiceRegistry serviceRegistry;
	
	@Override
	public void afterPropertiesSet() throws Exception
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try
		{
			ServerBootstrap server = new ServerBootstrap();
			server.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>(){
				@Override
				protected void initChannel(SocketChannel ch) throws Exception
				{
					ch.pipeline()
					.addLast(new RpcDecoder(RpcRequest.class))
					.addLast(new RpcEncoder(RpcResponse.class))
					.addLast(new RpcServerHandler(handlerMap));
				}
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.option(ChannelOption.SO_KEEPALIVE, true);
			
			String[] attr = serverAddress.split(":");
			String host = attr[0];
			int port = Integer.valueOf(attr[1]);
			ChannelFuture f = server.bind(host, port).sync();
			if(serviceRegistry != null)
			{
				serviceRegistry.register(serverAddress);
			}
			f.channel().closeFuture().sync();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		Map<String,Object> beanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
		if(beanMap != null && beanMap.size() > 0)
		{
			for(Object bean : beanMap.values())
			{
				String name = bean.getClass().getAnnotation(RpcService.class).value().getName();
				handlerMap.put(name, bean);
			}
		}
	}

	public String getServerAddress()
	{
		return serverAddress;
	}

	public void setServerAddress(String serverAddress)
	{
		this.serverAddress = serverAddress;
	}

	public ServiceRegistry getServiceRegistry()
	{
		return serviceRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	
}
