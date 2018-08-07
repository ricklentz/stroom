/*
 * This file is generated by jOOQ.
*/
package stroom.properties.impl.db.stroom;


import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.AbstractKeys;

import stroom.properties.impl.db.stroom.tables.Property;
import stroom.properties.impl.db.stroom.tables.PropertyHistory;


/**
 * A class modelling indexes of tables of the <code>stroom</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index PROPERTY_NAME = Indexes0.PROPERTY_NAME;
    public static final Index PROPERTY_PRIMARY = Indexes0.PROPERTY_PRIMARY;
    public static final Index PROPERTY_HISTORY_PRIMARY = Indexes0.PROPERTY_HISTORY_PRIMARY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 extends AbstractKeys {
        public static Index PROPERTY_NAME = createIndex("name", Property.PROPERTY, new OrderField[] { Property.PROPERTY.NAME }, true);
        public static Index PROPERTY_PRIMARY = createIndex("PRIMARY", Property.PROPERTY, new OrderField[] { Property.PROPERTY.ID }, true);
        public static Index PROPERTY_HISTORY_PRIMARY = createIndex("PRIMARY", PropertyHistory.PROPERTY_HISTORY, new OrderField[] { PropertyHistory.PROPERTY_HISTORY.ID }, true);
    }
}