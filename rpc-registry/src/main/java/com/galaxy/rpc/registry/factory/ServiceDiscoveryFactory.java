package com.galaxy.rpc.registry.factory;

import com.galaxy.rpc.common.util.Constants;
import com.galaxy.rpc.registry.ServiceDiscovery;
import com.galaxy.rpc.registry.zookeeper.ZooKeeperServiceDiscovery;

public class ServiceDiscoveryFactory
{
	public static ServiceDiscovery create(String protocol, String registryAddress)
	{
		if(Constants.PROTOCOL_ZK.equalsIgnoreCase(protocol))
		{
			return new ZooKeeperServiceDiscovery(registryAddress);
		}
		else if(Constants.PROTOCOL_REDIS.equalsIgnoreCase(protocol))
		{
			
		}
		return null;
	}
}
