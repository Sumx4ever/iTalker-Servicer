package net.xudong.web.italker.push.service;

import net.xudong.web.italker.push.bean.api.account.RegisterModel;
import net.xudong.web.italker.push.bean.card.UserCard;
import net.xudong.web.italker.push.bean.db.User;
import net.xudong.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author xudongsun
 * <p>
 * 1.定义访问路径
 */
//实际访问路径 127.0.0.1/api/account/...
@Path("/account")
public class AccountService {

    @POST
    @Path("/register")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserCard register(RegisterModel model) {

        User user = UserFactory.findByPhone(model.getAccount().trim());
        if(user != null){
            UserCard userCard = new UserCard();
            userCard.setName("已有了该用户");
            return userCard;
        }

        user = UserFactory.register(model.getAccount(),
                model.getPassword(),
                model.getName());

        if(user != null){
            UserCard userCard = new UserCard();
            userCard.setName(user.getName());
            userCard.setPhone(user.getPhone());
            userCard.setSex(user.getSex());
            userCard.setFollow(true);
            userCard.setModifyAt(user.getUpdateAt());
            return userCard;
        }
        return null;
    }

}
