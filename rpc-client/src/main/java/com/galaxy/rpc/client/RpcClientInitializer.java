package com.galaxy.rpc.client;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class RpcClientInitializer implements BeanPostProcessor
{
	private RpcProxy proxy = new RpcProxy();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
	{
		// TODO Auto-generated method stub
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		try
		{
			if (bean != null)
			{
				Field[] fields = bean.getClass().getDeclaredFields();
				for (Field field : fields)
				{
					RpcReference ref = field.getAnnotation(RpcReference.class);
					if (ref != null)
					{
						if (!field.isAccessible())
						{
							field.setAccessible(true);
						}
						field.set(bean, proxy.create(field.getType()));
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return bean;
	}

}
