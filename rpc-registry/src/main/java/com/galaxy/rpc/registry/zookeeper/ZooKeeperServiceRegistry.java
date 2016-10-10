package com.galaxy.rpc.registry.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.galaxy.rpc.common.util.Constants;
import com.galaxy.rpc.registry.ServiceRegistry;
public class ZooKeeperServiceRegistry implements ServiceRegistry
{
	private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);
	final CountDownLatch latch = new CountDownLatch(1);
	private String registryAddress;

	public ZooKeeperServiceRegistry(String registryAddress)
	{
		this.registryAddress = registryAddress;
	}
	
	public void register(String data)
	{
		if(data != null)
		{
			ZooKeeper zk = connectServer();
			if(zk != null)
			{
				createRoot(zk);
				createNode(zk,data);
			}
			
		}
	}
	private ZooKeeper connectServer()
	{
		ZooKeeper zk = null;
		try
		{
			zk = new ZooKeeper(registryAddress, Constants.ZK_SESSION_TIMEOUT, new Watcher() {
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
	private void createRoot(ZooKeeper zk)
	{
		try
		{
			Stat stat = zk.exists(Constants.ZK_REGISTRY_PATH, false);
			if(stat == null)
			{
				zk.create(Constants.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
		catch (Exception e)
		{
			logger.error("Create root error",e);
		}
	}
	private void createNode(ZooKeeper zk, String data)
	{
		byte[] bytes = data.getBytes();
		try
		{
			String path = zk.create(Constants.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			logger.debug("create zookeeper node ({} => {})", path, data);
		}
		catch (Exception e)
		{
			logger.error("Create node "+data,e);
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
