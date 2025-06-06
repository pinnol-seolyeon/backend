package jpabasic.pinnolbe.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
// 로드밸런서 대상 그룹 healthy체크를 하도록 함
public class HealthCheckController {

    @GetMapping("/health-check")
    public ResponseEntity<String> helathCheck(){
        return ResponseEntity.ok("success");
    }
}
