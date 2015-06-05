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

import java.io.Serializable;
import java.util.List;

/**
 * The interface for all reader implementations.
 *  
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 */
public interface Writer<T> {
	public void open( Serializable state );
	
	public void close();
	
	public Serializable getState();
	
	/**
	 * Writes a single record.
	 * 
	 * @param record
	 */
	public void write(T record);

	/**
	 * Writes all records in the collection.
	 * 
	 * @param records
	 */
	public void write(List<? extends T> records);
}
