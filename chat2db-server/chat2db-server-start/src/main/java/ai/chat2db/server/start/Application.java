package ai.chat2db.server.start;

import ai.chat2db.server.tools.common.enums.ModeEnum;
import ai.chat2db.server.tools.common.model.ConfigJson;
import ai.chat2db.server.tools.common.util.ConfigUtils;
import ai.chat2db.server.tools.common.util.EasyEnumUtils;
import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Indexed;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

/**
 * 启动类
 *
 * @author Jiaju Zhuang
 */
@SpringBootApplication
@ComponentScan(value = {"ai.chat2db.server"})
@Indexed
@EnableCaching
@EnableScheduling
@EnableAsync
@Slf4j
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder){
        return applicationBuilder.sources(Application.class);
    }

    @Override
    protected WebApplicationContext run(SpringApplication application) {
        //application.setApplicationContextClass(WeblogicServletWebServerApplicationContext.class);
        application.getSources().remove(ErrorPageFilter.class);
        String currentVersion = ConfigUtils.getLocalVersion();
        ConfigJson configJson = ConfigUtils.getConfig();

        // The unique ID of the entire system will not change after multiple starts
        if (StringUtils.isBlank(configJson.getSystemUuid())) {
            configJson.setSystemUuid(UUID.fastUUID().toString(true));
            ConfigUtils.setConfig(configJson);
        }

        // Represents that the current version has been successfully launched
        if (StringUtils.isNotBlank(currentVersion) && StringUtils.equals(currentVersion,
                configJson.getLatestStartupSuccessVersion())) {
            // Flyway doesn't need to start every time to increase startup speed
            //args = ArrayUtils.add(args, "--spring.flyway.enabled=false");
            log.info("The current version {} has been successfully launched once and will no longer load Flyway.",
                    currentVersion);
        }

        String jwtSecretKey = System.getProperty("sa-token.jwt-secret-key");
        // The user did not specify the jws key
        if (StringUtils.isBlank(jwtSecretKey)) {
            if (StringUtils.isBlank(configJson.getJwtSecretKey())) {
                configJson.setJwtSecretKey(UUID.fastUUID().toString(true));
                ConfigUtils.setConfig(configJson);
            }
            System.setProperty("sa-token.jwt-secret-key",configJson.getJwtSecretKey());
        }

        return super.run(application);
    }

    public static void main(String[] args) {
        //ConfigUtils.pid();
        SpringApplication.run(Application.class, args);
    }
}
