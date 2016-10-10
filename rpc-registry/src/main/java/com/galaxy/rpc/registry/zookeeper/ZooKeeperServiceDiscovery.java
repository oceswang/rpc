package com.galaxy.rpc.registry.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxy.rpc.common.util.Constants;
import com.galaxy.rpc.registry.ServiceDiscovery;
public class ZooKeeperServiceDiscovery implements ServiceDiscovery
{
	private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);
	final CountDownLatch latch = new CountDownLatch(1);
	private volatile List<String> dataList = new ArrayList<>();
	
	private String registryAddress;
	
	public ZooKeeperServiceDiscovery(String registryAddress)
	{
		this.registryAddress = registryAddress;
		ZooKeeper zk = connectServer();
		if(zk != null)
		{
			watchNode(zk);
		}
	}
	
	public String discover()
	{
		String data = null;
		int size = dataList.size();
		if(size > 0)
		{
			if(size == 1)
			{
				data = dataList.get(0);
				logger.debug("use only data:{}",data);
			}
			else
			{
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				logger.debug("use random data:{}",data);
				
			}
		}
		
		return data;
	}
	private ZooKeeper connectServer()
	{
		ZooKeeper zk = null;
		try
		{
			zk = new ZooKeeper(getRegistryAddress(), Constants.ZK_SESSION_TIMEOUT, new Watcher() {
				public void process(WatchedEvent event)
				{
					if (event.getState().equals(Event.KeeperState.SyncConnected))
					{
						latch.countDown();
					}
				}
			});
			latch.await();
		}
		catch (Exception e)
		{
			logger.error("Failure to connect server "+registryAddress,e);
		}
		return zk;
	}
	private void watchNode(ZooKeeper zk)
	{
		try
		{
			List<String> nodes = zk.getChildren(Constants.ZK_REGISTRY_PATH, new Watcher(){

				@Override
				public void process(WatchedEvent event)
				{
					if(event.getType() == Event.EventType.NodeChildrenChanged)
					{
						watchNode(zk);
					}
				}
			});
			
			List<String> dataList = new ArrayList<>();
			for(String node : nodes)
			{
				byte[] bytes = zk.getData(Constants.ZK_REGISTRY_PATH+"/"+node, false, null);
				dataList.add(new String(bytes));
			}
			this.dataList = dataList;
			
		}
		catch (Exception e)
		{
			logger.error("watch node error ", e);
		}
		
	}

	public String getRegistryAddress()
	{
		return registryAddress;
	}
	public void setRegistryAddress(String registryAddress)
	{
		this.registryAddress = registryAddress;
	}
	
	
}
