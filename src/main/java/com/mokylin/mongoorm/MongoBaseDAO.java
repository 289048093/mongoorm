package com.mokylin.mongoorm;

import com.mokylin.mongoorm.annotation.*;
import com.mokylin.mongoorm.cache.ClassInfoCache;
import com.mongodb.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/11/24.
 */

public class MongoBaseDAO<T extends BaseModel> extends AbstractBaseDAO<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoBaseDAO.class);

    private static final ConcurrentHashMap<Class<? extends BaseModel>, MongoBaseDAO<? extends BaseModel>> instances = new ConcurrentHashMap<>();

    public static <V extends BaseModel> MongoBaseDAO<V> instanceOf(Class<V> clazz) {
        //noinspection unchecked
        MongoBaseDAO<V> dao = (MongoBaseDAO<V>) instances.get(clazz);
        if (dao == null) {
            synchronized (instances) {
                //noinspection unchecked
                dao = (MongoBaseDAO<V>) instances.get(clazz);
                if (dao == null) {
                    dao = new MongoBaseDAO<>();
                    dao.setModelClazz(clazz);
                    instances.put(clazz, dao);
                }
            }
        }
        return dao;
    }

    @Override
    public WriteResult insert(T t) {
        DBObject dbObject = getDBObject(t);
        WriteResult res = getCollection().insert(dbObject);
        t.setId(getId(dbObject));
        return res;
    }

    @Override
    public WriteResult saveOrUpdate(T t) {
        if (t.getId() != null) {
            return update(t);
        }
        return insert(t);
    }

    @Override
    public T get(ObjectId id) {
        return getModel(getCollection().findOne(new BasicDBObject(BaseColumn.ID, id)));
    }

    @Override
    public List<T> find(DBObject query) {
        DBCursor dbObjects = getCollection().find(query);
        List<T> res = new LinkedList<>();
        for (DBObject o : dbObjects) {
            res.add(getModel(o));
        }
        return res;
    }

    @Override
    public List<T> find(DBObject query, int start, int limit) {
        DBCursor dbObjects = getCollection().find(query);
        if (start > 0) {
            dbObjects.skip(start);
        }
        if (limit > 0) {
            dbObjects.limit(limit);
        }
        List<T> res = new LinkedList<>();
        for (DBObject o : dbObjects) {
            res.add(getModel(o));
        }
        return res;
    }

    @Override
    public T findOne(DBObject query) {
        return getModel(getCollection().findOne(query));
    }

    @Override
    public List<T> findAll() {
        DBCursor dbObjects = getCollection().find();
        List<T> res = new LinkedList<>();
        for (DBObject o : dbObjects) {
            res.add(getModel(o));
        }
        return res;
    }

    @Override
    public WriteResult update(T t) {
        return getCollection().update(
                new BasicDBObject(BaseColumn.ID, t.getId()),
                new BasicDBObject("$set", getDBObject(t)),
                false,
                true);
    }

    @Override
    public WriteResult update(DBObject where, DBObject set) {
        return getCollection().update(where, new BasicDBObject("$set", set), false, true);
    }

    @Override
    public WriteResult delete(T t) {
        return getCollection().remove(new BasicDBObject(BaseColumn.ID, t.getId()));
    }

    @Override
    public WriteResult delete(ObjectId id) {
        return getCollection().remove(new BasicDBObject(BaseColumn.ID, id));
    }

    @Override
    public WriteResult delete(DBObject obj) {
        return getCollection().remove(obj);
    }

    @Override
    public long count(DBObject query) {
        return getCollection().count(query);
    }

    public static ObjectId getId(DBObject dbo) {
        return (ObjectId) dbo.get(BaseColumn.ID);
    }

    @Override
    public DBCollection getCollection() {
        Table annotation = ClassInfoCache.getAnnotation(getModelClazz(), Table.class);
        String tableName;
        if (annotation != null) {
            tableName = annotation.value();
        } else {
//            throw new IllegalArgumentException("该类没有指定Table");
            tableName = getModelClazz().getSimpleName();
        }
        return MongoDb.getInstance().getCollection(tableName);
    }


    /**
     * 通过实体生成MongoDB对象
     *
     * @param entity
     * @return
     */
    public DBObject getDBObject(Object entity) {
        if (entity == null) {
            return null;
        }
        DBObject object = new BasicDBObject();

        Collection<Field> fields = ClassInfoCache.getPersistFields(entity.getClass());

        for (Field field : fields) {
            Column column = ClassInfoCache.getAnnotation(field, Column.class);
            if (column == null) {
                continue;
            }
            String dbCol = column.value();
            try {
                Object fieldVal = field.get(entity);
                Object dbVal = fieldVal;
                if (fieldVal instanceof Enum) {
                    Enum ev = (Enum) fieldVal;
                    EnumValue enumValue = ClassInfoCache.getAnnotation(field, EnumValue.class);
                    if (enumValue != null) {
                        EnumValueType type = enumValue.value();
                        switch (type) {
                            case NAME:
                                dbVal = ev.name();
                                break;
                            case STRING:
                                dbVal = ev.toString();
                                break;
                            case CUSTOM:
                                String methodName = enumValue.method();
                                try {
                                    Method enumMethod = ClassInfoCache.getMethod(fieldVal.getClass(), methodName);
                                    dbVal = enumMethod.invoke(fieldVal);
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                                break;
                        }
                    } else {
                        dbVal = ev.name();
                    }
                }
                Custom customAnn = ClassInfoCache.getAnnotation(field, Custom.class);
                if (customAnn != null) {
                    Class<? extends CustomConverter> converterClazz = customAnn.converter();
                    CustomConverter converter = ClassInfoCache.getInstance(converterClazz);
                    converter.setFieldClazz(field.getClass());
                    dbVal = converter.serialize(fieldVal);
                }
                object.put(dbCol, dbVal);
            } catch (IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return object;
    }

    /**
     * 通过mongoDB对象获取Model
     *
     * @param dbo
     * @return
     */
    public T getModel(DBObject dbo) {
        if (dbo == null) {
            return null;
        }
        Collection<Field> fields = ClassInfoCache.getPersistFields(getModelClazz());
        T t = null;
        try {
            t = getModelClazz().newInstance();
            for (Field field : fields) {
                Column column = ClassInfoCache.getAnnotation(field, Column.class);
                String dbCol = column.value();
                Object dbVal = dbo.get(dbCol);
                if (dbVal == null) {
                    continue;
                }
                Object fieldVal = null;
                Class<?> type = field.getType();
                if (Enum.class.isAssignableFrom(type)) {
                    fieldVal = getEnumValue(field, dbVal);
                } else {
                    fieldVal = dbVal;
                }
                Custom customAnn = ClassInfoCache.getAnnotation(field, Custom.class);
                if (customAnn != null) {
                    Class<? extends CustomConverter> converterClazz = customAnn.converter();
                    CustomConverter converter = ClassInfoCache.getInstance(converterClazz);
                    converter.setFieldClazz(field.getClass());
                    fieldVal = converter.deSerialize(dbVal);
                }
                field.set(t, fieldVal);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return t;
    }

    private Object getEnumValue(Field field, Object dbVal) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        EnumValue enumAnn = ClassInfoCache.getAnnotation(field, EnumValue.class);
        EnumValueType annValue = enumAnn.value();
        //noinspection unchecked
        Class<? extends Enum> fieldEnum = (Class<? extends Enum>) field.getType();
        switch (annValue) {
            case NAME:
                return Enum.valueOf(fieldEnum, dbVal.toString());
            case STRING:
                return getEnumByToString(fieldEnum, dbVal);
            case CUSTOM:
                String enumMethodName = enumAnn.method();
                return getEnumByMethodValue(enumMethodName, fieldEnum, dbVal);
        }
        return null;
    }

    private Object getEnumByToString(Class<? extends Enum> enumClass, Object fieldValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ClassInfoCache.getMethod(enumClass, "values");
        Object[] objs = (Object[]) method.invoke(enumClass);
        for (Object obj : objs) {
            if (obj.equals(fieldValue)) {
                return obj;
            }
        }
        return null;
    }

    private Object getEnumByMethodValue(String methodName, Class<? extends Enum> enumClass, Object fieldValue) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method method = ClassInfoCache.getMethod(enumClass, "values");
        Object[] objs = (Object[]) method.invoke(enumClass);
        for (Object obj : objs) {
            Method enumMethod = ClassInfoCache.getMethod(obj.getClass(), methodName);
            Object enumMethodReturnValue = enumMethod.invoke(obj);
            if (enumMethodReturnValue.equals(fieldValue)) {
                return obj;
            }
        }
        return null;
    }
}
