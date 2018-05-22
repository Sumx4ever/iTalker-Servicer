package net.xudong.web.italker.push.factory;

import com.google.common.base.Strings;
import net.xudong.web.italker.push.bean.db.User;
import net.xudong.web.italker.push.utils.Hib;
import net.xudong.web.italker.push.utils.TextUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

/**
 * @author Sumx
 */
public class UserFactory {
    // 通过phone找到user
    public static User findByPhone(String phone) {
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }

    // 通过name找到user
    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:inName")
                .setParameter("inName", name)
                .uniqueResult());
    }

    // 通过token找到user
    // 只用于查询自己信息
    public static User findByToken(String token) {
        return Hib.query(session -> (User) session
                .createQuery("from User where token=:inToken")
                .setParameter("inToken", token)
                .uniqueResult());
    }

    /**
     * 当前的账户绑定pushId
     * @param user 自己的User
     * @param pushId 自己设备的pushId
     * @return User
     */
    public static User bindPushId(User user,String pushId){
        if(Strings.isNullOrEmpty(pushId)){
            return null;
        }
        // 第一步是否有其他设备绑定了pushId
        // 取消绑定，避免推送混乱
        // 查询的列表不能包括自己
        Hib.queryOnly((Session session) -> {
            @SuppressWarnings("unchecked")
            List<User> userList = (List<User>) session.createQuery("from User where lower(pushId)=:pushId and id !=: userId")
                    .setParameter("pushId",pushId.toLowerCase())
                    .setParameter("userId",user.getId())
                    .list();

            for (User u : userList) {
                // 更新为null
                u.setPushId(null);
                session.saveOrUpdate(u);
            }


        });

        if(pushId.equalsIgnoreCase(user.getPushId())){
            // 如果当前需要绑定的设备Id，就是已经绑定过的
            // 那么不需要额外绑定
            return user;
        }else{
            // 如果当前账户之前的设备Id，和需要绑定的不同
            // 那么需要单点登陆，让之前的设备退出账户，
            // 给之前的账户推送一条退出消息
            if(Strings.isNullOrEmpty(user.getPushId())){
                // TODO 推送一条退出消息

            }

            user.setPushId(pushId);
            return Hib.query(session -> {
                session.saveOrUpdate(user);
                return user;
            });
        }
    }

    /**
     * 使用账户和密码进行登陆
     */
    public static User login(String account, String password) {
        String accountStr = account.trim();
        // 把原文进行同样的处理，然后才能进行匹配
        String encodePassword = encodePassword(password);

        User user = Hib.query(session -> (User) session
                .createQuery("from User where phone=:phone and password=:password")
                .setParameter("phone", accountStr)
                .setParameter("password", encodePassword)
                .uniqueResult());

        if (user != null) {
            // 对user进行登陆操作,更新Token
            user = login(user);
        }
        return user;
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

        User user = createUser(account, password, name);

        if (user != null) {
            user = login(user);
        }
        return user;
    }

//    public static User register(String account, String password, String name) {
//        // 去除账户中的收尾空格
//        account = account.trim();
//        // 处理密码
//        password = encodePassword(password);
//
//
//        User user = new User();
//
//        user.setName(name);
//        user.setPassword(password);
//        // 账户就是手机号
//        user.setPhone(account);
//
//        // 进行数据库操作
//        // 首先创建一个会话
//        Session session = Hib.session();
//        // 开启一个事务
//        session.beginTransaction();
//        try {
//            // 保存操作
//            session.save(user);
//            // 提交我们的事务
//            session.getTransaction().commit();
//            return user;
//        } catch (Exception e) {
//            // 失败情况下需要回滚事务
//            session.getTransaction().rollback();
//            return null;
//        }
//    }

    /**
     * 注册部分的新建用户逻辑
     *
     * @param account  手机号
     * @param password 加密后的密码
     * @param name     用户名
     * @return 返回一个用户
     */
    private static User createUser(String account, String password, String name) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        // 账户就是手机号
        user.setPhone(account);
        // 数据库存储
        return Hib.query(session -> {
            session.save(user);
            return user;
        });
    }

    /**
     * 把一个用户进行登陆操作
     * 本质上就是对Token进行操作
     *
     * @param user User
     * @return User
     */
    private static User login(User user) {
        // 使用一个随机的uuid值充当Token
        String newToken = UUID.randomUUID().toString();
        // 进行一次Base64格式化
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);
        //
        return Hib.query(session -> {
            session.saveOrUpdate(user);
            return user;
        });
    }

    /**
     * 对密码进行加密操作
     *
     * @param password 明文
     * @return 密文
     */
    private static String encodePassword(String password) {
        // 密码去除首尾空格
        password = password.trim();
        // 进行MD5非对称加密，加盐会更安全，当然盐也要存储
        password = TextUtil.getMD5(password);
        // 再进行一次对称的Base64加密，当然可以采取加盐的方案
        return TextUtil.encodeBase64(password);
    }
}
