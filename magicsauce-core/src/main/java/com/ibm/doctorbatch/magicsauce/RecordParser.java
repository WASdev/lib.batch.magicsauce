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

package com.ibm.doctorbatch.magicsauce;

import java.util.Properties;

/**
 * <p>RecordParser is used to convert a record between its serialized and in-memory 
 * representations. For example, an instance declared as <code>RecordParser&lt;Customer,String&gt;</code> 
 * has two methods: <code>public Customer parseRecordToObject( String record )</code>, in which you 
 * would implement logic to parse the string into a new Customer object instance; and 
 * <code>public String parseObjectToRecord( Customer record )</code> in which you translate the 
 * Customer instance back into a String for serialization.</p> 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 *
 * @version 1.0

 * @param <ObjectType> The type of the object that represents a record in memory ... eg: Customer
 * @param <RecordType> The type of the serialized form of the record ... (e.g., byte[], or String)
 */
public interface RecordParser<ObjectType,RecordType> {
	/**
	 * Initialize this record parser, if needed.
	 * @param props
	 */
	public void initialize( Properties props );

	/**
	 * Returns an object of type T initialized from the given byte[].
	 * 
	 * @param An ObjectType instance representing the record
	 * @return
	 */
	public ObjectType parseRecordToObject( RecordType record );
	
	/**
	 * Returns a RecordType instance created from the given object.
	 * 
	 * @param record
	 * @return
	 */
	public RecordType parseObjectToRecord(ObjectType object);
}
