package org.refugerestrooms;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Ahmed Fahmy on 3/2/16.
 *
 * RestroomsDaoGenerator is only used one time to generate the DAO so it can
 * handle the SQLite code.
 *
 * To run, simply run gradle restroomsdaogenerator>task>run
 * in AndroidStudio.
 *
 * @output
 * org.refugerestrooms.database.model
 *
 * @resources
 * https://github.com/greenrobot/greenDAO
 * http://www.devteam83.com/en/tutorial-greendao-from-scratch-part-1/
 */
public class RestroomsDaoGenerator {


    private static final String BATHROOM_ENTITY = "BathroomEntity";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String STATE = "state";
    private static final String COUNTRY = "country";
    private static final String ACCESSIBLE = "accessible";
    private static final String UNISEX = "unisex";
    private static final String DIRECTIONS = "directions";
    private static final String COMMENT = "comment";
    private static final String DOWN_VOTE = "downvote";
    private static final String UP_VOTE = "upvote";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String TIMESTAMP = "timestamp";
    private static final String GENERATE_IN_PATH = "../app/src/main/java";
    private static final String DATABASE_PACKAGE = "org.refugerestrooms.database.model";

    public static void main(String args[]) throws Exception {
        initModel();
    }

    public static void initModel() {
        Schema schema = new Schema(1000, DATABASE_PACKAGE);
        addBathroom(schema);
        try {
            new DaoGenerator().generateAll(schema, GENERATE_IN_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addBathroom(Schema schema) {

        Entity entity = schema.addEntity(BATHROOM_ENTITY);
        entity.addLongProperty(ID).primaryKey();
        entity.addStringProperty(NAME);
        entity.addStringProperty(STREET);
        entity.addStringProperty(CITY);
        entity.addStringProperty(STATE);
        entity.addStringProperty(COUNTRY);
        entity.addBooleanProperty(ACCESSIBLE);
        entity.addBooleanProperty(UNISEX);
        entity.addStringProperty(DIRECTIONS);
        entity.addStringProperty(COMMENT);
        entity.addIntProperty(UP_VOTE);
        entity.addIntProperty(DOWN_VOTE);
        entity.addDoubleProperty(LATITUDE);
        entity.addDoubleProperty(LONGITUDE);
        entity.addLongProperty(TIMESTAMP);
    }


}
