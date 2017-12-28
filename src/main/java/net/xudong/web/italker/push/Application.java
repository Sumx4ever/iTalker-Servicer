package net.xudong.web.italker.push;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.xudong.web.italker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * @author xudongsun
 */

public class Application extends ResourceConfig {


    public Application() {

        // 1.注册packgaes 注册逻辑处理包名，放在service中
        // 写法1
        // packages("net.xudong.web.italker.push.service");
        // 写法2
        packages(AccountService.class.getPackage().getName());

        //2.注册json的解析器
        register(JacksonJsonProvider.class);

        //3.注册日志打印输出
        register(Logger.class);


    }

}
