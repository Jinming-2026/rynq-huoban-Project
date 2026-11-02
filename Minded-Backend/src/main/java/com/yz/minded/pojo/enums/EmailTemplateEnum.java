package com.yz.minded.pojo.enums;

import lombok.Getter;

/**
 * 定义发送邮箱的模板
 */
@Getter
public enum EmailTemplateEnum {

    // 验证码邮件
    VERIFICATION_CODE_EMAIL_HTML("<html><body>用户您好，您的验证码是:<h1>%s</h1>请在五分钟内完成验证</body></html>","Minded");

    private final String template;
    private final String subject;

    EmailTemplateEnum(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }

    public String set(String captcha) {
        return String.format(this.template, captcha);
    }

}
