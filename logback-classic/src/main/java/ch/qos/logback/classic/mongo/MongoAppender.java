package ch.qos.logback.classic.mongo;

import ch.qos.logback.classic.pattern.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.util.TimeUtil;
import org.bson.Document;

/**
 * MongoDB Appender
 * <p>
 * {@link UnsynchronizedAppenderBase}非同步Appender需要继承的抽象类.
 *
 * @author Daniel Lea
 */
public class MongoAppender extends MongoAppenderBase<ILoggingEvent> {

    private static final String UNKNOW = "UNKNOW";

    private static DateConverter        dateConverter     ;
    private static ThreadConverter      threadConverter   ;
    private static LevelConverter       levelConverter    ;
    private static LoggerConverter      loggerConverter   ;
    private static MessageConverter     messageConverter  ;

    static {
        dateConverter       = new DateConverter();
        threadConverter     = new ThreadConverter();
        levelConverter      = new LevelConverter();
        loggerConverter     = new LoggerConverter();
        messageConverter    = new MessageConverter();
    }

    public MongoAppender(final String collectionName) {
        super(collectionName);
    }

    @Override
    protected Document formatLogEventObject2Document(ILoggingEvent eventObject) {
        return
                new Document()
                        .append("time",     dateConverter.convert(eventObject))
                        .append("thread",   threadConverter.convert(eventObject))
                        .append("level",    levelConverter.convert(eventObject))
                        .append("logger",   loggerConverter.convert(eventObject))
                        .append("message",  messageConverter.convert(eventObject))
                        ;
    }

}
