package com.galaxy.rpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.common.codec.RpcDecoder;
import com.galaxy.rpc.common.codec.RpcEncoder;
import com.galaxy.rpc.common.util.ClassFilter;
import com.galaxy.rpc.common.util.ClassUtil;
import com.galaxy.rpc.common.util.ConfigUtil;
import com.galaxy.rpc.registry.ServiceRegistry;
import com.galaxy.rpc.registry.factory.ServiceRegistryFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class RpcServer
{

	private Map<String,Object> handlerMap = new HashMap<>();
	private String serverAddress;
	private ServiceRegistry serviceRegistry;
	private String servicePkg;
	private long timeout = 1000L;
	
	public RpcServer()
	{
		Properties config = ConfigUtil.getClasspathProp("rpc.properties");
		serverAddress = config.getProperty("server.address");
		String protocol = config.getProperty("registry.protocol");
		String registryAddress = config.getProperty("registry.address");
		String timeoutStr = config.getProperty("server.timeout");
		if(timeoutStr != null)
		{
			timeout = Long.valueOf(timeoutStr);
		}
		if(serverAddress != null && protocol != null && registryAddress != null)
		{
			serviceRegistry = ServiceRegistryFactory.create(protocol, registryAddress);
		}
		if(serviceRegistry == null)
		{
			throw new RuntimeException("ServiceRegistry can not be initialized. protocol="+protocol+", serverAddress="+serverAddress);
		}
		servicePkg = config.getProperty("service.packages");
		if(!RpcServerInitializer.isUseSpring())
		{
			scanCommonService(servicePkg.split(","));
		}
	}
	
	public void startRpcServer()
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
					.addLast(new RpcServerHandler(handlerMap,timeout));
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

	public void scanCommonService(String... servicePackages) 
	{
		for(String pkg : servicePackages)
		{
			if(pkg == null || pkg.length()==0)
			{
				break;
			}
			ClassUtil.scan(pkg, new ClassFilter(){
				@Override
				public boolean accept(Class<?> clazz)
				{
					RpcService service = clazz.getAnnotation(RpcService.class);
					if(service != null)
					{
						Object bean = null;
						String name = service.value().getName();
						if(bean == null)
						{
							try
							{
								bean = clazz.newInstance();
							} catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						if(bean != null)
						{
							handlerMap.put(name, bean);
							return true;
						}
					}
					return false;
				}
			});
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

	public Map<String, Object> getHandlerMap()
	{
		return handlerMap;
	}

	public void setHandlerMap(Map<String, Object> handlerMap)
	{
		this.handlerMap = handlerMap;
	}

	public String getServicePkg()
	{
		return servicePkg;
	}

}
