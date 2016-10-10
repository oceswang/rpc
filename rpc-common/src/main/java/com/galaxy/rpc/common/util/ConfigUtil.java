package com.galaxy.rpc.common.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigUtil
{
	public static Properties getClasspathProp(String name)
	{
		Properties prop = new Properties();
		try
		{
			prop.load(ConfigUtil.class.getClassLoader().getResourceAsStream(name));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop;
	}
}
