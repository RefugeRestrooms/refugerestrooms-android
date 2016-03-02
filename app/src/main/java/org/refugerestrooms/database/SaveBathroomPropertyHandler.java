package org.refugerestrooms.database;


import org.refugerestrooms.database.model.BathroomEntity;
import org.refugerestrooms.database.model.DaoSession;
import org.refugerestrooms.models.Bathroom;

public  class SaveBathroomPropertyHandler {
    /**
     * Save property will take the bathroom object and convert it into a BathroomEntity Object
     * and insert it into the doasession.
     * @param doaSession
     * @param bathroom
     */
    public static void saveProperty(DaoSession doaSession, Bathroom bathroom){
        BathroomEntity entity = new BathroomEntity();
        entity.setId(bathroom.getmId());
        entity.setName(bathroom.getName());
        entity.setAccessible(bathroom.isAccessible());
        entity.setCity(bathroom.getmCity());
        entity.setCountry(bathroom.getmCountry());
        entity.setDirections(bathroom.getDirections());
        entity.setStreet(bathroom.getmStreet());
        entity.setUpvote(bathroom.getmUpvote());
        entity.setDownvote(bathroom.getmDownvote());
        entity.setLatitude(bathroom.getmLatitude());
        entity.setLongitude(bathroom.getmLongitude());
        entity.setState(bathroom.getmState());
        entity.setUnisex(bathroom.isUnisex());
        entity.setComment(bathroom.getComments());
        doaSession.insertOrReplace(entity);
    }

}