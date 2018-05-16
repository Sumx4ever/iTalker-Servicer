package net.xudong.web.italker.push.factory;

import net.xudong.web.italker.push.bean.db.User;
import net.xudong.web.italker.push.utils.Hib;
import net.xudong.web.italker.push.utils.TextUtil;
import org.hibernate.Session;

/**
 * @author Sumx
 */
public class UserFactory {
    // 查询指定账户
    public static User findByPhone(String phone) {
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }

    // 查询指定用户名
    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:inName")
                .setParameter("inName", name)
                .uniqueResult());
    }

    /**
     * 用户注册
     * 注册操作需要写入数据库，并且返回数据库中User信息
     *
     * @param account  账户
     * @param password 密码
     * @param name     用户名
     * @return 用户信息
     */
    public static User register(String account, String password, String name) {
        // 去除账户中的收尾空格
        account = account.trim();
        // 处理密码
        password = encodePassword(password);


        User user = new User();

        user.setName(name);
        user.setPassword(password);
        // 账户就是手机号
        user.setPhone(account);

        // 进行数据库操作
        // 首先创建一个会话
        Session session = Hib.session();
        // 开启一个事务
        session.beginTransaction();
        try {
            // 保存操作
            session.save(user);
            // 提交我们的事务
            session.getTransaction().commit();
            return user;
        } catch (Exception e) {
            // 失败情况下需要回滚事务
            session.getTransaction().rollback();
            return null;
        }
    }

    private static String encodePassword(String password) {
        // 密码去除首尾空格
        password = password.trim();
        // 进行MD5非对称加密，加盐会更安全，当然盐也要存储
        password = TextUtil.getMD5(password);
        // 再进行一次对称的Base64加密，当然可以采取加盐的方案
        return TextUtil.encodeBase64(password);
    }
}
