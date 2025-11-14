package org.grails.datastore.bson.codecs.encoders

import java.time.OffsetTime

import groovy.transform.CompileStatic
import org.bson.BsonWriter

import org.grails.datastore.bson.codecs.temporal.OffsetTimeBsonConverter
import org.grails.datastore.mapping.model.PersistentProperty

import static org.grails.datastore.bson.codecs.encoders.SimpleEncoder.TypeEncoder

/**
 * A simple encoder for {@link java.time.OffsetTime}
 *
 * @author James Kleeh
 */
@CompileStatic
class OffsetTimeEncoder implements TypeEncoder, OffsetTimeBsonConverter {

    @Override
    void encode(BsonWriter writer, PersistentProperty property, Object value) {
        write(writer, (OffsetTime) value)
    }

}
