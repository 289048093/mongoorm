package com.mokylin.mongoorm;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/11/24.
 */

public interface BaseDAO<T extends BaseModel> {

    DBCollection getCollection();

    WriteResult insert(T t);

    WriteResult saveOrUpdate(T t);

    T get(ObjectId id);

    List<T> find(DBObject query);

    T findOne(DBObject query);

    public List<T> find(DBObject query, int start, int limit);

    List<T> findAll();

    WriteResult update(T t);

    WriteResult update(DBObject where, DBObject set);

    WriteResult delete(T t);

    WriteResult delete(ObjectId id);

    WriteResult delete(DBObject obj);

    long count(DBObject obj);
}
