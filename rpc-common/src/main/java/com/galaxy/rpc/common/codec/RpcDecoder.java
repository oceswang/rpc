package com.galaxy.rpc.common.codec;

import java.util.List;

import com.galaxy.rpc.common.util.ProtostuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder
{
	private Class<?> genericClass;
	public RpcDecoder(Class<?> genericClass)
	{
		this.genericClass = genericClass;
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
	{
		if(in.readableBytes()<4)
		{
			return;
		}
		in.markReaderIndex();
		int len = in.readInt();
		if(len<0)
		{
			ctx.close();
		}
		if(in.readableBytes() < len)
		{
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[len];
		in.readBytes(data);
		Object obj = ProtostuffUtil.deserialize(data, genericClass);
		out.add(obj);
	}

}
