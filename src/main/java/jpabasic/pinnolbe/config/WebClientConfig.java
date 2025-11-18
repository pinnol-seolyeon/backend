package jpabasic.pinnolbe.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        //타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) //TCP 소켓 연결 타임아웃 (5초)
                .responseTimeout(Duration.ofMillis(5000)) //HTTP 응답 타임아웃
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS)) //데이터 수신 중 타임아웃
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))); //데이터 송신 중 타임아웃

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://kapi.kakao.com/v2/api/talk/memo/default/send")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .build();
    }

    @Bean
    @Qualifier("sseWebClient") //같은 타입(WebClient)의 Bean 여러 개 존재 -> 어떤 Bean을 사용할지 정확하게 지정
    public WebClient sseWebClient() {
        HttpClient httpClient = HttpClient.create()
                .keepAlive(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) //연결 시도 30초까지
                .responseTimeout(Duration.ofSeconds(600)) //응답 대기 10분
                .doOnConnected(conn->
                        conn.addHandlerLast(new ReadTimeoutHandler(600))
                                .addHandlerLast(new WriteTimeoutHandler(600))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
