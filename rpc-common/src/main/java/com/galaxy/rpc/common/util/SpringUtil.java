package com.galaxy.rpc.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringUtil implements ApplicationContextAware,BeanPostProcessor 
{
	private static boolean useSpring = false;
	private static Map<String,ApplicationContext> contextMap = new HashMap<>();
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		contextMap.put(applicationContext.getDisplayName(), applicationContext);
	}
	
	public static <T> T  getBean(Class<T> clazz)
	{
		try
		{
			Collection<ApplicationContext> ctxs = contextMap.values();
			for(ApplicationContext ctx : ctxs)
			{
				T bean = ctx.getBean(clazz);
				if(bean != null)
				{
					return bean;
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		useSpring = true;
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		System.err.println("=================================postProcessAfterInitialization=================================");
		return null;
	}

	public static boolean isUseSpring()
	{
		return useSpring;
	}
	
	

}
