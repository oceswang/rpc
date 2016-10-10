package com.galaxy.rpc.registry.factory;

import com.galaxy.rpc.common.util.Constants;
import com.galaxy.rpc.registry.ServiceRegistry;
import com.galaxy.rpc.registry.zookeeper.ZooKeeperServiceRegistry;

public class ServiceRegistryFactory
{
	public static ServiceRegistry create(String protocol, String registryAddress)
	{
		if(Constants.PROTOCOL_ZK.equalsIgnoreCase(protocol))
		{
			return new ZooKeeperServiceRegistry(registryAddress);
		}
		else if(Constants.PROTOCOL_REDIS.equalsIgnoreCase(protocol))
		{
			
		}
		return null;
	}
}
