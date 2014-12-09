package test.com.mokylin.mongoorm.dao;

import com.mokylin.mongoorm.MongoBaseDAO;
import test.com.mokylin.mongoorm.model.RoleModel;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/12/9.
 */

public class RoleDAO extends MongoBaseDAO<RoleModel> {

    public static RoleDAO getInstance(){
        return instanceOf(RoleModel.class,new DaoBuilder<RoleDAO>() {
            @Override
            public RoleDAO newInstance() {
                return new RoleDAO();
            }
        });
    }
}
