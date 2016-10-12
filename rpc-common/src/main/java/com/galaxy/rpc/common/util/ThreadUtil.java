package com.galaxy.rpc.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtil
{
	private static final class RpcNameThreadFactory implements ThreadFactory
	{
		private static AtomicInteger seq = new AtomicInteger();
		private String poolName;
		public RpcNameThreadFactory(String poolName)
		{
			this.poolName = poolName;
		}
		@Override
		public Thread newThread(Runnable r)
		{
			String tname = poolName+"-"+seq.getAndIncrement();
			ThreadGroup group = null;
			SecurityManager s = System.getSecurityManager();
			if( s != null)
			{
				group = s.getThreadGroup();
			}
			else
			{
				group = Thread.currentThread().getThreadGroup();
			}
			Thread t = new Thread(group,r,tname);
			return t;
		}
	}
	
	private static final class RpcAbortPolicy implements RejectedExecutionHandler
	{
		private final String poolName;
		public RpcAbortPolicy(String poolName)
		{
			this.poolName = poolName;
		}
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e)
		{
			String msg = String.format("Thread pool is EXHAUSTED!" +
	                " Pool Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
	                " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)!" ,
	                poolName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
	                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
			 throw new RejectedExecutionException(msg);
		}
	}
	
	
	public static ExecutorService newFixedThreadPool(int size,String name)
	{
		return new ThreadPoolExecutor(
				size, 
				size,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new RpcNameThreadFactory(name),
				new RpcAbortPolicy(name));
	}
}
