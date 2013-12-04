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
package eu.trentorise.smartcampus.trentinofamiglia.listener;

import it.sayservice.platform.client.InvocationException;
import it.sayservice.platform.client.ServiceBusClient;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Subscriber {

	public static final String GET_DOSSIER = "GetDossier";
	public static final String GET_ORGANIZZAZIONI = "GetOrganizzazioni";
	public static final String GET_EVENTI = "GetEventi";
	public static final String GET_MANIFESTAZIONI = "GetManifestazioni";
	public static final String GET_STRUTTURE_RICETTIVE = "GetStruttureRicettive";
	public static final String GET_PERSONE_AUDIT = "GetPersoneAudit";
	public static final String GET_NEW_MEDIA = "GetNewMedia";
	public static final String GET_ALLATTAMENTO = "GetAllattamento";
	public static final String GET_BIKE_TRACKS = "GetBikeTracks";
	public static final String GET_WALKBIKE_TRACKS = "GetWalkBikeTracks";
	public static final String GET_WALK_TRACKS = "GetWalkTracks";

	public static final String GET_DISTRETTI = "GetDistretti";
	public static final String GET_PROGRAMMI = "GetProgrammi";

	public static final String GET_EVENTI_GARDA = "GetEventiGarda";

	public static final String GET_FAMILY_TRENTINO = "GetFamilyTrentino";
	public static final String GET_BABY_LITTLE_HOME = "GetBabyLittleHome";

	public static final String TRENTINOFAMIGLIA = "smartcampus.service.trentinofamiglia";
	public static final String TRACKS = "smartcampus.service.trentinotrack";

	private Log logger = LogFactory.getLog(getClass());

	public Subscriber(ServiceBusClient client) {
		try {
			System.out.println("SUBSCRIBE");
			Map<String, Object> params = new TreeMap<String, Object>();
			client.subscribeService(TRENTINOFAMIGLIA, GET_DOSSIER, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_ORGANIZZAZIONI, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_EVENTI, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_MANIFESTAZIONI, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_STRUTTURE_RICETTIVE, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_PERSONE_AUDIT, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_NEW_MEDIA, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_ALLATTAMENTO, params);
			// client.subscribeService(TRACKS, GET_BIKE_TRACKS, params);
			client.subscribeService(TRACKS, GET_WALKBIKE_TRACKS, params);
			client.subscribeService(TRACKS, GET_WALK_TRACKS, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_EVENTI_GARDA, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_FAMILY_TRENTINO, params);
			client.subscribeService(TRENTINOFAMIGLIA, GET_BABY_LITTLE_HOME, params);

			String ids[] = { "16342", "17441", "17442", "17443", "17444", "17445", "17446", "18163", "18167", "18171" };
			for (String id : ids) {
				params.put("idDistretto", id);
				client.subscribeService(TRENTINOFAMIGLIA, GET_DISTRETTI, params);
				client.subscribeService(TRENTINOFAMIGLIA, GET_PROGRAMMI, params);
			}

		} catch (InvocationException e) {
			logger.error("Failed to subscribe for service events: " + e.getMessage());
		}
	}
}
