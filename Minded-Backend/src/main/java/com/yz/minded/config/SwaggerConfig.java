package com.yz.minded.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 自定义 swagger 2 文档接口的配置
 * @author yz
 */
@Configuration  // 说明该类为配置类
@EnableSwagger2     // 开启 swagger 2
@Profile({"dev", "test"})   // 版本控制访问
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        // 创建一个 swagger 的 Bean 实例
       return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .enable(true)   // TODO 是否开启（true - 开启，false - 隐藏。生产环境建议隐藏）
                .select()   // 扫描的路径包，设置basePackage会将包下所有被 @Api 标记类的所有方法作为 api
                .apis(RequestHandlerSelectors.basePackage("com.yz.minded.controller"))
                .paths(PathSelectors.any())     // 指定路径处理，PathSelectors.any() 代表所有路径
                .build();

    }
    // 基本信息
    private ApiInfo apiInfo() {
        Contact contact = new Contact(
                "Yz", // 作者姓名
                "Yz.cn", // 作者网址
                "dcyuhan@163.com"); // 作者邮箱
        return new ApiInfoBuilder()
                .title("Minded 项目接口文档")     // 设置文档标题（API名称）
                .description("众里寻他千百度，慕然回首那人却在灯火阑珊处")    // 文档描述
                .termsOfServiceUrl("http://localhost:8080/")    //服务条款URL
                .version("1.0")     // 版本号
                .termsOfServiceUrl("https://www.baidu.com") // 跳转连接
                .contact(contact)
                .build();
    }
}
