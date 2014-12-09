package test.com.mokylin.mongoorm.dao;

import com.mokylin.mongoorm.MongoBaseDAO;
import test.com.mokylin.mongoorm.model.UserModel;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/12/9.
 */

public class UserDAO extends MongoBaseDAO<UserModel> {


    public static UserDAO getInstance(){
        return instanceOf(UserModel.class,new DaoBuilder<UserDAO>() {
            @Override
            public UserDAO newInstance() {
                return new UserDAO();
            }
        });
    }


}
