package net.purefunc.practice;

import lombok.extern.slf4j.Slf4j;
import net.purefunc.practice.config.security.JwtTokenService;
import net.purefunc.practice.config.security.LoginRequestDto;
import net.purefunc.practice.member.data.dto.MemberAboutRequestDTO;
import net.purefunc.practice.member.data.dto.MemberLoginResponseDTO;
import net.purefunc.practice.member.data.dto.MemberPasswordRequestDTO;
import net.purefunc.practice.member.data.dto.MemberResponseDTO;
import net.purefunc.practice.member.data.enu.MemberRole;
import net.purefunc.practice.wallet.data.dto.WalletOpResponseDTO;
import net.purefunc.practice.wallet.data.dto.WalletRequestDTO;
import net.purefunc.practice.wallet.data.dto.WalletResponseDTO;
import net.purefunc.practice.wallet.data.dto.WalletTransferRequestDTO;
import net.purefunc.practice.wallet.data.vo.WalletVO;
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

import java.math.BigDecimal;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class WalletTests {

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

        // query wallet
        final ResponseEntity<WalletResponseDTO> queryWalletsEntity = queryWallets(bearerToken);
        Assert.assertEquals(200, queryWalletsEntity.getStatusCodeValue());
        Assert.assertEquals(0, queryWalletsEntity.getBody().getBalance().compareTo(BigDecimal.ZERO));

        // deposit
        final ResponseEntity<WalletOpResponseDTO> depositEntity = deposit(bearerToken, new BigDecimal("100.0"));
        Assert.assertEquals(200, depositEntity.getStatusCodeValue());
        Assert.assertEquals(0, depositEntity.getBody().getToMemberBalance().compareTo(new BigDecimal("100.0")));
        final ResponseEntity<WalletResponseDTO> queryWalletsAfterDepositEntity = queryWallets(bearerToken);
        Assert.assertEquals(200, queryWalletsAfterDepositEntity.getStatusCodeValue());
        Assert.assertEquals(0, queryWalletsAfterDepositEntity.getBody().getBalance().compareTo(new BigDecimal("100.0")));

        // withdraw
        final ResponseEntity<WalletOpResponseDTO> withdrawEntity = withdraw(bearerToken, new BigDecimal("1.0"));
        Assert.assertEquals(200, withdrawEntity.getStatusCodeValue());
        Assert.assertEquals(0, withdrawEntity.getBody().getToMemberBalance().compareTo(new BigDecimal("99.0")));
        final ResponseEntity<WalletResponseDTO> queryWalletsAfterWithdrawEntity = queryWallets(bearerToken);
        Assert.assertEquals(200, queryWalletsAfterWithdrawEntity.getStatusCodeValue());
        Assert.assertEquals(0, queryWalletsAfterWithdrawEntity.getBody().getBalance().compareTo(new BigDecimal("99.0")));

        // login
        final LoginRequestDto newLoginRequestDto = new LoginRequestDto("test2", "test");
        final ResponseEntity<String> newLoginResponseEntity = login(newLoginRequestDto);
        final String newBearerToken = getBearerToken(newLoginResponseEntity);
        Assert.assertEquals(200, newLoginResponseEntity.getStatusCodeValue());
        Assert.assertEquals("test2", jwtTokenService.retrieveSubject(newBearerToken.substring(7)));

        // query member
        final ResponseEntity<MemberResponseDTO> memberResponseEntity = queryMembers(newBearerToken);
        Assert.assertEquals(200, memberResponseEntity.getStatusCodeValue());
        Assert.assertEquals("test2", memberResponseEntity.getBody().getUsername());

        // transfer
        final ResponseEntity<WalletOpResponseDTO> transferEntity = transfer(bearerToken, memberResponseEntity.getBody().getId(), new BigDecimal("89.0"));
        Assert.assertEquals(200, transferEntity.getStatusCodeValue());
        Assert.assertEquals(0, transferEntity.getBody().getFromMemberBalance().compareTo(new BigDecimal("10.0")));
        Assert.assertEquals(0, transferEntity.getBody().getToMemberBalance().compareTo(new BigDecimal("89.0")));

        // query wallet 1
        final ResponseEntity<WalletResponseDTO> queryWalletsEntity1 = queryWallets(bearerToken);
        Assert.assertEquals(200, queryWalletsEntity1.getStatusCodeValue());
        Assert.assertEquals(0, queryWalletsEntity1.getBody().getBalance().compareTo(new BigDecimal("10.0")));

        // query wallet 2
        final ResponseEntity<WalletResponseDTO> queryWalletsEntity2 = queryWallets(newBearerToken);
        Assert.assertEquals(200, queryWalletsEntity2.getStatusCodeValue());
        Assert.assertEquals(0, queryWalletsEntity2.getBody().getBalance().compareTo(new BigDecimal("89.0")));

        // query wallet 1 transactions
        final ResponseEntity<CustomPageImpl<WalletVO>> queryWalletsTransactions1 = queryWalletsTransactions(bearerToken);
        Assert.assertEquals(200, queryWalletsTransactions1.getStatusCodeValue());
        Assert.assertEquals(3, queryWalletsTransactions1.getBody().numberOfElements);

        // query wallet 2 transactions
        final ResponseEntity<CustomPageImpl<WalletVO>> queryWalletsTransactions2 = queryWalletsTransactions(newBearerToken);
        Assert.assertEquals(200, queryWalletsTransactions2.getStatusCodeValue());
        Assert.assertEquals(1, queryWalletsTransactions2.getBody().numberOfElements);
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

    private ResponseEntity<WalletResponseDTO> queryWallets(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/wallets", port),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                WalletResponseDTO.class);
    }

    private ResponseEntity<WalletOpResponseDTO> deposit(String bearerToken, BigDecimal amount) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/wallets:deposit", port),
                HttpMethod.POST,
                new HttpEntity<>(new WalletRequestDTO(amount), httpHeaders),
                WalletOpResponseDTO.class);
    }

    private ResponseEntity<WalletOpResponseDTO> withdraw(String bearerToken, BigDecimal amount) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/wallets:withdraw", port),
                HttpMethod.POST,
                new HttpEntity<>(new WalletRequestDTO(amount), httpHeaders),
                WalletOpResponseDTO.class);
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

    private ResponseEntity<WalletOpResponseDTO> transfer(String bearerToken, Long toMemberId, BigDecimal amount) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/wallets:transfer", port),
                HttpMethod.POST,
                new HttpEntity<>(new WalletTransferRequestDTO(toMemberId, amount), httpHeaders),
                WalletOpResponseDTO.class);
    }

    private ResponseEntity<CustomPageImpl<WalletVO>> queryWalletsTransactions(String bearerToken) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);

        return restTemplate.exchange(
                String.format("http://localhost:%d/api/v1.0/wallets/transactions", port),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<>() {
                });
    }
}
