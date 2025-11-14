package org.grails.datastore.bson.codecs.encoders

import java.time.ZonedDateTime

import groovy.transform.CompileStatic
import org.bson.BsonWriter

import org.grails.datastore.bson.codecs.temporal.ZonedDateTimeBsonConverter
import org.grails.datastore.mapping.model.PersistentProperty

/**
 * A simple encoder for {@link java.time.ZonedDateTime}
 *
 * @author James Kleeh
 */
@CompileStatic
class ZonedDateTimeEncoder implements SimpleEncoder.TypeEncoder, ZonedDateTimeBsonConverter {

    @Override
    void encode(BsonWriter writer, PersistentProperty property, Object value) {
        write(writer, (ZonedDateTime) value)
    }

}
