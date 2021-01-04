package com.segment.proxyservice.controllers;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.DockerComposeContainer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {ProxyControllerTest.Initializer.class})
public class ProxyControllerTest {

    @LocalServerPort
    private int port;

    private static JedisPool JEDIS;
    private static DockerComposeContainer dockerComposeContainer;

    static {
        dockerComposeContainer = new DockerComposeContainer(new File("src/test/resources/docker-compose-test.yml"));
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "server.port=8080",
                    "redis.hostname=" + dockerComposeContainer.getServiceHost("redis", 6379),
                    "redis.port=6379",
                    "redis.expiry=2",
                    "redis.capacity=2"
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @BeforeAll
    public static void init() {
        dockerComposeContainer.start();

        JEDIS = new JedisPool("localhost", 6379);
    }

    @BeforeEach
    public void start() {
        RestAssured.port = port;
    }

    @Test
    public void test_should_return_404_due_missing_key() {
        given()
            .get("/cache/missing_key")
        .then()
            .assertThat()
            .statusCode(404);

    }

    @Test
    public void test_should_return_200_due_existing_key() {
        try (Jedis jedis = JEDIS.getResource()) {
            jedis.set("name", "rafael");
        }

        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));

    }

    @Test
    public void making_sure_value_is_coming_from_proxy_internal_cache() {
        try (Jedis jedis = JEDIS.getResource()) {
            jedis.set("name", "rafael");
        }

        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));

        try (Jedis jedis = JEDIS.getResource()) {
            jedis.set("name", "nataly");
        }

        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));
    }

    @Test
    public void should_remove_key_due_cache_expiration() throws Exception {
        try (Jedis jedis = JEDIS.getResource()) {
            jedis.set("name", "rafael");
        }

        // checks the key is cached
        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));

        // removes key from redis
        try (Jedis jedis = JEDIS.getResource()) {
            jedis.del("name");
        }

        // checks the key is still cached
        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));

        TimeUnit.SECONDS.sleep(2);

        // after 2 seconds checks that the key has been removed
        given()
            .get("/cache/name")
        .then()
            .assertThat()
            .statusCode(404);
    }

    @Test
    public void should_evict_after_max_capacity_is_reached() throws Exception {
        // max capacity was set as 2
        try (Jedis jedis = JEDIS.getResource()) {
            jedis.set("first", "rafael");
            jedis.set("second", "nataly");
            jedis.set("third", "daniel");
        }

        given()
            .get("/cache/first")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("rafael"));

        // calls second key twice to put it on top in the cache
        given()
            .get("/cache/second")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("nataly"));

        given()
            .get("/cache/second")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("nataly"));

        // with this call first should be evicted
        given()
            .get("/cache/third")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("daniel"));

        try (Jedis jedis = JEDIS.getResource()) {
            jedis.del("first", "second", "third");
        }

        given()
            .get("/cache/first")
        .then()
            .assertThat()
            .statusCode(404);

        given()
            .get("/cache/second")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("nataly"));

        given()
            .get("/cache/third")
        .then()
            .assertThat()
            .statusCode(200)
            .body(equalTo("daniel"));
    }
}