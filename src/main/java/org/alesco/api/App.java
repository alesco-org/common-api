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

            rest().get("/health")
                    .produces("text/plain")
                    .route()
                    .transform().constant("OK");

            from("timer:clock?period=300000&delay=0")
                    .log("Retrieving new data from Facebook")
                    .to("facebook:getPosts?reading.limit=60&reading.fields=message,created_time,id,link,full_picture,type,likes.limit(1).summary(true)&userId={{fb-page}}&oAuthAppId={{fb-app-id}}&oAuthAppSecret={{fb-app-secret}}&oAuthAccessToken={{fb-access-token}}")
                    .bean(App.class, "map")
                    .bean(postsCache, "save");

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
