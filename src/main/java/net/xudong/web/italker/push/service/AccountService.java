package net.xudong.web.italker.push.service;

import net.xudong.web.italker.push.bean.db.User;

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

    //127.0.0.1/api/account/login
    @GET
    @Path("/login")
    public String get() {
        return "You get the login by get.";
    }

    @POST
    @Path("/login")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User post() {
        User user = new User();
        user.setName("admin");
        return user;
    }

}
