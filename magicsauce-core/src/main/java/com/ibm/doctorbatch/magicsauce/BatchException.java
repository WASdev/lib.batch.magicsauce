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
 * This is the base class for all runtime exceptions in this framework. 
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 *
 * @version 1.0
 */

public class BatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BatchException() {
		super();
	}
	
	public BatchException(Throwable cause) {
		super(cause);
	}
	
	public BatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchException(String message) {
		super(message);
	}
}
