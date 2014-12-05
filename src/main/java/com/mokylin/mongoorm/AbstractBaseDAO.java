package com.mokylin.mongoorm;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/11/24.
 */

public abstract class AbstractBaseDAO<T extends BaseModel> implements BaseDAO<T> {


    private Class<T> modelClazz;

    protected  void setModelClazz(Class<T> modelClazz){
       this.modelClazz = modelClazz;
    }

    protected Class<T> getModelClazz() {
        return modelClazz;
    }




}
