package com.vercer.convert;


public class StringArrayToString implements Converter<String[], String>
{

	@Override
	public String convert(String[] source)
	{
		if (source.length == 1)
		{
			return source[0];
		}
		else
		{
			return null;
		}
	}

}
