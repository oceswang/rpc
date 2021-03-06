package com.galaxy.rpc.common.util;

public class Constants
{
	public static final int ZK_SESSION_TIMEOUT = 5000;
	public static final String ZK_REGISTRY_PATH = "/registry";
	public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH+"/data";
	public static final String PROTOCOL_ZK = "zookeeper";
	public static final String PROTOCOL_REDIS = "redis";
	
	public static final String CATEGORY_PROVIDERS="providers";
	public static final String CATEGORY_CONSUMERS="consumers";
}
