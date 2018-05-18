package net.xudong.web.italker.push.service;

import net.xudong.web.italker.push.bean.api.account.AccountRspModel;
import net.xudong.web.italker.push.bean.api.account.LoginModel;
import net.xudong.web.italker.push.bean.api.account.RegisterModel;
import net.xudong.web.italker.push.bean.api.base.ResponseModel;
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

    // 登陆
    @POST
    @Path("/login")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> login(LoginModel model) {

        if(!LoginModel.chekc(model)){
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.login(model.getAccount(),model.getPassword().trim());
        if (user != null) {
            // 登陆成功
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else{
            // 登陆失败
            return ResponseModel.buildLoginError();
        }
    }


    // 注册
    @POST
    @Path("/register")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model) {

        if(!RegisterModel.chekc(model)){
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }


        User user = UserFactory.findByPhone(model.getAccount().trim());
        if (user != null) {
            // 已有账户
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(model.getName().trim());
        if (user != null) {
            // 已有用户名
            return ResponseModel.buildHaveNameError();
        }


        // 开始注册逻辑
        user = UserFactory.register(model.getAccount(),
                model.getPassword(),
                model.getName());

        if (user != null) {
            // 返回当前的账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            // 注册异常
            return ResponseModel.buildRegisterError();
        }
    }

}
