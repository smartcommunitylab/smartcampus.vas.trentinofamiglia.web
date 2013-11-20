package eu.trentorise.smartcampus.trentinofamiglia.processor;

import it.sayservice.platform.client.InvocationException;
import it.sayservice.platform.client.ServiceBusClient;
import it.sayservice.platform.client.ServiceBusListener;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.mongodb.util.JSON;

import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.InfoObject;
import eu.trentorise.smartcampus.dt.model.POIData;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.TrackObject;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiAllattamento;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiAttivita;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiAzione;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiManifestazione;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiNewMedia;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiOrganizzazioniDistretto;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiPersonaAudit;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiProgramma;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DatiProgrammiDistretto;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.DossierFamiglia;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.EventoFamiglia;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.EventoGarda;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.OrganizzazioneAderente;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.OrganizzazioneFamiglia;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.StrutturaRicettiva;
import eu.trentorise.smartcampus.service.trentinotrack.data.message.Trentinotrack.BikeTrack;
import eu.trentorise.smartcampus.trentinofamiglia.listener.Subscriber;

//import eu.trentorise.smartcampus.dt.model.InfoObject;

public class EventProcessorImpl implements ServiceBusListener {

	private static final String DOSSIER = "Dossier";
	private static final String ORGANIZZAZIONE = "Organizzazione";
	private static final String EVENTO = "Evento";
	private static final String MANIFESTAZIONE = "Manifestazione";
	private static final String STRUTTURA_RICETTIVA = "Struttura ricettiva";
	private static final String PERSONA_AUDIT = "Persona Audit";
	private static final String NEW_MEDIA = "New Media Education";
	private static final String ALLATTAMENTO = "Zona allattamento";

	private static final String BIKE_TRACK = "Pista ciclabile";
	private static final String BIKEWALK_TRACK = "Pista ciclopedonale";
	private static final String WALK_TRACK = "Trekking";

	private static final String DISTRETTO = "Distretto";
	private static final String ORGANIZZAZIONE_DISTRETTO = "Organizzazione distretto";
	private static final String ATTIVITA_DISTRETTO = "Attivita distretto";
	
	private static final String EVENTO_GARDA = "Evento Garda";

	@Autowired
	private BasicObjectSyncStorage storage;

	@Autowired
	ServiceBusClient client;

	private static Log logger = LogFactory.getLog(EventProcessorImpl.class);

	@Override
	public void onServiceEvents(String serviceId, String methodName, String subscriptionId, List<ByteString> data) {
		System.out.println(new Date() + " -> " + methodName + "@" + serviceId);
		try {
			if (Subscriber.TRENTINOFAMIGLIA.equals(serviceId) || Subscriber.TRACKS.endsWith(serviceId)) {
				if (Subscriber.GET_DOSSIER.equals(methodName)) {
					updateDossier(data);
				} else if (Subscriber.GET_ORGANIZZAZIONI.equals(methodName)) {
					updateOrganizzazioni(data);
				} else if (Subscriber.GET_EVENTI.equals(methodName)) {
					updateEventi(data);
				} else if (Subscriber.GET_MANIFESTAZIONI.equals(methodName)) {
					updateManifestazioni(data);
				} else if (Subscriber.GET_STRUTTURE_RICETTIVE.equals(methodName)) {
					updateStruttureRicettive(data);
				} else if (Subscriber.GET_PERSONE_AUDIT.equals(methodName)) {
					updatePersoneAudit(data);
				} else if (Subscriber.GET_NEW_MEDIA.equals(methodName)) {
					updateNewMedia(data);
				} else if (Subscriber.GET_BIKE_TRACKS.equals(methodName)) {
					updateTrack(data, methodName, BIKE_TRACK);
				} else if (Subscriber.GET_WALKBIKE_TRACKS.equals(methodName)) {
					updateTrack(data, methodName, BIKEWALK_TRACK);
				} else if (Subscriber.GET_WALK_TRACKS.equals(methodName)) {
					updateTrack(data, methodName, WALK_TRACK);
				} else if (Subscriber.GET_ALLATTAMENTO.equals(methodName)) {
					updateAllattamento(data);
				} else if (Subscriber.GET_DISTRETTI.equals(methodName)) {
					updateDistretti(data);
				} else if (Subscriber.GET_PROGRAMMI.equals(methodName)) {
					updateProgrammi(data);
				} else if (Subscriber.GET_EVENTI_GARDA.equals(methodName)) {
					updateEventiGarda(data);
				}

			}

		} catch (Exception e) {
			logger.error("Error updating " + methodName);
			e.printStackTrace();
		}

	}

