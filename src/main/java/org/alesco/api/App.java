package org.alesco.api;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import facebook4j.PagableList;
import facebook4j.Post;


@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {

        public void configure() throws Exception {

            LocalCache postsCache = new LocalCache();

            restConfiguration()
                    .port(8080)
                    .bindingMode(RestBindingMode.json)
                    .enableCORS(true);

            rest().get("/posts")
                    .produces("application/json")
                    .route()
                    .log("Requested posts from page")
                    .setBody().method(postsCache, "get");

            rest().post("/message")
                    .param().name("Message-Name").dataType("String").required(true).endParam()
                    .param().name("Message-Mail").dataType("String").required(true).endParam()
                    .param().name("Message-Subject").dataType("String").required(true).endParam()
                    .param().name("Message-Text").dataType("String").required(true).endParam()
                    .param().name("Message-Phone").dataType("String").required(false).endParam()
                    .produces("text/plain")
                    .bindingMode(RestBindingMode.off)
                    .route()
                    .validate(header("Message-Name").isNotNull())
                    .validate(header("Message-Mail").isNotNull())
                    .validate(header("Message-Subject").isNotNull())
                    .validate(header("Message-Text").isNotNull())
                    .log("Received message from user")
                    .to("log:org.alesco?level=INFO&showHeaders=true")
                    .to("seda:send-mail?waitForTaskToComplete=Never")
                    .setBody().constant("Messages enqueued")
                    .removeHeaders("*")
                    .setHeader("Access-Control-Allow-Origin", constant("*"))
                    .setHeader("Content-Type", constant("text/plain"));

            rest().get("/health")
                    .produces("text/plain")
                    .route()
                    .transform().constant("OK");

            from("timer:clock?period=300000&delay=0")
                    .log("Retrieving new data from Facebook")
                    .to("facebook:getPosts?reading.limit=60&reading.fields=message,created_time,id,link,full_picture,type,likes.limit(1).summary(true)&userId={{fb-page}}&oAuthAppId={{fb-app-id}}&oAuthAppSecret={{fb-app-secret}}&oAuthAccessToken={{fb-access-token}}")
                    .bean(App.class, "map")
                    .bean(postsCache, "save");

            from("seda:send-mail")
                    .multicast()
                        .to("direct:send-mail-to-company")
                        .to("direct:send-mail-to-user")
                    .end()
                    .log("Mail effectively sent");

            from("direct:send-mail-to-company")
                    .removeHeaders("*", "Message-*")
                    .setBody().simple("{{mail-company-template}}")
                    .setHeader("From", simple("{{mail-company-from}}"))
                    .setHeader("To", simple("{{mail-company-recipient}}"))
                    .setHeader("Reply-To", header("Message-Mail"))
                    .setHeader("Subject", simple("{{mail-subject-template}}"))
                    .removeHeaders("Message-*")
                    .to("smtps://{{mail-server}}?username={{mail-user}}&password={{mail-pass}}");

            from("direct:send-mail-to-user")
                    .removeHeaders("*", "Message-*")
                    .setBody().simple("{{mail-user-template}}")
                    .setHeader("From", simple("{{mail-user-from}}"))
                    .setHeader("To", header("Message-Mail"))
                    .setHeader("Subject", simple("{{mail-subject-template}}"))
                    .removeHeaders("Message-*")
                    .to("smtps://{{mail-server}}?username={{mail-user}}&password={{mail-pass}}");

        }
    }

    public static List<PagePost> map(PagableList<Post> posts) {
        List<PagePost> pagePosts = new LinkedList<>();
        if (posts != null) {
            for(int i=0; i< posts.size(); i++) {
                Post p = posts.get(i);
                if (!"photo".equals(p.getType())) {
                    continue;
                }

                PagePost pp = new PagePost();
                pagePosts.add(pp);

                pp.setMessage(p.getMessage());
                pp.setPicture(p.getFullPicture() != null ? p.getFullPicture().toString() : null);
                pp.setLikes(p.getLikes() != null && p.getLikes().getSummary() != null ? p.getLikes().getSummary().getTotalCount() : null);
                pp.setLink(p.getLink() != null ? p.getLink().toString() : null);
                pp.setDate(p.getCreatedTime());
            }
        }
        return pagePosts;
    }

}
