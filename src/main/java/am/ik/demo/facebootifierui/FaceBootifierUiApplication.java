package am.ik.demo.facebootifierui;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
@RestController
@ConfigurationProperties(prefix = "bootifier")
public class FaceBootifierUiApplication {
    private String url;
    private final WebClient webClient;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FaceBootifierUiApplication(WebClient.Builder builder) {
        this.webClient = builder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .secure(spec -> spec.sslContext(SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)))
                        .compress(true)))
                .build();
    }

    @GetMapping("/")
    public ResponseEntity<String> redirect() {
        return ResponseEntity.status(HttpStatus.SEE_OTHER).header("Location", "/index.html").build();
    }

    @PostMapping("/")
    public Mono<String> bootify(@RequestBody Mono<String> source) {
        return this.webClient.post()
                .uri(this.url)
                .contentType(MediaType.TEXT_PLAIN)
                .body(source, String.class)
                .retrieve()
                .bodyToMono(String.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(FaceBootifierUiApplication.class, args);
    }
}
