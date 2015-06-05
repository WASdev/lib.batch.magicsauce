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

/**
 * SkipRecordException is an unchecked runtime exception that encapsulates a 
 * record object. It is thrown during a batch process to indicate that an input
 * record is not being processed. When used in conjunction with AbstractSkipRecordJobStep
 * job step implementations, it provides job step resiliency for malformed input 
 * data. 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 * @version 1.0
 */
public class SkipRecordException extends BatchException {
	
	private static final long serialVersionUID = -6060807363878460320L;
	private Object skippedRecord;
	
	public SkipRecordException( Object skippedRecord ) {
		super();
		this.skippedRecord = skippedRecord;
	}
	
	public SkipRecordException( Object skippedRecord, Throwable cause ) {
		super(cause);
		this.skippedRecord = skippedRecord;
	}
	
	public SkipRecordException( Object skippedRecord, String reason ) {
		super(reason);
		this.skippedRecord = skippedRecord;
	}
	
	public SkipRecordException( Object skippedRecord, String reason, Throwable cause ) {
		super(reason,cause);
		this.skippedRecord = skippedRecord;
	}
	
	public Object getSkippedRecord() {
		return this.skippedRecord;
	}
}
