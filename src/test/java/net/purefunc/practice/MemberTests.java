package net.purefunc.practice;

import lombok.extern.slf4j.Slf4j;
import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.config.security.LoginRequestDto;
import net.purefunc.practice.member.data.dto.MemberLoginResponseDTO;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class MemberTests {

    @Container
    private final static GenericContainer redisContainer = new GenericContainer("redis:7")
            .withEnv("REDIS_REPLICATION_MODE", "master")
            .withEnv("REDIS_PASSWORD", "rootroot")
            .withExposedPorts(6379);

    @Container
    private final static PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres:15");

    @Container
    private final static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtTokenService jwtTokenService;

    @LocalServerPort
    private Integer port = 0;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Test
    void test() {
        // login
        final HttpEntity<LoginRequestDto> loginRequestEntity = new HttpEntity<>(new LoginRequestDto("test", "test"));
        final ResponseEntity<String> loginResponseEntity = testRestTemplate.postForEntity(String.format("http://localhost:%d/api/v1.0/members:login", port), loginRequestEntity, String.class);
        testRestTemplate.postForEntity(String.format("http://localhost:%d/api/v1.0/members:login", port), loginRequestEntity, String.class);
        final String bearerToken = loginResponseEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        Assert.assertEquals("test", jwtTokenService.retrieveSubject(bearerToken.substring(7)));

        // get member

        // modify about

        // modify password

        // delete member

        // get member login records
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);
        final ResponseEntity<CustomPageImpl<MemberLoginResponseDTO>> memberLoginResponseEntity = testRestTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members/records", port),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<>() {});
        Assert.assertEquals(2, memberLoginResponseEntity.getBody().getSize());
    }
}
