/**
 * Copyright 2016 the Rex-Soft Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rex.db.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.rex.db.exception.DBRuntimeException;
import org.rex.db.logger.Logger;
import org.rex.db.logger.LoggerFactory;

/**
 * Constant utilities.
 * 
 * @version 1.0, 2016-02-14
 * @since Rexdb-1.0
 */
public class ConstantUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstantUtil.class);

	private final Map<String, Object> constants = new HashMap<String, Object>();

	private final Class<?> clazz;

	public ConstantUtil(Class<?> clazz) {
		this.clazz = clazz;
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
				String name = f.getName();
				try {
					Object value = f.get(null);
					constants.put(name, value);
				} catch (IllegalAccessException ex) {
					LOGGER.warn("could not read constant property {0} of {1}, {2}.", name, clazz.getName(), ex.getMessage());
				}
			}
		}
	}

	public int getSize() {
		return constants.size();
	}

	public Object asObject(String code) {
		code = code.toUpperCase();
		if (!constants.containsKey(code))
			throw new DBRuntimeException("DB-UCS01", clazz.getName(), code);
		
		return constants.get(code);
	}

	public Number asNumber(String code) {
		Object obj = asObject(code);
		if (!(obj instanceof Number))
			throw new DBRuntimeException("DB-UCS02", clazz.getName(), code, obj.getClass().getName());
		return (Number) obj;
	}

	public String asString(String code) {
		Object obj = asObject(code);
		return obj == null ? null : obj.toString();
	}

	public Set getValues(String namePrefix) {
		namePrefix = namePrefix.toUpperCase();
		Set values = new HashSet();
		for (Iterator it = constants.keySet().iterator(); it.hasNext();) {
			String code = (String) it.next();
			if (code.startsWith(namePrefix)) {
				values.add(constants.get(code));
			}
		}
		return values;
	}

	public String toCode(Object value, String namePrefix) {
		namePrefix = namePrefix.toUpperCase();
		for (Iterator it = constants.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			if (key.startsWith(namePrefix) && entry.getValue().equals(value))
				return key;
		}
		throw new DBRuntimeException("DB-UCS03", clazz.getName(), namePrefix, value);
	}

}
