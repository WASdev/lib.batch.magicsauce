/*
 * Copyright 2015 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.doctorbatch.magicsauce.auto;

import java.util.Properties;

import com.ibm.doctorbatch.magicsauce.BatchException;
import com.ibm.doctorbatch.magicsauce.ClassUtil;
import com.ibm.doctorbatch.magicsauce.MagicSauceLogger;
import com.ibm.doctorbatch.magicsauce.RecordParser;

/**
 * The AutoRecordParser is a reusable implementation of the {@link com.ibm.doctorbatch.magicsauce.RecordParser}
 * interface which uses introspection to instantiate it's RecordParser instance. If your record object implements 
 * @{link com.ibm.doctorbatch.magicsauce.auto.BatchRecord}, then you can use an AutoRecordParser with 
 * batchframework-websphere's ETLJobStep or batchframework-java's ETLProcessor implementations by specifying the 
 * RECORD_PARSER_CLASSNAME property to your job step's properties.
 *  
 * @see RecordParser
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@tobat.ch)
 *
 * @param <ObjectType> The type for the in-memory representation of your record structure
 * @param <RecordType> The type for the serialized representation of your record structure
 */
public class AutoRecordParser<ObjectType extends BatchRecord<RecordType>, RecordType> 
	implements RecordParser<ObjectType, RecordType > 
{
	private String className;
	private Class<?> clazz;
	
	private final static String AUTO_RECORD_PARSER_CLASSNAME = "AUTO_RECORD_PARSER_CLASSNAME";
	
	@Override
	public void initialize(Properties props) {
		className = props.getProperty(AUTO_RECORD_PARSER_CLASSNAME);
		
		MagicSauceLogger.getInstance().trace("Instantiate class specified by "+AUTO_RECORD_PARSER_CLASSNAME+": " + className);
		clazz = ClassUtil.getClass(className);
		
		try {
			Object o = ClassUtil.getInstanceForClass( clazz );
			
			@SuppressWarnings({ "unused", "unchecked" })
			ObjectType ot = (ObjectType) o;
		} catch ( ClassCastException cce ) {
			MagicSauceLogger.getInstance().exception(
					"Objects of type " + clazz.getCanonicalName() +  ", specified by "+AUTO_RECORD_PARSER_CLASSNAME+", must inherit from " + BatchRecord.class.getCanonicalName(),
					cce );
			
			throw new BatchException(
					"Objects of type " + clazz.getCanonicalName() +  ", specified by "+AUTO_RECORD_PARSER_CLASSNAME+", must inherit from " + BatchRecord.class.getCanonicalName(), 
					cce );
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObjectType parseRecordToObject(RecordType record) {
		ObjectType rec = null;

		rec = (ObjectType) ClassUtil.getInstanceForClass(clazz);
		rec.setRecordData( record );
		return rec;
	}

	@Override
	public RecordType parseObjectToRecord(ObjectType object) {
		return object.getRecordData();
	}

}
