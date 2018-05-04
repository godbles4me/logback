package ch.qos.logback.classic.mongo;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.net.UnknownHostException;

public abstract class MongoAppenderBase<E> extends UnsynchronizedAppenderBase<E> {


    // 默认常量

    private static final String             DEFAULT_MONGO_HOST              = "127.0.0.1";
    private static final int                DEFAULT_MONGO_PORT              = 27017;
    private static final String             DEFAULT_DB_NAME                 = "db";
    private static final String             DEFAULT_DB_COLLECTION_NAME      = "business_log";
    private static final String             DEFAULT_MONGO_USERNAME          = "root";
    private static final int                DEFAULT_MAX_WAIT_TIME           = 1000 * 60 * 2;
    private static final int                DEFAULT_CONNECTIONS_PER_HOST    = 10;
    private static final int                DEFAULT_THREAD_ALLOWED          = 5;
    private static final int                DEFAULT_CONNECT_TIMEOUT         = 1000 * 30;
    private static final int                DEFAULT_SOCKET_TIMEOUT          = 1000 * 30;



    // 基本信息

    private String         host                                             = DEFAULT_MONGO_HOST;
    private int            port                                             = DEFAULT_MONGO_PORT;
    private String         username                                         ;
    private String         password                                         ;
    private String         dbName                                           = DEFAULT_DB_NAME;
    private String         collectionName                                   = DEFAULT_DB_COLLECTION_NAME;
    /** 最大连接空闲时间 */
    private int            maxConnectionIdleTime                            ;
    /** 最长等待时间 */
    private int            maxWaitTime                                      = DEFAULT_MAX_WAIT_TIME;
    private int            connectionsPerHost                               = DEFAULT_CONNECTIONS_PER_HOST;
    private int            threadsAllowedToBlockForConnectionMultiplier     = DEFAULT_THREAD_ALLOWED;
    /** 连接超时(建立连接、关闭连接) */
    private int            connectTimeout                                   = DEFAULT_CONNECT_TIMEOUT;
    /** 套接字超时(读写数据交互) */
    private int            socketTimeout                                    = DEFAULT_SOCKET_TIMEOUT;
    /** 心跳连接超时 */
    private int            heartbeatConnectTimeout;
    /** 心跳频率 */
    private int            heartbeatFrequency;
    /** 最小心跳频率 */
    private int            minHeartbeatFrequency;
    /** 心跳套接字超时 */
    private int            heartbeatSocketTimeout;
    /** 重试写 */
    private Boolean        retryWrites                                      = Boolean.TRUE;



    // 基本操作

    /** 客户端 */
    private MongoClient                     mongoClient                     ;
    /** 指定文档的集合 */
    private MongoCollection<Document>       dbCollection                    ;
    /** 是否自动重试 */
    private boolean                         autoConnectRetry                ;
    /** 异步刷盘 */
    private boolean                         fsync                           ;

    protected String                        source                          ;

    /**
     * 构造MongoDB集合
     * @param collectionName 集合名
     */
    protected MongoAppenderBase(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public void start() {

        try {
            // 与Mongo服务器建立连接
            if (null != this.mongoClient) {
                addError("Mongo client is up now.");
            } else {
                this.mongoClient = this.connect();
            }

            MongoDatabase mongoDatabase = null;
            if (null != this.mongoClient) {
                mongoDatabase = this.mongoClient.getDatabase(dbName);
            }

            // 获取集合
            dbCollection = mongoDatabase.getCollection(this.collectionName);

            // 标记appender开启
            super.start();

            // JVM关闭钩子,实现优雅关机
            Runtime.getRuntime().addShutdownHook(new Thread("Mongo-Shutdown-Hook-Thread") {
                @Override
                public void run() {
                    if (null != mongoClient) {
                        mongoClient.close();
                        addInfo("Closing Mongo Connection!");
                    }
                }
            });
        } catch (UnknownHostException e) {
            this.addError("Fail to connect MongoDB Server " + this.host + ":" + this.port, e);
        }
    }

    private MongoClient connect() throws UnknownHostException {

        ServerAddress serverAddress = new ServerAddress(this.host, this.port);

        if (null != this.getMongoCredential()) {
            return new MongoClient(serverAddress, this.getMongoCredential(), this.getMongoClientOptions());
        } else {
            return new MongoClient(serverAddress, this.getMongoClientOptions());
        }
    }

    private MongoClientOptions getMongoClientOptions() {
        return
                new MongoClientOptions
                        .Builder()
                        .maxWaitTime(this.maxWaitTime)
                        .connectionsPerHost(this.connectionsPerHost)
                        .threadsAllowedToBlockForConnectionMultiplier(this.threadsAllowedToBlockForConnectionMultiplier)
                        .connectTimeout(this.connectTimeout)
                        .socketTimeout(this.socketTimeout)
                        .retryWrites(this.retryWrites)
                        .build();
    }

    private MongoCredential getMongoCredential() {
        if (StringUtils.isNoneBlank(username) && StringUtils.isNoneBlank(password)) {
            return MongoCredential.createCredential(username, "source", new String(password).toCharArray());
        } else {
            return null;
        }

    }

    @Override
    public void stop() {
        // 断开mongo连接
        if (null != this.mongoClient) {
            this.mongoClient.close();
        }

        // 标记appender关闭
        super.stop();
    }

    /**
     * 扩展点, 派生类构造文档
     *
     * @param eventObject 日志事件对象
     * @return 构建的文档
     */
    protected abstract Document formatLogEventObject2Document(final E eventObject);

    /**
     *
     *
     * @param eventObject 日志事件对象
     */
    @Override
    public void append(final E eventObject) {
        final Document document = this.formatLogEventObject2Document(eventObject);
        // 保存日志相关文档
        dbCollection.insertOne(document);
    }


    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getConnectionsPerHost() {
        return connectionsPerHost;
    }

    public void setConnectionsPerHost(int connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    public int getThreadsAllowedToBlockForConnectionMultiplier() {
        return threadsAllowedToBlockForConnectionMultiplier;
    }

    public void setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
        this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Boolean getRetryWrites() {
        return retryWrites;
    }

    public void setRetryWrites(Boolean retryWrites) {
        this.retryWrites = retryWrites;
    }
}
