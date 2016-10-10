package com.galaxy.rpc.server;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class RpcServerInitializer
		implements InstantiationAwareBeanPostProcessor, ApplicationListener<ContextRefreshedEvent>
{
	private static boolean useSpring = false;
	private Map<String, Object> serviceMap = new HashMap<>();
	private RpcServer rpcServer = new RpcServer();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		return bean;
	}

	public static boolean isUseSpring()
	{
		return useSpring;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException
	{
		useSpring = true;
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException
	{
		if(bean != null)
		{
			RpcService service = bean.getClass().getAnnotation(RpcService.class);
			if(service != null)
			{
				String name = service.value().getName();
				String pkgs = rpcServer.getServicePkg();
				if(pkgs != null)
				{
					String[] packages = pkgs.split(",");
					for(String pkg : packages)
					{
						if(pkg != null && bean.getClass().getPackage().getName().indexOf(pkg)!=-1)
						{
							serviceMap.put(name, bean);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException
	{
		return null;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		System.err.println("===================onApplicationEvent " + serviceMap + "=====================");
		rpcServer.setHandlerMap(serviceMap);
		rpcServer.startRpcServer();
	}

}
