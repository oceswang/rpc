package com.galaxy.rpc.common.codec;

import com.galaxy.rpc.common.util.ProtostuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object>
{
	private Class<?> genericClass;
	public RpcEncoder(Class<?> genericClass)
	{
		this.genericClass = genericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception
	{
		if(genericClass.isInstance(msg))
		{
			byte[] data = ProtostuffUtil.serialize(msg);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
		
	}

}
