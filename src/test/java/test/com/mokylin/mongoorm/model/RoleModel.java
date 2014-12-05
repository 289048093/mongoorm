package test.com.mokylin.mongoorm.model;

import com.mokylin.mongoorm.BaseModel;
import com.mokylin.mongoorm.annotation.Column;
import com.mokylin.mongoorm.annotation.Table;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/12/5.
 */
@Table("t_role")
public class RoleModel extends BaseModel {

    @Column
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
