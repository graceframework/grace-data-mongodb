package org.grails.datastore.bson.codecs.temporal

import java.time.Period

import groovy.transform.CompileStatic
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter

import grails.gorm.time.PeriodConverter

/**
 * A trait to read and write a {@link java.time.Period} to MongoDB
 *
 * @author James Kleeh
 */
@CompileStatic
trait PeriodBsonConverter implements TemporalBsonConverter<Period>, PeriodConverter {

    @Override
    void write(BsonWriter writer, Period value) {
        writer.writeString(convert(value))
    }

    @Override
    Period read(BsonReader reader) {
        convert(reader.readString())
    }

    @Override
    BsonType bsonType() {
        BsonType.STRING
    }

}
