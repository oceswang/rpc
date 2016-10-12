package com.galaxy.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService
{
	public Class<?> value();
	public String version() default "1.0";
	public long timeout() default 1000L;
}
