package com.galaxy.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest>
{
	private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
	private Map<String,Object> handleMap;
	

	public RpcServerHandler(Map<String, Object> handleMap)
	{
		super();
		this.handleMap = handleMap;
	}
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RpcRequest req) throws Exception
	{
		RpcResponse resp = new RpcResponse();
		resp.setRequestId(req.getRequestId());
		
		try
		{
			Object result = handle(req);
			resp.setResult(result);
		}
		catch (Throwable e)
		{
			resp.setError(e);
		}
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
		
		
	}
	private Object handle(RpcRequest req) throws Throwable
	{
		String className = req.getClassName();
		String methodName = req.getMethodName();
		Class<?>[] paramTypes = req.getParamTypes();
		Object[] params = req.getParams();
		
		Object bean = handleMap.get(className);
		Class<?> serviceClass = bean.getClass();
		Method method = serviceClass.getMethod(methodName, paramTypes);
		return method.invoke(bean, params);
		
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		logger.error("Server caught error",cause);
		ctx.close();
	}
	

}
