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

package com.ibm.doctorbatch.magicsauce.mvs;

import java.util.Properties;

import com.ibm.etools.marshall.RecordBytes;

public interface DataSetManager<T extends RecordBytes> {

	public abstract RecordBytesParser<T> getRecordParser();

	public abstract void initialize(Properties props);

	public abstract void initialize(Properties props, boolean restart);

	public abstract void flush();

	public abstract long getCurrentPosition();

	public abstract void setPosition(long position);

	/**
	 * Allocates a dynamic DD card using the DD_ALLOC (or DD_ALLOC_RESTART) statement,
	 * and then opens the file using the ds_parameters (or ds_parameters_restart) options, 
	 * provided in the xJCL. 
	 */
	public abstract void open();

	public abstract void close();

}