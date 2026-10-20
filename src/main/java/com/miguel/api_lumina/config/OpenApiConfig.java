package com.miguel.api_lumina.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresá el token JWT obtenido desde POST /api/auth/login. " +
                "Formato: el token sin prefijo Bearer (Swagger lo agrega automáticamente)."
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Lumina")
                        .version("1.0.0")
                        .description(
                                "API REST del sistema Lumina para gestión de cuidados de pacientes con Alzheimer.\n\n" +
                                "**Cómo autenticarse:**\n" +
                                "1. Ejecutá `POST /api/auth/login` con tu email y contraseña.\n" +
                                "2. Copiá el campo `token` de la respuesta.\n" +
                                "3. Hacé clic en el botón **Authorize** (arriba a la derecha) y pegá el token.\n" +
                                "4. Swagger lo enviará automáticamente en cada petición.\n\n" +
                                "**Endpoints públicos (no requieren token):**\n" +
                                "- `POST /api/auth/login`\n" +
                                "- `POST /api/usuarios` (registro de nuevo usuario)\n\n" +
                                "**Todos los demás endpoints requieren token JWT válido.**"
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