	public BasicObjectSyncStorage getStorage() {
		return storage;
	}

	public void setStorage(BasicObjectSyncStorage storage) {
		this.storage = storage;
	}

	public ServiceBusClient getClient() {
		return client;
	}

	public void setClient(ServiceBusClient client) {
		this.client = client;
	}

	public void updateDossier(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DossierFamiglia df = DossierFamiglia.parseFrom(bs);
			String id = encode(Subscriber.GET_DOSSIER + "_" + df.getId());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject dtobj = new InfoObject();
			dtobj.setDescription(df.getDescription());
			dtobj.setTitle(df.getTitle());
			dtobj.setType(DOSSIER);
			dtobj.setSource(Subscriber.TRENTINOFAMIGLIA);

			dtobj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("type", df.getType());
			cd.put("who", df.getWho());
			cd.put("what", df.getWhat());
			cd.put("more", df.getMore());
			dtobj.setCustomData(cd);

			if (!dtobj.equals(oldDtobj)) {
				storage.storeObject(dtobj);
				System.out.println("CHANGED " + id);
			}
		}

	}

	public void updateOrganizzazioni(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			OrganizzazioneFamiglia of = OrganizzazioneFamiglia.parseFrom(bs);
			String id = encode(Subscriber.GET_ORGANIZZAZIONI + "_" + of.getOrder());

			POIObject oldDtobj = null;
			try {
				oldDtobj = (POIObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			POIObject poiObject = new POIObject();
			poiObject.setDescription("");
			poiObject.setTitle(of.getName());
			poiObject.setType(ORGANIZZAZIONE);
			poiObject.setSource(Subscriber.TRENTINOFAMIGLIA);

			poiObject.setId(id);

			double loc[] = new double[] { of.getPoi().getCoordinate().getLatitude(), of.getPoi().getCoordinate().getLongitude() };
			poiObject.setLocation(loc);

			poiObject.setPoi(new POIData(of.getPoi()));

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("link", of.getLink());
			cd.put("status", of.getStatus());
			poiObject.setCustomData(cd);

			if (!poiObject.equals(oldDtobj)) {
				storage.storeObject(poiObject);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateEventi(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			EventoFamiglia ef = EventoFamiglia.parseFrom(bs);
			String id = encode(Subscriber.GET_EVENTI + "_" + ef.getId() + "_" + ef.getPlace());

			EventObject oldDtobj = null;
			try {
				oldDtobj = (EventObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			EventObject eventObject = new EventObject();

			eventObject.setDescription(ef.getDescription());
			eventObject.setTitle(ef.getTitle());
			eventObject.setType(EVENTO);
			eventObject.setSource(Subscriber.TRENTINOFAMIGLIA);

			eventObject.setId(id);

			double loc[] = new double[] { ef.getPoi().getCoordinate().getLatitude(), ef.getPoi().getCoordinate().getLongitude() };
			eventObject.setLocation(loc);

			eventObject.setPoiId(ef.getPoi().getPoiId());

			eventObject.setFromTime(parseDate(ef.getFrom()));
			eventObject.setToTime(parseDate(ef.getTo()));

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("certified", ef.getCertified());
			cd.put("organization", ef.getOrganization());
			cd.put("place", ef.getPlace());
			cd.put("days", ef.getDays());

			eventObject.setCustomData(cd);

			if (!eventObject.equals(oldDtobj)) {
				storage.storeObject(eventObject);
				System.out.println("CHANGED " + id);
			}
		}

	}

	public void updateManifestazioni(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiManifestazione dm = DatiManifestazione.parseFrom(bs);
			String id = encode(Subscriber.GET_MANIFESTAZIONI + "_" + dm.getId());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject dtobj = new InfoObject();
			dtobj.setDescription(dm.getDescription());
			dtobj.setTitle(dm.getTitle());
			dtobj.setType(MANIFESTAZIONE);
			dtobj.setSource(Subscriber.TRENTINOFAMIGLIA);

			dtobj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("link", dm.getLink());
			// TODO pubDate
			dtobj.setCustomData(cd);

			if (!dtobj.equals(oldDtobj)) {
				storage.storeObject(dtobj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateStruttureRicettive(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			StrutturaRicettiva sr = StrutturaRicettiva.parseFrom(bs);
			String id = encode(Subscriber.GET_STRUTTURE_RICETTIVE + "_" + sr.getId());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject dtobj = new InfoObject();
			dtobj.setDescription("");
			dtobj.setTitle(sr.getName());
			dtobj.setType(STRUTTURA_RICETTIVA);
			dtobj.setSource(Subscriber.TRENTINOFAMIGLIA);

			double loc[] = new double[] { sr.getLat(), sr.getLon() };
			dtobj.setLocation(loc);					
			
			dtobj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("levelFamily", sr.getLevelFamily());
			cd.put("stars", sr.getStars());
			cd.put("region", sr.getRegion());
			cd.put("town", sr.getTown());
			cd.put("bookingHow", sr.getBookingHow());
			cd.put("bookingWhere", sr.getBookingWhere());
			cd.put("bookingAddress", sr.getBookingAddress());
			cd.put("bookingZipCode", sr.getBookingZipCode());
			cd.put("bookingTown", sr.getBookingTown());
			cd.put("bookingPhone", sr.getBookingPhone());
			cd.put("bookingEmail", sr.getBookingEmail());
			cd.put("bookingLink", sr.getBookingLink());
			cd.put("guide", sr.getGuide());
			dtobj.setCustomData(cd);

			if (!dtobj.equals(oldDtobj)) {
				storage.storeObject(dtobj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updatePersoneAudit(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiPersonaAudit pa = DatiPersonaAudit.parseFrom(bs);
			String id = encode(Subscriber.GET_PERSONE_AUDIT + "_" + pa.getId());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject dtobj = new InfoObject();
			dtobj.setDescription("");
			dtobj.setTitle(pa.getName());
			dtobj.setType(PERSONA_AUDIT);
			dtobj.setSource(Subscriber.TRENTINOFAMIGLIA);

			dtobj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("name", pa.getName());
			cd.put("email", pa.getEmail());
			cd.put("date", pa.getDate());
			cd.put("type", pa.getType());
			dtobj.setCustomData(cd);

			if (!dtobj.equals(oldDtobj)) {
				storage.storeObject(dtobj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateNewMedia(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiNewMedia dnm = DatiNewMedia.parseFrom(bs);
			String id = encode(Subscriber.GET_NEW_MEDIA + "_" + dnm.getName());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject dtobj = new InfoObject();
			dtobj.setDescription("");
			dtobj.setTitle(dnm.getName());
			dtobj.setType(NEW_MEDIA);
			dtobj.setSource(Subscriber.TRENTINOFAMIGLIA);

			double loc[] = new double[] { dnm.getLat(), dnm.getLon() };
			dtobj.setLocation(loc);								
			
			dtobj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("contact", dnm.getContact());
			cd.put("role", dnm.getRole());
			cd.put("link", dnm.getLink());
			cd.put("address", dnm.getAddress());
			cd.put("phone", dnm.getPhone());
			cd.put("email", dnm.getEmail());
			dtobj.setCustomData(cd);

			if (!dtobj.equals(oldDtobj)) {
				storage.storeObject(dtobj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateTrack(List<ByteString> bsl, String methodName, String type) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			BikeTrack bt = BikeTrack.parseFrom(bs);
			String id = encode(methodName + "_" + bt.getId());

			TrackObject oldDtobj = null;
			try {
				oldDtobj = (TrackObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			TrackObject tObj = new TrackObject();
			tObj.setTrack(bt.getPolyline());

			tObj.setType(type);
			tObj.setSource(Subscriber.TRENTINOFAMIGLIA);

			tObj.setTitle("");
			tObj.setDescription("");

			tObj.setId(id);
			//
			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("length", bt.getLength());
			cd.put("label", bt.getLabel());
			cd.put("about", bt.getAbout());

			tObj.setCustomData(cd);

			if (!tObj.equals(oldDtobj)) {
				storage.storeObject(tObj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateAllattamento(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiAllattamento da = DatiAllattamento.parseFrom(bs);
			String id = encode(Subscriber.GET_ALLATTAMENTO + "_" + da.getId());

			// TODO poi
			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			InfoObject tObj = new InfoObject();

			tObj.setType(ALLATTAMENTO);
			tObj.setSource(Subscriber.TRENTINOFAMIGLIA);

			tObj.setTitle(da.getName());
			tObj.setDescription("");
			
			double loc[] = new double[] { da.getLat(), da.getLon() };
			tObj.setLocation(loc);		
			

			tObj.setId(id);
			//
			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("address", da.getAddress());
			cd.put("town", da.getTown());
			cd.put("area", da.getArea());

			tObj.setCustomData(cd);

			if (!tObj.equals(oldDtobj)) {
				storage.storeObject(tObj);
				System.out.println("CHANGED " + id);
			}
		}
	}

	public void updateDistretti(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiOrganizzazioniDistretto dod = DatiOrganizzazioniDistretto.parseFrom(bs);
			String id = encode(Subscriber.GET_DISTRETTI + "_" + dod.getId());

			InfoObject oldDtobj = null;
			try {
				oldDtobj = (InfoObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}
			InfoObject tObj = new InfoObject();

			tObj.setType(DISTRETTO);
			tObj.setSource(Subscriber.TRENTINOFAMIGLIA);

			tObj.setTitle(dod.getTitle());
			tObj.setDescription(dod.getDescription());

			tObj.setId(id);

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("alias", dod.getAlias());

			tObj.setCustomData(cd);
			
			if (!tObj.equals(oldDtobj)) {
				storage.storeObject(tObj);
				System.out.println("CHANGED " + id);
			}
			
			updateOrganizzazioniDistretto(dod);
			
		}

	}
	
	private void updateOrganizzazioniDistretto(DatiOrganizzazioniDistretto dod) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		for (OrganizzazioneAderente oa : dod.getOrganizzazioniList()) {
					String id = encode(Subscriber.GET_DISTRETTI + "_" + oa.getName());

					InfoObject oldDtobj = null;
					try {
						oldDtobj = (InfoObject) storage.getObjectById(id);
					} catch (NotFoundException e) {}
					InfoObject tObj = new InfoObject();

					tObj.setType(ORGANIZZAZIONE_DISTRETTO);
					tObj.setSource(Subscriber.TRENTINOFAMIGLIA);

					tObj.setTitle(oa.getName());
					tObj.setDescription(oa.getDescription());
					double loc[] = new double[] { oa.getLat(), oa.getLon() };
					tObj.setLocation(loc);					

					tObj.setId(id);

					Map<String, Object> cd = new TreeMap<String, Object>();
					
					Map<String, Object> map = new TreeMap<String, Object>();
					map.put("alias", oa.getAlias());
					map.put("address", oa.getAddress());
					map.put("phone", oa.getPhone());
					map.put("fax", oa.getFax());
					map.put("email", oa.getEmail());
					map.put("link", oa.getLink());
					map.put("logo", oa.getLogo());
					map.put("district", dod.getTitle());

					tObj.setCustomData(map);
					
					if (!tObj.equals(oldDtobj)) {
						storage.storeObject(tObj);
						System.out.println("CHANGED " + id);
					}					
				}
			}

	

	public void updateProgrammi(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			DatiProgrammiDistretto dpd = DatiProgrammiDistretto.parseFrom(bs);
			updateAttivita(dpd);
		}

	}

	private void updateAttivita(DatiProgrammiDistretto dpd) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		for (DatiProgramma dp : dpd.getProgrammiList()) {
			for (DatiAttivita dat : dp.getAttivitaList()) {
//				for (DatiAzione daz : dat.getAzioniList()) {
					String id = encode(Subscriber.GET_PROGRAMMI + "_" + dpd.getId() + "_" + dat.getTitle());

					InfoObject oldDtobj = null;
					try {
						oldDtobj = (InfoObject) storage.getObjectById(id);
					} catch (NotFoundException e) {}
					InfoObject tObj = new InfoObject();

					tObj.setType(ATTIVITA_DISTRETTO);
					tObj.setSource(Subscriber.TRENTINOFAMIGLIA);

					tObj.setTitle(dat.getTitle());
					tObj.setDescription("");

					tObj.setId(id);

					Map<String, Object> cd = new TreeMap<String, Object>();
//					cd.put("alias", dpd.getAlias());					
					
					Map<String, Object> map = new TreeMap<String, Object>();
//					map.put("goal", daz.getGoal());
//					map.put("contact", daz.getContact());
//					map.put("times", daz.getTimes());
//					map.put("activity title", dat.getTitle());
					map.put("contact", dat.getContact());
					map.put("times", dat.getTimes());
					map.put("program year", dp.getYear());
					map.put("program link", dp.getLink());
					map.put("district name", dpd.getTitle());
					
					
					List<Object> actions = new ArrayList<Object>();
					for (DatiAzione daz : dat.getAzioniList()) {
						Map<String, Object> map2 = new TreeMap<String, Object>();
					map2.put("goal", daz.getGoal());
					map2.put("contact", daz.getContact());
					map2.put("times", daz.getTimes());
					map2.put("title", daz.getTitle());
					actions.add(map2);
					}
					map.put("actions", actions);
					
					tObj.setCustomData(map);
					
					if (!tObj.equals(oldDtobj)) {
						storage.storeObject(tObj);
						System.out.println("CHANGED " + id);
					}					
				}
			}
	}
	
	public void updateEventiGarda(List<ByteString> bsl) throws InvocationException, InvalidProtocolBufferException, NotFoundException, DataException {
		List<Map> fsl = new ArrayList<Map>();
		for (ByteString bs : bsl) {
			EventoGarda eg = EventoGarda.parseFrom(bs);
			String id = encode(Subscriber.GET_EVENTI_GARDA + "_" + eg.getTitle());

			EventObject oldDtobj = null;
			try {
				oldDtobj = (EventObject) storage.getObjectById(id);
			} catch (NotFoundException e) {}

			EventObject eventObject = new EventObject();

			eventObject.setDescription(eg.getDescription());
			eventObject.setTitle(eg.getTitle());
			eventObject.setType(EVENTO_GARDA);
			eventObject.setSource(Subscriber.TRENTINOFAMIGLIA);

			eventObject.setId(id);

			double loc[] = new double[] { eg.hasLat()?eg.getLat():0, eg.hasLon()?eg.getLon():0 };
			eventObject.setLocation(loc);

			eventObject.setFromTime(eg.getFrom());
			eventObject.setToTime(eg.getTo());

			Map<String, Object> cd = new TreeMap<String, Object>();
			cd.put("link", eg.getLink());
			cd.put("shortText", eg.getCategory());
			cd.put("place", eg.getPlace());
			cd.put("location", eg.getLocation());
			cd.put("category", eg.getCategory());
			cd.put("price", eg.getPrice());
			cd.put("free", eg.getFree());

			eventObject.setCustomData(cd);

			if (!eventObject.equals(oldDtobj)) {
				storage.storeObject(eventObject);
				System.out.println("CHANGED " + id);
			}
		}

	}	
	
	private Long parseDate(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.parse(date).getTime();
		} catch (Exception e) {
			return null;
		}
	}

	private static String encode(String s) {
		return new BigInteger(s.getBytes()).toString(16);
	}

}
