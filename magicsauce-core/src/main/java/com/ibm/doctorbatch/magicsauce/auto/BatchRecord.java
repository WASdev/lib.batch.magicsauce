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

/**
 * To use the AutoRecordParser, your ObjectType must implement this BatchRecord interface.
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@tobat.ch)
 * @param <RecordType> The type of the serialized form of the record ... (e.g., byte[], or String)
 */
public interface BatchRecord<RecordType> {
	/**
	 * Sets the record data for this BatchRecord instance. 
	 * 
	 * @param data
	 */
	public void setRecordData( RecordType data );
	
	/**
	 * Returns the record data for this BatchRecord instance.
	 * 
	 * @return
	 */
	public RecordType getRecordData();
}
