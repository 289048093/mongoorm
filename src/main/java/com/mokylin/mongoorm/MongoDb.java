package com.mokylin.mongoorm;

import com.mokylin.mongoorm.util.ConfigInfo;
import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * MongoDb单例
 *
 * @author lizhao
 */
public class MongoDb {

    private static Logger LOGGER = LoggerFactory.getLogger(MongoDb.class);

    private MongoDb() {
        initDb();
    }

    private static String getConfigPath() {
        return ConfigInfo.getConfigPath();
    }

    /**
     * 初始化db连接属性
     */
    public void initDb() {
        if (StringUtils.isBlank(getConfigPath())) {
            throw new IllegalStateException("没有配置configPath");
        }
        if (mongo == null) {
            try {
                ConfigInfo configInfo = ConfigInfo.getInstance();
                String dbUrl = configInfo.getString(ConfigInfo.DB_URL);
                int dbPort = configInfo.getInt(ConfigInfo.DB_PORT);
                dbName = configInfo.getString(ConfigInfo.DB_NAME);
                mongo = new Mongo(dbUrl, dbPort);
                int poolSize = ConfigInfo.getInstance().getInt(ConfigInfo.DB_POOL_SIZE);// 连接数量
                int blockSize = ConfigInfo.getInstance().getInt(ConfigInfo.DB_BLOCK_SIZE); // 等待队列长度
                MongoOptions opt = mongo.getMongoOptions();
                opt.connectionsPerHost = poolSize;
                opt.threadsAllowedToBlockForConnectionMultiplier = blockSize;
            } catch (UnknownHostException e) {
                LOGGER.error("CDKey 初始化 数据库配置 UnknownHostException: {}", e.getMessage(), e);
            } catch (MongoException me) {
                LOGGER.error("CDKey 初始化 数据库配置 MongoException: {}", me.getMessage(), me);
            }
        }
        if (db == null) {
            db = mongo.getDB(dbName);
            String dbUser = ConfigInfo.getInstance().getString(ConfigInfo.DB_JDBC_USER);
            if (StringUtils.isNotBlank(dbUser)) {
                boolean auth = db.authenticate(dbUser, ConfigInfo.getInstance().getString(ConfigInfo.DB_JDBC_PASSWORD).toCharArray());
                if (!auth) {
                    throw new RuntimeException("用户认证失败,请确认用户名密码正确");
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                getInstance().destroy();
            }
        });
    }

    /**
     * 关闭数据库连接
     */
    public void destroy() {
        if (mongo != null) {
            mongo.close();
        }
        mongo = null;
        db = null;
    }


    private static MongoDb instance = new MongoDb();

    public static MongoDb getInstance() {
        return instance;
    }

    private String dbName;
    private Mongo mongo;
    private DB db;

    public DBCollection getCollection(String tableName) {
        return db.getCollection(tableName);
    }

}
