package com.nttdata.bank.gateway;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Published OpenAPI contract of the API gateway.
 * Routes are declared in configuration, so each prefix is documented here.
 */
@Configuration
public class OpenApiConfig {

    /**
     * @return contract metadata, the bearer scheme, and every proxied prefix
     */
    @Bean
    public OpenAPI gatewayOpenApi() {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("API gateway")
                        .version("1.0.0")
                        .description("Single entry point. Login and registration are public. Every other /api route requires a bearer token and is forwarded through Eureka."))
                .components(new Components().addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
        route(api, "/api/v1/auth", "ms-auth", true);
        route(api, "/api/v1/customers", "ms-customer", false);
        route(api, "/api/v1/accounts", "ms-account", false);
        route(api, "/api/v1/movements", "ms-account", false);
        route(api, "/api/v1/transfers", "ms-account", false);
        route(api, "/api/v1/debit-cards", "ms-account", false);
        route(api, "/api/v1/account-reports", "ms-account", false);
        route(api, "/api/v1/loans", "ms-credit", false);
        route(api, "/api/v1/credit-cards", "ms-credit", false);
        route(api, "/api/v1/credit-movements", "ms-credit", false);
        route(api, "/api/v1/debt-status", "ms-credit", false);
        route(api, "/api/v1/credit-reports", "ms-credit", false);
        route(api, "/api/v1/wallets", "ms-yanki", false);
        return api;
    }

    private static void route(OpenAPI api, String prefix, String service, boolean open) {
        PathParameter remainder = new PathParameter();
        remainder.setName("path");
        remainder.setDescription("Remainder of the downstream path");
        remainder.setSchema(new StringSchema());
        remainder.setRequired(true);
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(remainder);
        PathItem item = new PathItem();
        item.setParameters(parameters);
        item.setGet(operation(service, open));
        item.setPost(operation(service, open));
        item.setPut(operation(service, open));
        item.setPatch(operation(service, open));
        item.setDelete(operation(service, open));
        api.path(prefix + "/{path}", item);
    }

    private static Operation operation(String service, boolean open) {
        Operation operation = new Operation()
                .summary("Forwarded to " + service)
                .description("The gateway proxies this call to " + service + " through Eureka.")
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Downstream response")));
        if (!open) {
            operation.addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
        return operation;
    }
}
