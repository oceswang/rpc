package com.galaxy.rpc.server;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.common.util.ThreadUtil;
import com.galaxy.rpc.remoting.RemotingException;
import com.galaxy.rpc.remoting.exchange.RpcFuture;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest>
{
	private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
	private static final ExecutorService exeServcie = ThreadUtil.newFixedThreadPool(200, "rpc-server-pool");
	private long timeout;
	private Map<String,Object> handleMap;
	

	public RpcServerHandler(Map<String, Object> handleMap, long timeout)
	{
		super();
		this.handleMap = handleMap;
		this.timeout = timeout;
	}
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, RpcRequest req) throws Exception
	{
		RpcResponse resp;
		try
		{
			resp = handle(req);
		} catch (Exception e)
		{
			resp = new RpcResponse();
			resp.setRequestId(req.getRequestId());
			resp.setError(e);
		}
		ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
	}
	private RpcResponse handle(RpcRequest req) throws Exception
	{
		
		RpcFuture future = new RpcFuture(req,timeout);
		exeServcie.submit(new Runnable(){
			@Override
			public void run()
			{
				RpcResponse resp = new RpcResponse();
				resp.setRequestId(req.getRequestId());
				try
				{
					String className = req.getClassName();
					String methodName = req.getMethodName();
					Class<?>[] paramTypes = req.getParamTypes();
					Object[] params = req.getParams();
					
					Object bean = handleMap.get(className);
					Class<?> serviceClass = bean.getClass();
					Method method = serviceClass.getMethod(methodName, paramTypes);
					Object result = method.invoke(bean, params);
					resp.setResult(result);
				} catch (Exception e)
				{
					resp.setError(e);
				} 
				future.done(resp);
			}
			
		}, future);
		
		try
		{
			long start = System.currentTimeMillis();
			RpcResponse resp = future.get();
			System.err.println("==========================time "+(System.currentTimeMillis()-start));
			return resp;
		} catch (RemotingException e)
		{
			throw new Exception(e);
		}
		
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		logger.error("Server caught error",cause);
		ctx.close();
	}
	

}
