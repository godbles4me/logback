package ch.qos.logback.core.spi;

/**
 * 生命周期
 */
public interface LifeCycle {

    void start();

    void stop();

    boolean isStarted();

}
