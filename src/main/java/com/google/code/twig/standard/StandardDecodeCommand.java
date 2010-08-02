package com.google.code.twig.standard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.code.twig.Path;
import com.google.code.twig.Property;
import com.google.code.twig.Restriction;
import com.google.code.twig.util.PropertySets;
import com.google.code.twig.util.RestrictionToPredicateAdaptor;
import com.google.common.collect.Sets;

class StandardDecodeCommand extends StandardCommand
{
	StandardDecodeCommand(StrategyObjectDatastore datastore)
	{
		super(datastore);
	}
	
	@SuppressWarnings("unchecked")
	final <T> T entityToInstance(Entity entity, Restriction<Property> predicate)
	{
		T instance = (T) datastore.keyCache.getInstance(entity.getKey());
		if (instance == null)
		{
			// push a new context
			Key existingDecodeKey = datastore.decodeKey;
			datastore.decodeKey = entity.getKey();

			Type type = datastore.fieldStrategy.kindToType(entity.getKind());

			Set<Property> properties = PropertySets.create(entity.getProperties(), datastore.indexed);
			
			// filter out unwanted properties at the lowest level
			if (predicate != null)
			{
				properties = Sets.filter(properties, new RestrictionToPredicateAdaptor<Property>(predicate));
			}

			// order the properties for efficient separation by field
			properties = new TreeSet<Property>(properties);

			instance = (T) datastore.decoder(entity).propertiesToTypesafe(properties, Path.EMPTY_PATH, type);
			if (instance == null)
			{
				throw new IllegalStateException("Could not translate entity " + entity);
			}

			// pop the context
			datastore.decodeKey = existingDecodeKey;
		}

		return instance;
	}
	

	final <T> Iterator<T> entitiesToInstances(final Iterator<Entity> entities, final Restriction<Property> filter)
	{
		return new Iterator<T>()
		{
			@Override
			public boolean hasNext()
			{
				return entities.hasNext();
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next()
			{
				return (T) entityToInstance(entities.next(), filter);
			}

			@Override
			public void remove()
			{
				entities.remove();
			}
		};
	}


	@SuppressWarnings("unchecked")
	<T> T keyToInstance(Key key, Restriction<Property> filter)
	{
		T instance = (T) datastore.keyCache.getInstance(key);
		if (instance == null)
		{
			Entity entity = keyToEntity(key);
			if (entity == null)
			{
				instance = null;
			}
			else
			{
				instance = (T) entityToInstance(entity, filter);
			}
		}

		return instance;
	}
	
	@SuppressWarnings("unchecked")
	final <T> Map<Key, T> keysToInstances(Collection<Key> keys, Restriction<Property> filter)
	{
		Map<Key, T> result = new HashMap<Key, T>(keys.size());
		List<Key> missing = null;
		for (Key key : keys)
		{
			T instance = (T) datastore.keyCache.getInstance(key);
			if (instance != null)
			{
				result.put(key, instance);
			}
			else
			{
				if (missing == null)
				{
					missing = new ArrayList<Key>(keys.size());
				}
				missing.add(key);
			}
		}
		
		if (!missing.isEmpty())
		{
			Map<Key, Entity> entities = keysToEntities(missing);
			if (!entities.isEmpty())
			{
				Set<Entry<Key, Entity>> entries = entities.entrySet();
				for (Entry<Key, Entity> entry : entries)
				{
					T instance = (T) entityToInstance(entry.getValue(), filter);
					result.put(entry.getKey(), instance);
				}
			}
		}

		return result;
	}

	final Entity keyToEntity(Key key)
	{
		if (datastore.getActivationDepth() > 0)
		{
			try
			{
				return datastore.serviceGet(key);
			}
			catch (EntityNotFoundException e)
			{
				return null;
			}
		}
		else
		{
			// don't load entity if it will not be activated - but need one for key
			return new Entity(key);
		}
	}
	
	final Map<Key, Entity> keysToEntities(Collection<Key> keys)
	{
		// only load entity if we will activate instance
		if (datastore.getActivationDepth() > 0)
		{
			return datastore.serviceGet(keys);
		}
		else
		{
			// we must return empty entities with the correct kind to instantiate
			HashMap<Key, Entity> result = new HashMap<Key, Entity>();
			for (Key key : keys)
			{
				result.put(key, new Entity(key));
			}
			return result;
		}
	}
}