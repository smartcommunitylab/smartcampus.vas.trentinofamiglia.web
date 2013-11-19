/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.data;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.springframework.data.mongodb.core.geo.Circle;

import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;

public interface GeoTimeObjectSyncStorage extends BasicObjectSyncStorage {
	
	public <T extends BaseDTObject> List<T> searchObjects(Class<T> cls, Circle circle, String text, Long from, Long to, Map<String, Object> criteria, SortedMap<String,Integer> sort) throws DataException; 
	public <T extends BaseDTObject> List<T> searchObjects(Class<T> cls, Circle circle, String text, Long from, Long to, Map<String, Object> criteria, SortedMap<String,Integer> sort, int limit, int skip) throws DataException; 
	
}
