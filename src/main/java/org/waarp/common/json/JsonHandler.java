/**
   This file is part of Waarp Project.

   Copyright 2009, Frederic Bregier, and individual contributors by the @author
   tags. See the COPYRIGHT.txt in the distribution for a full listing of
   individual contributors.

   All Waarp Project is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Waarp is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.waarp.common.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON handler
 * @author "Frederic Bregier"
 *
 */
public class JsonHandler {

	/**
	 * JSON parser
	 */
	public static ObjectMapper mapper = new ObjectMapper();

	/**
	 * 
	 * @return an empty ObjectNode
	 */
	public static ObjectNode createObjectNode() {
		return mapper.createObjectNode();
	}
	
	/**
	 * 
	 * @param value
	 * @return the objectNode or null if an error occurs
	 */
	public static ObjectNode getFromString(String value) {
		try {
			return (ObjectNode) mapper.readTree(value);
		} catch (JsonProcessingException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	/**
	 * 
	 * @param object
	 * @return the Json representation of the object
	 */
	public static String writeAsString(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return "";
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return the corresponding HashMap
	 */
	public static Map<String, Object> getMapFromString(String value) {
		if (value != null && value.length() > 0) {
			Map<String, Object> info = null;
			try {
				info = JsonHandler.mapper.readValue(value, new TypeReference<Map<String, Object>>() {});
			} catch (JsonParseException e1) {
			} catch (JsonMappingException e1) {
			} catch (IOException e1) {
			}
			if (info == null) {
				info = new HashMap<String, Object>();
			}
			return info;
		} else {
			return new HashMap<String, Object>();
		}
	}
	
	
}
