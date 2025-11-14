package org.grails.datastore.bson.codecs.encoders

import java.time.LocalDateTime

import groovy.transform.CompileStatic
import org.bson.BsonWriter

import org.grails.datastore.bson.codecs.temporal.LocalDateTimeBsonConverter
import org.grails.datastore.mapping.model.PersistentProperty

import static org.grails.datastore.bson.codecs.encoders.SimpleEncoder.TypeEncoder

/**
 * A simple encoder for {@link java.time.LocalDateTime}
 *
 * @author James Kleeh
 */
@CompileStatic
class LocalDateTimeEncoder implements TypeEncoder, LocalDateTimeBsonConverter {

    @Override
    void encode(BsonWriter writer, PersistentProperty property, Object value) {
        write(writer, (LocalDateTime) value)
    }

}
