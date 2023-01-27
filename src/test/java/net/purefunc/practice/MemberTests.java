package net.purefunc.practice;

import lombok.extern.slf4j.Slf4j;
import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.config.security.LoginRequestDto;
import net.purefunc.practice.member.data.dto.MemberAboutRequestDTO;
import net.purefunc.practice.member.data.dto.MemberLoginResponseDTO;
import net.purefunc.practice.member.data.dto.MemberPasswordRequestDTO;
import net.purefunc.practice.member.data.dto.MemberResponseDTO;
import net.purefunc.practice.member.data.enu.MemberRole;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    private RestTemplate restTemplate;

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
        final LoginRequestDto loginRequestDto = new LoginRequestDto("test", "test");
        final ResponseEntity<String> loginResponseEntity = login(loginRequestDto);
        final String bearerToken = getBearerToken(loginResponseEntity);
        Assert.assertEquals(200, loginResponseEntity.getStatusCodeValue());
        Assert.assertEquals("test", jwtTokenService.retrieveSubject(bearerToken.substring(7)));

        // query member
        final ResponseEntity<MemberResponseDTO> queryMembersEntity = queryMembers(bearerToken);
        Assert.assertEquals(200, queryMembersEntity.getStatusCodeValue());
        Assert.assertTrue((queryMembersEntity.getBody().getId() > 0L));
        Assert.assertEquals("test", queryMembersEntity.getBody().getUsername());
        Assert.assertEquals("", queryMembersEntity.getBody().getAbout());
        Assert.assertTrue(StringUtils.isNotBlank(queryMembersEntity.getBody().getAvatarLink()));
        Assert.assertEquals(MemberRole.ROLE_USER, queryMembersEntity.getBody().getRole());
        Assert.assertTrue(StringUtils.isNotBlank(queryMembersEntity.getBody().getCreatedDateStr()));

        // modify about
        final ResponseEntity<Object> modifyAboutEntity = modifyAbout(bearerToken);
        Assert.assertEquals(204, modifyAboutEntity.getStatusCodeValue());

        // modify password
        final ResponseEntity<Object> modifyPasswordEntity = modifyPassword(bearerToken);
        Assert.assertEquals(204, modifyPasswordEntity.getStatusCodeValue());

        // login with new password
        final LoginRequestDto newLoginRequestDto = new LoginRequestDto("test", "test123");
        final ResponseEntity<String> newLoginRequestEntity = login(newLoginRequestDto);
        final String newBearerToken = getBearerToken(newLoginRequestEntity);
        Assert.assertEquals(200, newLoginRequestEntity.getStatusCodeValue());
        Assert.assertEquals("test", jwtTokenService.retrieveSubject(newBearerToken.substring(7)));

        // query member for new profile
        final ResponseEntity<MemberResponseDTO> newQueryMembersEntity = queryMembers(newBearerToken);
        Assert.assertEquals(200, newQueryMembersEntity.getStatusCodeValue());
        Assert.assertEquals("about", newQueryMembersEntity.getBody().getAbout());

        // get member login records
        final ResponseEntity<CustomPageImpl<MemberLoginResponseDTO>> memberLoginResponseEntity = queryMemberLoginRecords(newBearerToken);
        Assert.assertEquals(200, memberLoginResponseEntity.getStatusCodeValue());
        Assert.assertEquals(2, memberLoginResponseEntity.getBody().numberOfElements);

        // remove member
        final LoginRequestDto adminLoginRequestDto = new LoginRequestDto("admin", "admin");
        final ResponseEntity<String> adminLoginResponseEntity = login(adminLoginRequestDto);
        final String adminBearerToken = getBearerToken(adminLoginResponseEntity);
        Assert.assertEquals(200, adminLoginResponseEntity.getStatusCodeValue());
        Assert.assertEquals("admin", jwtTokenService.retrieveSubject(adminBearerToken.substring(7)));

        final ResponseEntity<Object> deleteMemberEntity = removeMember(adminBearerToken);
        Assert.assertEquals(204, deleteMemberEntity.getStatusCodeValue());
    }

    private String getBearerToken(ResponseEntity<String> responseEntity) {
        return responseEntity.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
    }

    private ResponseEntity<String> login(LoginRequestDto loginRequestDto) {
        return restTemplate.postForEntity(
                String.format("http://localhost:%d/api/v1.0/members:login", port),
                new HttpEntity<>(loginRequestDto),
                String.class);
    }

    private ResponseEntity<MemberResponseDTO> queryMembers(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members", port),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                MemberResponseDTO.class);
    }

    private ResponseEntity<Object> modifyAbout(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members:modifyAbout", port),
                HttpMethod.PATCH,
                new HttpEntity<>(new MemberAboutRequestDTO("about"), httpHeaders),
                Object.class);
    }

    private ResponseEntity<Object> modifyPassword(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members:modifyPassword", port),
                HttpMethod.PATCH,
                new HttpEntity<>(new MemberPasswordRequestDTO("test", "test123"), httpHeaders),
                Object.class);
    }

    private ResponseEntity<CustomPageImpl<MemberLoginResponseDTO>> queryMemberLoginRecords(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members/records", port),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<>() {
                });
    }

    private ResponseEntity<Object> removeMember(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/members/%s", port, "test"),
                HttpMethod.DELETE,
                new HttpEntity<>(httpHeaders),
                Object.class);
    }
}
