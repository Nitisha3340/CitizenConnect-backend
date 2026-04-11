package com.citizenconnect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * On {@code local} profile: logs whether SMTP credentials are present and reminds about OTP console logging.
 */
@Component
@Profile("local")
public class LocalMailStartupHints implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalMailStartupHints.class);

    private final Environment environment;

    public LocalMailStartupHints(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String user = environment.getProperty("spring.mail.username", "");
        String pass = environment.getProperty("spring.mail.password", "");
        boolean passPresent = pass != null && !pass.replaceAll("\\s+", "").isEmpty();

        if (user == null || user.isBlank() || !passPresent) {
            log.warn("""
                    [local] Mail is NOT configured (spring.mail.username / spring.mail.password missing).
                    Copy src/main/resources/application-mail-local.properties.example to application-mail-local.properties
                    or set MAIL_USERNAME and MAIL_PASSWORD. OTP endpoints will fail until mail is configured.""");
        } else {
            log.info("[local] SMTP sender configured as: {}. If Swagger returns 200 but no email: check Spam, "
                    + "correct recipient address, and firewall for port 587.", user);
        }

        if (Boolean.parseBoolean(environment.getProperty("app.mail.log-otp-to-console", "false"))) {
            log.info("[local] app.mail.log-otp-to-console=true - OTP values will appear in this console (DEV ONLY).");
        }

        if (Boolean.parseBoolean(environment.getProperty("spring.mail.properties.mail.debug", "false"))) {
            log.info("[local] spring.mail.properties.mail.debug=true - verbose SMTP protocol logging is ON.");
        }
    }
}
