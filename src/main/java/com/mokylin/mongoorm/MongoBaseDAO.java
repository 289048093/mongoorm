package com.mokylin.mongoorm;

import com.mokylin.mongoorm.annotation.*;
import com.mokylin.mongoorm.cache.ClassInfoCache;
import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
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

public abstract class MongoBaseDAO<T extends BaseModel> extends AbstractBaseDAO<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoBaseDAO.class);

    private static final ConcurrentHashMap<String, MongoBaseDAO<? extends BaseModel>> instances = new ConcurrentHashMap<>();

    private String dbTableName = null;

    public static <R extends MongoBaseDAO<V>, V extends BaseModel> R instanceOf(Class<V> clazz, DaoBuilder<R> builder) {
        return instanceOf(clazz.getName(), builder);
    }

    public static <R extends MongoBaseDAO<V>, V extends BaseModel> R instanceOf(String key, DaoBuilder<R> builder) {
        //noinspection unchecked
        MongoBaseDAO<V> dao = (MongoBaseDAO<V>) instances.get(key);
        if (dao != null) {
            return (R) dao;
        }
        R daoTmp = builder.newInstance();
//        daoTmp.setModelClazz(clazz);
        MongoBaseDAO existDao = instances.putIfAbsent(key, daoTmp);
        return existDao != null ? (R) existDao : daoTmp;
    }

    protected static ConcurrentHashMap<String, MongoBaseDAO<? extends BaseModel>> getAllInstances() {
        return instances;
    }

    public static interface DaoBuilder<T extends MongoBaseDAO> {
        T newInstance();
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
        return find(query, start, limit, null);
    }

    @Override
    public List<T> find(DBObject query, int start, int limit, DBObject sort) {
        DBCursor dbObjects = getCollection().find(query);
        if (start > 0) {
            dbObjects.skip(start);
        }
        if (limit > 0) {
            dbObjects.limit(limit);
        }
        if (sort != null) {
            dbObjects.sort(sort);
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
        return MongoDb.getInstance().getCollection(getDbTableName());
    }

    protected void setDbTableName(String dbTableName) {
        this.dbTableName = dbTableName;
    }

    protected String getDbTableName() {
        if (dbTableName != null) {
            return dbTableName;
        }
        Table annotation = ClassInfoCache.getAnnotation(getModelClazz(), Table.class);
        String tableName;
        if (annotation != null) {
            tableName = annotation.value();
        } else {
//            throw new IllegalArgumentException("该类没有指定Table");
            tableName = getModelClazz().getSimpleName();
        }
        dbTableName = tableName;
        return tableName;
    }

    public void beginTransaction() {
        getCollection().getDB().requestStart();
    }

    public void commitTransaction() {
        getCollection().getDB().requestDone();
    }


    private static ThreadLocal<BasicDBObject> dboLocal = new InheritableThreadLocal<>();

    public static BasicDBObject newBasicDBObject() {
        BasicDBObject dbo = dboLocal.get();
        if (dbo != null) {
            dbo.clear();
            return dbo;
        }
        dbo = new BasicDBObject();
        dboLocal.set(dbo);
        return dbo;
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
        BasicDBObject object = newBasicDBObject();

        Collection<Field> fields = ClassInfoCache.getPersistFields(entity.getClass());

        for (Field field : fields) {
            Column column = ClassInfoCache.getAnnotation(field, Column.class);
            if (column == null) {
                continue;
            }
            String dbCol = column.value();
            if (StringUtils.isBlank(dbCol)) {
                dbCol = field.getName();
            }
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
                if (fieldVal instanceof BaseModel) {
                    dbVal = ((BaseModel) fieldVal).getId();
                }
                Custom customAnn = ClassInfoCache.getAnnotation(field, Custom.class);
                if (customAnn != null) {
                    Class<? extends CustomConverter> converterClazz = customAnn.converter();
                    CustomConverter converter = ClassInfoCache.getSingleton(converterClazz);
                    converter.setFieldClazz(field.getType());
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
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        for (Field field : fields) {
            try {
                Column column = ClassInfoCache.getAnnotation(field, Column.class);
                String dbCol = column.value();
                if (StringUtils.isBlank(dbCol)) {
                    dbCol = field.getName();
                }
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
                if (BaseModel.class.isAssignableFrom(field.getType())) {
                    fieldVal = field.getType().newInstance();
                    ((BaseModel) fieldVal).setId((ObjectId) dbVal);
                }
                if (customAnn != null) {
                    Class<? extends CustomConverter> converterClazz = customAnn.converter();
                    CustomConverter converter = ClassInfoCache.getSingleton(converterClazz);
                    converter.setFieldClazz(field.getType());
                    fieldVal = converter.deSerialize(dbVal);
                }
                Class<?> fieldClass = field.getType();
                if (!fieldClass.isAssignableFrom(fieldVal.getClass())) {
                    fieldVal = convertVal(fieldClass, fieldVal);
                }
                field.set(t, fieldVal);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return t;
    }

    private Object convertVal(Class<?> fieldClass, Object fieldVal) {
        if (fieldVal == null) return null;
        if (Integer.class.equals(fieldClass)
                || int.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).intValue();
        }
        if (Short.class.equals(fieldClass)
                || short.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).shortValue();
        }
        if (Byte.class.equals(fieldClass)
                || byte.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).byteValue();
        }
        if (Long.class.equals(fieldClass)
                || long.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).longValue();
        }
        if (Double.class.equals(fieldClass)
                || double.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).doubleValue();
        }
        if (Float.class.equals(fieldClass)
                || float.class.equals(fieldClass)
                && fieldVal instanceof Number) {
            return ((Number) fieldVal).floatValue();
        }
        return fieldVal;
    }

    private Object getEnumValue(Field field, Object dbVal) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        EnumValue enumAnn = ClassInfoCache.getAnnotation(field, EnumValue.class);
        EnumValueType annValue = enumAnn == null ? EnumValueType.NAME : enumAnn.value();
        //noinspection unchecked
        Class<? extends Enum> fieldEnum = (Class<? extends Enum>) field.getType();
        switch (annValue) {
            case NAME:
                try {
                    return Enum.valueOf(fieldEnum, dbVal.toString());
                } catch (Exception e) {
                    return null;
                }
            case STRING:
                return getEnumByToString(fieldEnum, dbVal);
            case CUSTOM:
                String enumMethodName = enumAnn == null ? "name" : enumAnn.method();
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
