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
package eu.trentorise.smartcampus.dt.model;

public class POIObject extends BaseDTObject {
	private static final long serialVersionUID = 3377022799304541031L;
	
	private POIData poi;

	public POIObject() {
		super();
	}

	public POIData getPoi() {
		return poi;
	}

	public void setPoi(POIData poi) {
		this.poi = poi;
	}

	@Override
	public double[] getLocation() {
		if (super.getLocation() != null) return super.getLocation();
		if (getPoi() != null) return new double[]{getPoi().getLatitude(),getPoi().getLongitude()};
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((poi == null) ? 0 : poi.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		POIObject other = (POIObject) obj;
		if (poi == null) {
			if (other.poi != null)
				return false;
		} else if (!poi.equals(other.poi))
			return false;
		return true;
	}

}
