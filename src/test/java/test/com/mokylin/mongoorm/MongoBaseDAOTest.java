package test.com.mokylin.mongoorm;

import com.mokylin.mongoorm.MongoBaseDAO;
import com.mokylin.mongoorm.MongoDb;
import com.mokylin.mongoorm.util.ConfigInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.com.mokylin.mongoorm.model.RoleModel;
import test.com.mokylin.mongoorm.model.UserModel;

/** 
* MongoBaseDAO Tester. 
* 
* @author <Authors name> 
* @since <pre>十二月 5, 2014</pre> 
* @version 1.0 
*/ 
public class MongoBaseDAOTest { 

@Before
public void before() throws Exception {
    ConfigInfo.setConfigPath("mongodb.properties");
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: instanceOf(Class<V> clazz) 
* 
*/ 
@Test
public void testInstanceOf() throws Exception {
    UserModel user = new UserModel();
    user.setUsername("user tom");
    user.setNickName("汤姆");
    user.setAge(22);
    RoleModel role = new RoleModel();
    role.setName("程序员");
    user.setRole(role);
    MongoBaseDAO<RoleModel> roleDao = MongoBaseDAO.instanceOf(RoleModel.class);
    roleDao.insert(role);
    MongoBaseDAO<UserModel> userDao = MongoBaseDAO.instanceOf(UserModel.class);
    userDao.insert(user);
} 

/** 
* 
* Method: insert(T t) 
* 
*/ 
@Test
public void testInsert() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: saveOrUpdate(T t) 
* 
*/ 
@Test
public void testSaveOrUpdate() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: get(ObjectId id) 
* 
*/ 
@Test
public void testGet() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: find(DBObject query) 
* 
*/ 
@Test
public void testFindQuery() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: find(DBObject query, int start, int limit) 
* 
*/ 
@Test
public void testFindForQueryStartLimit() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findOne(DBObject query) 
* 
*/ 
@Test
public void testFindOne() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findAll() 
* 
*/ 
@Test
public void testFindAll() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: update(T t) 
* 
*/ 
@Test
public void testUpdateT() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: update(DBObject where, DBObject set) 
* 
*/ 
@Test
public void testUpdateForWhereSet() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: delete(T t) 
* 
*/ 
@Test
public void testDeleteT() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: delete(ObjectId id) 
* 
*/ 
@Test
public void testDeleteId() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: delete(DBObject obj) 
* 
*/ 
@Test
public void testDeleteObj() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: count(DBObject query) 
* 
*/ 
@Test
public void testCount() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getId(DBObject dbo) 
* 
*/ 
@Test
public void testGetId() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getCollection() 
* 
*/ 
@Test
public void testGetCollection() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDBObject(Object entity) 
* 
*/ 
@Test
public void testGetDBObject() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getModel(DBObject dbo) 
* 
*/ 
@Test
public void testGetModel() throws Exception { 
//TODO: Test goes here... 
} 


/** 
* 
* Method: getEnumValue(Field field, Object dbVal) 
* 
*/ 
@Test
public void testGetEnumValue() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MongoBaseDAO.getClass().getMethod("getEnumValue", Field.class, Object.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getEnumByToString(Class<? extends Enum> enumClass, Object fieldValue) 
* 
*/ 
@Test
public void testGetEnumByToString() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MongoBaseDAO.getClass().getMethod("getEnumByToString", Class<?.class, Object.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: getEnumByMethodValue(String methodName, Class<? extends Enum> enumClass, Object fieldValue) 
* 
*/ 
@Test
public void testGetEnumByMethodValue() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = MongoBaseDAO.getClass().getMethod("getEnumByMethodValue", String.class, Class<?.class, Object.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

} 
