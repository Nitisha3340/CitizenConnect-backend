package com.citizenconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI citizenConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CitizenConnect API")
                        .version("1.0")
                        .description("""
                                **Auth:** Sign up (no email verification step) → login (sends OTP to email) → verify-login returns JWT.

                                **OTP / email:** Always check the **HTTP status** and response body. If SMTP fails you should see **502** (not 200). With profile `local`, the OTP is also printed in the **backend console** (`DEV ONLY — OTP for …`). If status is 200 but no email: check Spam, recipient address, and port 587 / firewall.

                                **Try protected routes:** click **Authorize** and paste the **JWT only** (Swagger sends `Authorization: Bearer ...` automatically). You do **not** need to type the `Authorization` header again on `/auth/profile` or portal routes.

                                Roles: `CITIZEN`, `POLITICIAN`, `MODERATOR`, `ADMIN` — use matching `/citizen`, `/politician`, `/moderator`, `/admin` paths."""));
    }
}