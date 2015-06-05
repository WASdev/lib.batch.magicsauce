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
 * Contains static utility methods used in a variety of places throughout this framework.
 * 
 * @author Timothy C. Fanelli (tfanelli@us.ibm.com, tim@fanel.li, doc@torbat.ch)
 */
public class ClassUtil {
	
	/**
	 * This method creates an instance of the class specified by className. The class must
	 * be resolved by the calling contexts' class loader, and the class must have a default, 
	 * no-args constructor. 
	 * 
	 * @param className The fully qualified class name to instantiate
	 * @return An instance of the class specified by className.
	 */
	static public final Class<?> getClass(String className) {
		try {
			MagicSauceLogger.getInstance().trace( "Attempting to load Class: " + className );
			Class<?> clazz = Class.forName(className);
			return clazz;
		} catch ( ClassNotFoundException cfe ) {
			MagicSauceLogger.getInstance().exception("Class not found exception attempting to instantiate class " + className, cfe );
			throw new BatchException( "Unexpected error while instantiating Class. Expected Class not found", cfe.getCause() );
		} 	
	}

	@SuppressWarnings("unchecked")
	static public final <V> V getInstanceForClass(Class<?> clazz) {
		try {
			MagicSauceLogger.getInstance().trace( "Attempting to instantiate class: " + clazz.getCanonicalName() );
			
			Object o = clazz.newInstance();
			return (V) o;
		} catch (IllegalAccessException ile) {
			MagicSauceLogger.getInstance().exception( "IllegalAccessException attempting to instantiate class " + clazz.getCanonicalName(), ile );
			throw new BatchException( "Unexpected error while instantiating Class.", ile.getCause() );
		} catch (InstantiationException ine) {
			MagicSauceLogger.getInstance().exception("InstantiationException attempting to instantiate class " + clazz.getCanonicalName(), ine );
			throw new BatchException( "Unexpected error while instantiating Class.", ine.getCause() );
		}		
	}

	
	/**
	 * This method creates an instance of the class specified by className. The class must
	 * be resolved by the calling contexts' class loader, and the class must have a default, 
	 * no-args constructor. 
	 * 
	 * @param className The fully qualified class name to instantiate
	 * @return An instance of the class specified by className.
	 */
	static public final <V> V getInstanceForClass(String className) {
		Class<?> clazz = getClass( className );
		return getInstanceForClass( clazz );
	}
}
