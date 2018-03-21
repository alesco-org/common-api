package org.alesco.api;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Plants extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        LocalCache plantsStatus = new LocalCache(false);

        from("quartz2://water?cron={{plants.water.cron}}")
                .log("Setting plants water status (true)")
                .setBody(constant(true))
                .bean(plantsStatus, "save");

        from("timer:plants?period={{plants.alert.period}}")
                .choice()
                    .when(method(plantsStatus, "get").isEqualTo(true))
                        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                        .log("Calling webhook to trigger plants water notification")
                        .to("netty4-http:{{plants.webhook}}")
                .end();

        rest().post("/plants/reset")
                .produces("application/json")
                .route()
                .log("Resetting plants water status (false)")
                .setBody(constant(false))
                .bean(plantsStatus, "save");

        rest().post("/plants/set")
                .produces("application/json")
                .route()
                .log("Activating plants water status (true)")
                .setBody(constant(true))
                .bean(plantsStatus, "save");

    }
}
