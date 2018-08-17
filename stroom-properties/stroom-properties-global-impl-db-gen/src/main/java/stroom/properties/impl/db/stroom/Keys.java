/*
 * This file is generated by jOOQ.
*/
package stroom.properties.impl.db.stroom;


import javax.annotation.Generated;

import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;

import stroom.properties.impl.db.stroom.tables.Property;
import stroom.properties.impl.db.stroom.tables.PropertyHistory;
import stroom.properties.impl.db.stroom.tables.records.PropertyHistoryRecord;
import stroom.properties.impl.db.stroom.tables.records.PropertyRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>stroom</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<PropertyRecord, Integer> IDENTITY_PROPERTY = Identities0.IDENTITY_PROPERTY;
    public static final Identity<PropertyHistoryRecord, Integer> IDENTITY_PROPERTY_HISTORY = Identities0.IDENTITY_PROPERTY_HISTORY;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<PropertyRecord> KEY_PROPERTY_PRIMARY = UniqueKeys0.KEY_PROPERTY_PRIMARY;
    public static final UniqueKey<PropertyRecord> KEY_PROPERTY_NAME = UniqueKeys0.KEY_PROPERTY_NAME;
    public static final UniqueKey<PropertyHistoryRecord> KEY_PROPERTY_HISTORY_PRIMARY = UniqueKeys0.KEY_PROPERTY_HISTORY_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<PropertyRecord, Integer> IDENTITY_PROPERTY = createIdentity(Property.PROPERTY, Property.PROPERTY.ID);
        public static Identity<PropertyHistoryRecord, Integer> IDENTITY_PROPERTY_HISTORY = createIdentity(PropertyHistory.PROPERTY_HISTORY, PropertyHistory.PROPERTY_HISTORY.ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<PropertyRecord> KEY_PROPERTY_PRIMARY = createUniqueKey(Property.PROPERTY, "KEY_property_PRIMARY", Property.PROPERTY.ID);
        public static final UniqueKey<PropertyRecord> KEY_PROPERTY_NAME = createUniqueKey(Property.PROPERTY, "KEY_property_name", Property.PROPERTY.NAME);
        public static final UniqueKey<PropertyHistoryRecord> KEY_PROPERTY_HISTORY_PRIMARY = createUniqueKey(PropertyHistory.PROPERTY_HISTORY, "KEY_property_history_PRIMARY", PropertyHistory.PROPERTY_HISTORY.ID);
    }
}