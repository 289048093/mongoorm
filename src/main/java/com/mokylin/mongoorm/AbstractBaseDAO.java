package com.mokylin.mongoorm;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/11/24.
 */

public abstract class AbstractBaseDAO<T extends BaseModel> implements BaseDAO<T> {


    private Class<T> clazz;

    protected  void setClass(Class<T> clazz){
       this.clazz = clazz;
    }

    protected Class<T> getClazz() {
        return clazz;
    }




}
