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
package eu.trentorise.smartcampus.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.broker.UserIDBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.dt.model.BaseDTObject;
import eu.trentorise.smartcampus.dt.model.CommunityData;
import eu.trentorise.smartcampus.dt.model.EventObject;
import eu.trentorise.smartcampus.dt.model.InfoObject;
import eu.trentorise.smartcampus.dt.model.POIObject;
import eu.trentorise.smartcampus.dt.model.Rating;
import eu.trentorise.smartcampus.dt.model.Review;
import eu.trentorise.smartcampus.dt.model.ReviewObject;
import eu.trentorise.smartcampus.dt.model.TrackObject;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.storage.sync.BasicObjectSyncStorage;
import eu.trentorise.smartcampus.storage.ReviewsMongoStorage;

@Controller
public class ObjectController extends AbstractObjectController {

	@Autowired
	private BasicObjectSyncStorage syncStorage;

	@Autowired
	private ReviewsMongoStorage reviewStorage;
	
	@RequestMapping(method = RequestMethod.GET, value="/events")
	public ResponseEntity<List<EventObject>> getAllEventObject(HttpServletRequest request) throws Exception {
		List<EventObject> list = getAllObject(request, EventObject.class);
		String userId = null;
		try {
			userId = getUserId();
		} catch (SecurityException e) {
			
		}
		for (BaseDTObject bo : list) {
			bo.filterUserData(userId);
		}
		return new ResponseEntity<List<EventObject>>(list, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/pois")
	public ResponseEntity<List<POIObject>> getAllPOIObject(HttpServletRequest request) throws Exception {
		List<POIObject> list = getAllObject(request, POIObject.class);
		String userId = null;
		try {
			userId = getUserId();
		} catch (SecurityException e) {
			
		}
		for (BaseDTObject bo : list) {
			bo.filterUserData(userId);
		}
		return new ResponseEntity<List<POIObject>>(list, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/infos")
	public ResponseEntity<List<InfoObject>> getAllInfoObject(HttpServletRequest request) throws Exception {
		List<InfoObject> list = getAllObject(request, InfoObject.class);
		String userId = null;
		try {
			userId = getUserId();
		} catch (SecurityException e) {
			
		}
		for (BaseDTObject bo : list) {
			bo.filterUserData(userId);
		}
		return new ResponseEntity<List<InfoObject>>(list, HttpStatus.OK);
	}	
	
	@RequestMapping(method = RequestMethod.GET, value="/tracks")
	public ResponseEntity<List<TrackObject>> getAllITrackObject(HttpServletRequest request) throws Exception {
		List<TrackObject> list = getAllObject(request, TrackObject.class);
		String userId = null;
		try {
			userId = getUserId();
		} catch (SecurityException e) {
			
		}
		for (BaseDTObject bo : list) {
			bo.filterUserData(userId);
		}
		return new ResponseEntity<List<TrackObject>>(list, HttpStatus.OK);
	}		
	

	@RequestMapping(value = "/social/rate/{id}", method = RequestMethod.PUT)
	public void rate(HttpServletRequest request, HttpServletResponse response, @RequestParam String rating, @PathVariable String id) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(id);

			if (obj instanceof InfoObject) {
				logger.error("Cannot rate InfoObject with id " + id + " as user " + userId);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			Integer iRating = Integer.parseInt(rating);
			if (iRating > 5)
				iRating = 5;
			if (iRating < 0)
				iRating = 0;

			double avg = 0;

			Rating newRating = null;

			if (obj.getCommunityData() == null) {
				CommunityData cd = new CommunityData();
				cd.setRatings(new ArrayList<Rating>());
				obj.setCommunityData(cd);
			}

			for (Rating rat : obj.getCommunityData().getRatings()) {
				if (userId.equals(rat.getUserId())) {
					newRating = rat;
					break;
				}
			}

			if (newRating == null) {
				newRating = new Rating(userId, iRating);
				obj.getCommunityData().getRatings().add(newRating);
			} else {
				newRating.setValue(iRating);
			}

			for (Rating rat : obj.getCommunityData().getRatings()) {
				avg += rat.getValue();
			}
			avg = avg / obj.getCommunityData().getRatings().size();

			obj.getCommunityData().setAverageRating((int) avg);
			obj.getCommunityData().setRatingsCount(obj.getCommunityData().getRatings().size());

			syncStorage.storeObject(obj);
		} catch (NotFoundException e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to rate object with id " + id + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/social/readReviews/{parentId}", method = RequestMethod.GET)
	public @ResponseBody
	ReviewObject getReviews(HttpServletRequest request, HttpServletResponse response, @PathVariable String parentId) {
		try {
			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			if (reviews == null) {
				reviews = new ReviewObject(parentId);
			}

			return reviews;
		} catch (NotFoundException e) {
			logger.error("Failed to retrieve reviews for object with id " + parentId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		} catch (Exception e) {
			logger.error("Failed to retrieve reviews for object with id " + parentId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(value = "/social/review/{parentId}", method = RequestMethod.POST)
	public void review(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> pars, @PathVariable String parentId) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(parentId);

			if (obj instanceof InfoObject) {
				logger.error("Cannot review InfoObject with id " + parentId + " as user " + userId);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			if (reviews == null) {
				reviews = new ReviewObject(parentId);
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			String reviewString = (String) pars.get("review");
			Review review = null;
			for (Review oldReview : reviews.getReviews()) {
				if (userId.equals(oldReview.getUserId())) {
					review = oldReview;
					break;
				}
			}

			if (review != null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			review = new Review(userId, reviewString);
			reviews.getReviews().add(review);

			reviewStorage.storeObject(reviews);
		} catch (NotFoundException e) {
			logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed to review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/social/review/{parentId}", method = RequestMethod.PUT)
	public void updateReview(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> pars, @PathVariable String parentId) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to update review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}			
			
			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(parentId);

			if (obj instanceof InfoObject) {
				logger.error("Cannot review InfoObject with id " + parentId + " as user " + userId);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			String reviewString = (String) pars.get("review");
			Review review = null;
			for (Review oldReview : reviews.getReviews()) {
				if (userId.equals(oldReview.getUserId())) {
					review = oldReview;
					break;
				}
			}

			if (review == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			review.setComment(reviewString);

			reviewStorage.storeObject(reviews);
		} catch (NotFoundException e) {
			logger.error("Failed to update review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Failed to update review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/social/review/{parentId}", method = RequestMethod.DELETE)
	public void deleteReview(HttpServletRequest request, HttpServletResponse response, @PathVariable String parentId) {
		String userId = null;
		try {
			try {
				userId = getUserId();
			} catch (SecurityException e) {
				logger.error("Failed to delete review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}			
			
			BaseDTObject obj = (BaseDTObject) syncStorage.getObjectById(parentId);

			if (obj instanceof InfoObject) {
				logger.error("Cannot delete review for InfoObject with id " + parentId + " as user " + userId);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			ReviewObject reviews = reviewStorage.getObjectById(parentId);

			Review review = null;
			for (Review oldReview : reviews.getReviews()) {
				if (userId.equals(oldReview.getUserId())) {
					review = oldReview;
					break;
				}
			}

			if (review != null) {
				reviews.getReviews().remove(review);
				reviewStorage.storeObject(reviews);
				response.setStatus(HttpStatus.OK.value());
			} else {
				response.setStatus(HttpStatus.METHOD_FAILURE.value());
			}

		} catch (NotFoundException e) {
			logger.error("Failed to delete review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			response.setStatus(HttpStatus.METHOD_FAILURE.value());
		} catch (Exception e) {
			logger.error("Failed to delete review object with id " + parentId + " as user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			response.setStatus(HttpStatus.METHOD_FAILURE.value());
		}
	}

}
