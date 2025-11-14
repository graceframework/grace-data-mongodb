package org.grails.datastore.bson.codecs.encoders

import java.time.OffsetDateTime

import groovy.transform.CompileStatic
import org.bson.BsonWriter

import org.grails.datastore.bson.codecs.temporal.OffsetDateTimeBsonConverter
import org.grails.datastore.mapping.model.PersistentProperty

import static org.grails.datastore.bson.codecs.encoders.SimpleEncoder.TypeEncoder

/**
 * A simple encoder for {@link java.time.OffsetDateTime}
 *
 * @author James Kleeh
 */
@CompileStatic
class OffsetDateTimeEncoder implements TypeEncoder, OffsetDateTimeBsonConverter {

    @Override
    void encode(BsonWriter writer, PersistentProperty property, Object value) {
        write(writer, (OffsetDateTime) value)
    }

}
