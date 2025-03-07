package com.example.atipera;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApplication
@RestController
@RequestMapping("/github")
public class AtiperaApplication {

    private final WebClient webClient = WebClient.create("https://api.github.com");

    public static void main(String[] args) {
        SpringApplication.run(AtiperaApplication.class, args);
    }

    @GetMapping("/users/{username}/repos")
    public ResponseEntity<?> getUserRepositories(@PathVariable String username) {
        try {
            List<Map<String, Object>> repos = webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();

            if (repos == null) return ResponseEntity.notFound().build();

            List<Map<String, Object>> filteredRepos = repos.stream()
                .filter(repo -> !(boolean) repo.get("fork"))
                .map(repo -> Map.of(
                    "name", repo.get("name"),
                    "owner", ((Map<?, ?>) repo.get("owner")).get("login"),
                    "branches", getBranches(username, (String) repo.get("name"))
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(filteredRepos);

        } catch (Exception e) {
            return ResponseEntity.status(404)
                .body(Map.of("status", 404, "message", "User not found or does not exist"));
        }
    }

    private List<Map<String, String>> getBranches(String username, String repoName) {
        return webClient.get()
            .uri("/repos/{username}/{repo}/branches", username, repoName)
            .retrieve()
            .bodyToFlux(Map.class)
            .map(branch -> Map.of(
                "name", (String) branch.get("name"),
                "lastCommitSha", (String) ((Map<?, ?>) branch.get("commit")).get("sha")
            ))
            .collectList()
            .block();
    }

    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    static class AtiperaIntegrationTest {

        @LocalServerPort
        private int port;

        private final TestRestTemplate restTemplate = new TestRestTemplate();

        @Test
        void shouldFetchUserRepositories() {
            String username = "Herbert-Moore";
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/github/users/" + username + "/repos", String.class);

            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            assertThat(response.getBody()).contains("name").contains("owner").contains("branches");
        }

        @Test
        void shouldReturn404ForNonExistingUser() {
            String username = "Non-Existing-User";
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/github/users/" + username + "/repos", String.class);

            assertThat(response.getStatusCodeValue()).isEqualTo(404);
            assertThat(response.getBody()).contains("\"status\": 404");
        }
    }
}
