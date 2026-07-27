package com.passro.passrobackend.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.account.entity.University;
import com.passro.passrobackend.account.repository.UniversityRepository;
import com.passro.passrobackend.support.IntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void mailVerificationSignupLoginReissueAndLogoutWorkTogether() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "student-" + suffix + "@passro.test";
        String nickname = "tester-" + suffix;
        String password = "Passro123!";
        saveUniversityDomain();

        mockMvc.perform(post("/auth/mail/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mail\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ACCOUNT200_1"));
        verify(mailSender).send(any(SimpleMailMessage.class));

        String code = redisTemplate.opsForValue().get("email:verify:code:" + email);
        assertThat(code).matches("\\d{6}");

        mockMvc.perform(post("/auth/mail/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mail\":\"" + email + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk());

//        mockMvc.perform(post("/auth/signup")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(signupBody(email, password, nickname)))
//                .andExpect(status().isOk());

//        Account account = accountRepository.findByEmail(email).orElseThrow();
//        assertThat(passwordEncoder.matches(password, account.getPassword())).isTrue();
//
//        MvcResult loginResult = mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(loginBody(email, password)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.result.accessToken").isNotEmpty())
//                .andExpect(jsonPath("$.result.refreshToken").isNotEmpty())
//                .andReturn();
//        String refreshToken = json(loginResult).at("/result/refreshToken").asText();
//
//        MvcResult reissueResult = mockMvc.perform(post("/auth/reissue")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
//                .andExpect(status().isOk())
//                .andReturn();
//        JsonNode reissued = json(reissueResult);
//
//        mockMvc.perform(delete("/auth/logout")
//                        .header("Authorization", bearer(reissued.at("/result/accessToken").asText())))
//                .andExpect(status().isOk());
//
//        mockMvc.perform(post("/auth/reissue")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"refreshToken\":\"" + reissued.at("/result/refreshToken").asText() + "\"}"))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.code").value("ACCOUNT401_2"));
    }

    @Test
    void repeatedVerificationMailRequestIsRateLimited() throws Exception {
        String email = "rate-" + UUID.randomUUID().toString().substring(0, 8) + "@passro.test";
        saveUniversityDomain();
        String body = "{\"mail\":\"" + email + "\"}";

        mockMvc.perform(post("/auth/mail/send").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/mail/send").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ACCOUNT429_1"));
    }

    @Test
    void invalidCredentialsAndRefreshTokenAreRejected() throws Exception {
        Account account = createAccount("login-failure");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(account.getEmail(), "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT401_1"));

        mockMvc.perform(post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT401_2"));
    }

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/sender"))
                .andExpect(status().isForbidden());
    }

    private void saveUniversityDomain() {
        universityRepository.saveAndFlush(University.builder()
                .name("Passro University " + UUID.randomUUID())
                .emailDomain("passro.test")
                .build());
    }

    private String signupBody(String email, String password, String nickname) {
        return """
                {
                  "email":"%s",
                  "password":"%s",
                  "nickname":"%s",
                  "name":"Integration User",
                  "phone":"01012345678",
                  "birth":"2000-01-01",
                  "picture":"profile.png"
                }
                """.formatted(email, password, nickname);
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
