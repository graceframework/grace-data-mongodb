package org.grails.datastore.bson.codecs

import java.time.Instant

import groovy.transform.CompileStatic
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

import org.grails.datastore.bson.codecs.temporal.InstantBsonConverter

/**
 * A class to translate a {@link java.time.Instant} in MongoDB
 *
 * @author James Kleeh
 */
@CompileStatic
class InstantCodec implements Codec<Instant>, InstantBsonConverter {

    @Override
    Instant decode(BsonReader reader, DecoderContext decoderContext) {
        read(reader)
    }

    @Override
    void encode(BsonWriter writer, Instant value, EncoderContext encoderContext) {
        write(writer, value)
    }

    @Override
    Class<Instant> getEncoderClass() { Instant }

}
