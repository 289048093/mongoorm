/* 
 * Copyright (C) 2011  SZ7thRoad.
 * 
 * http://www.7road.com
 */
package com.mokylin.mongoorm;

import com.mokylin.mongoorm.annotation.Column;
import org.bson.types.ObjectId;

/**
 * 基类Model
 * @author 李朝(Li.Zhao)
 * @since 2014/11/20.
 */

public abstract class BaseModel
{
//    @JsonSerialize(using = ToStringSerializer.class)
    @Column(BaseColumn.ID)
    private ObjectId id;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
