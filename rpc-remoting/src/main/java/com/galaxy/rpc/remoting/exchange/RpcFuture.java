package com.galaxy.rpc.remoting.exchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.galaxy.rpc.common.bean.RpcRequest;
import com.galaxy.rpc.common.bean.RpcResponse;
import com.galaxy.rpc.remoting.RemotingException;
import com.galaxy.rpc.remoting.TimeoutException;

public class RpcFuture
{
	private static final Map<String, RpcFuture> FUTURES = new ConcurrentHashMap<>();
	private final Lock lock = new ReentrantLock();
	private final Condition done = lock.newCondition();
	private RpcResponse response;
	private String id;
	private long timeout;
	private boolean cancelled;

	public RpcFuture(RpcRequest request, long timeout)
	{
		this.id = request.getRequestId();
		this.timeout = timeout;
		FUTURES.put(id, this);
	}

	public boolean cancel(boolean mayInterruptIfRunning)
	{
		response = new RpcResponse();
		response.setError(new Throwable("Request future has been canceled."));
		FUTURES.remove(id);
		cancelled = true;
		return true;
	}

	public boolean isCancelled()
	{
		return cancelled;
	}

	public boolean isDone()
	{
		return response != null;
	}

	public RpcResponse get() throws RemotingException
	{
		return get(timeout, TimeUnit.MILLISECONDS);
	}

	public RpcResponse get(long timeout, TimeUnit unit) throws RemotingException
	{
		if(!isDone())
		{
			lock.lock();
			try
			{
				long start = System.currentTimeMillis();
				while(!isDone())
				{
					done.await(timeout, unit);
					if(isDone() || System.currentTimeMillis()-start>timeout)
					{
						break;
					}
				}
			} catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				lock.unlock();
			}
			if(!isDone())
			{
				String msg = String.format("Servert timeout, Request id= %s, timeout=%d.", id, timeout);
				throw new TimeoutException(msg);
			}
		}
		return response;
	}
	
	public void done(RpcResponse resp)
	{
		lock.lock();
		try
		{
			this.response = resp;
			if(done != null)
			{
				done.signal();
			}
		} 
		finally
		{
			lock.unlock();
		}
	}

}
