package net.xudong.web.italker.push.service;

import com.google.common.base.Strings;
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

        if (!LoginModel.chekc(model)) {
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.login(model.getAccount(), model.getPassword().trim());
        if (user != null) {
            // 如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user,model.getPushId());
            }

            // 登陆成功
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
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

        if (!RegisterModel.chekc(model)) {
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
            // 如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user,model.getPushId());
            }

            // 返回当前的账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            // 注册异常
            return ResponseModel.buildRegisterError();
        }
    }

    // 绑定pushId
    @POST
    @Path("/bind/{pushId}")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // 从请求头中获取token
    // pushId从url地址中获取
    public ResponseModel<AccountRspModel> bind(@HeaderParam("token") String token, @PathParam("pushId") String pushId) {

        if (Strings.isNullOrEmpty(token)
                || Strings.isNullOrEmpty(pushId)) {
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }

        // 拿到个人信息
        User user = UserFactory.findByToken(token);

        if (user != null) {
            // 进行设备号绑定的操作
            return bind(user, pushId);
        } else {
            // Token 失效，所有无法进行绑定
            return ResponseModel.buildAccountError();
        }
    }


    /**
     * 绑定的操作
     *
     * @param self 自己的user
     * @param pushId
     * @return
     */
    private ResponseModel<AccountRspModel> bind(User self, String pushId) {
        // 进行设备号绑定的操作
        self = UserFactory.bindPushId(self, pushId);

        if (self == null) {
            // 绑定失败，则是服务器异常
            return ResponseModel.buildServiceError();
        }

        // 返回当前账户，并且已经绑定
        AccountRspModel rspModel = new AccountRspModel(self, true);
        return ResponseModel.buildOk(rspModel);
    }

}
