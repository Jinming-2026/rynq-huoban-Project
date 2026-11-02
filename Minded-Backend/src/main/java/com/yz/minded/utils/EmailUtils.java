package com.yz.minded.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class EmailUtils {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;    // 发件人

    /**
     * 发送html的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 是否成功
     */
    @SneakyThrows(Exception.class)
    public boolean sendHtmlEmail(String subject, String content, String... to){
        // 创建邮件消息
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        // 设置收件人
        helper.setTo(to);
        // 设置邮件主题
        helper.setSubject(subject);
        // 设置邮件内容
        helper.setText(content, true);

        // 发送邮件
        mailSender.send(mimeMessage);
        log.info("发送邮件成功");
        return true;

    }
}
