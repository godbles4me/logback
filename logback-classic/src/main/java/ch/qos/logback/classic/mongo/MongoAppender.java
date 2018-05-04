package ch.qos.logback.classic.mongo;

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

    public MongoAppender(final String collectionName) {
        super(collectionName);
    }

    @Override
    protected Document formatLogEventObject2Document(ILoggingEvent eventObject) {
        return
                new Document()
                        .append("time", TimeUtil.formatSpecifiedTimestamp2Date(
                                ((ILoggingEvent) eventObject).getTimeStamp(), "yyyy-MM-dd HH:mm:ss"))
                        .append("thread",
                                ((ILoggingEvent) eventObject).getThreadName())
                        .append("level",
                                ((ILoggingEvent) eventObject).getLevel().toString())
                        .append("logger",
                                ((ILoggingEvent) eventObject).getLoggerName())
                        .append("message", ((ILoggingEvent) eventObject).getMessage());
    }

}
