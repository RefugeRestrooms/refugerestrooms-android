package org.refugerestrooms.database;


import org.refugerestrooms.database.model.BathroomEntity;
import org.refugerestrooms.database.model.DaoSession;
import org.refugerestrooms.models.Bathroom;

public final class SaveBathroomPropertyHandler {

    private SaveBathroomPropertyHandler() {}

    /**
     * Save property will take the bathroom object and convert it into a BathroomEntity Object
     * and then insert it into the daosession.
     * @param daoSession
     * @param bathroom
     */
    public static void saveProperty(DaoSession daoSession, Bathroom bathroom){
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
        entity.setTimestamp(bathroom.getTimestamp());
        daoSession.insertOrReplace(entity);
    }
}