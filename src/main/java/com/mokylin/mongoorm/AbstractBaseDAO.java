package com.mokylin.mongoorm;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/11/24.
 */

public abstract class AbstractBaseDAO<T extends BaseModel> implements BaseDAO<T> {


    private Class<T> modelClazz;

    {
        Type[] args = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
        if (args != null && args.length > 0)
            modelClazz = (Class<T>) args[0];
    }


    protected Class<T> getModelClazz() {
        return modelClazz;
    }


}
