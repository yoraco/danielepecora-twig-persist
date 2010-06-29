package com.vercer.engine.persist;

import junit.framework.Assert;

import org.junit.Test;

import com.vercer.engine.persist.annotation.AnnotationObjectDatastore;
import com.vercer.engine.persist.annotation.Id;
import com.vercer.engine.persist.annotation.Key;

@SuppressWarnings("deprecation")
public class AutoGeneratedIdTest extends LocalDatastoreTestCase
{
	public static class HasStringKey
	{
		@Key String keyField;
	}
	public static class HasIntKey
	{
		@Id int keyField;
	}

	public static class HasLongKey
	{
		@Id Long keyField;
	}
	
	@Test
	public void missingStringField()
	{
		ObjectDatastore datastore = new AnnotationObjectDatastore();
		
		HasStringKey hasNullKey = new HasStringKey();
		datastore.store(hasNullKey);
		
		Assert.assertNotNull(hasNullKey.keyField);
	}

	@Test
	public void missingIntField()
	{
		ObjectDatastore datastore = new AnnotationObjectDatastore();
		
		HasIntKey hasNullKey = new HasIntKey();
		datastore.store(hasNullKey);
		
		Assert.assertTrue(hasNullKey.keyField > 0);
	}

	@Test
	public void missingLongField()
	{
		ObjectDatastore datastore = new AnnotationObjectDatastore();
		
		HasLongKey hasNullKey = new HasLongKey();
		datastore.store(hasNullKey);
		
		Assert.assertNotNull(hasNullKey.keyField);
	}
	
	
}
