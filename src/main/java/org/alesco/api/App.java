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

            restConfiguration()
                    .port(8181)
                    .bindingMode(RestBindingMode.json)
                    .enableCORS(true);

            rest().get("/posts")
                    .produces("application/json")
                    .route()
                    .to("facebook:getPosts?userId={{fb-page}}&oAuthAppId={{fb-app-id}}&oAuthAppSecret={{fb-app-secret}}&oAuthAccessToken={{fb-access-token}}")
                    .bean(App.class, "map");

        }
    }

    public static List<PagePost> map(PagableList<Post> posts) {
        List<PagePost> pagePosts = new LinkedList<>();
        if (posts != null) {
            for(int i=0; i< posts.size(); i++) {
                Post p = posts.get(i);
                PagePost pp = new PagePost();
                pagePosts.add(pp);

                pp.setMessage(p.getMessage());
                pp.setPicture(p.getPicture() != null ? p.getPicture().toString() : null);
                pp.setLikes(p.getLikes() != null ? p.getLikes().size() : null);
                pp.setLink(p.getLink() != null ? p.getLink().toString() : null);
            }
        }
        return pagePosts;
    }

}
