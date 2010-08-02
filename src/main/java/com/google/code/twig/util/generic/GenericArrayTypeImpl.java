/*
 * Copied from Gentyref project http://code.google.com/p/gentyref/
 * Code was reformatted and moved to fit package structure
 */
package com.google.code.twig.util.generic;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

class GenericArrayTypeImpl implements GenericArrayType
{
	private Type componentType;

	static Class<?> createArrayType(Class<?> componentType)
	{
		// there's no (clean) other way to create a array class, then create an
		// instance of it
		return Array.newInstance(componentType, 0).getClass();
	}

	static Type createArrayType(Type componentType)
	{
		if (componentType instanceof Class<?>)
		{
			return createArrayType((Class<?>) componentType);
		}
		else
		{
			return new GenericArrayTypeImpl(componentType);
		}
	}

	private GenericArrayTypeImpl(Type componentType)
	{
		super();
		this.componentType = componentType;
	}

	public Type getGenericComponentType()
	{
		return componentType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GenericArrayType))
			return false;
		return componentType.equals(((GenericArrayType) obj).getGenericComponentType());
	}

	@Override
	public int hashCode()
	{
		return componentType.hashCode() * 7;
	}

	@Override
	public String toString()
	{
		return componentType + "[]";
	}
}