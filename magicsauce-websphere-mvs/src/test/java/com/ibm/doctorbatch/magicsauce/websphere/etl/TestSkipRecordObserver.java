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

package com.ibm.doctorbatch.magicsauce.websphere.etl;

import java.util.Observable;
import java.util.Observer;

public class TestSkipRecordObserver implements Observer {
	private static boolean wasUpdated = false;
	
	@Override
	public void update(Observable arg0, Object arg1) {
		TestSkipRecordObserver.wasUpdated = true;
	}

	public static boolean wasUpdated() {
		return TestSkipRecordObserver.wasUpdated;
	}
	
	public static void reset() {
		TestSkipRecordObserver.wasUpdated = false;
	}
}
