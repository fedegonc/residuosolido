package com.residuosolido.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "EcoSolicitud API",
                version = "v1",
                description = "API de gestión de solicitudes de recolección de residuos reciclables en la frontera Rivera-Livramento."
        )
)
@SuppressWarnings("PMD.UseUtilityClass")
public class ResiduoSolidoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResiduoSolidoApplication.class, args);
    }

}
