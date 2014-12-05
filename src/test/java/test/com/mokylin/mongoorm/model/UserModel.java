package test.com.mokylin.mongoorm.model;

import com.mokylin.mongoorm.BaseModel;
import com.mokylin.mongoorm.annotation.Column;
import com.mokylin.mongoorm.annotation.Table;

/**
 * @author 李朝(Li.Zhao)
 * @since 2014/12/5.
 */
@Table("t_user")
public class UserModel extends BaseModel {

    @Column
    private String username;

    @Column
    private int age;

    @Column
    private RoleModel role;

    @Column("nick_name")
    private String nickName;



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public RoleModel getRole() {
        return role;
    }

    public void setRole(RoleModel role) {
        this.role = role;
    }
}
