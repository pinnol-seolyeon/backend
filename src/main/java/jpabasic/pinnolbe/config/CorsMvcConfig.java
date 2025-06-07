//package jpabasic.pinnolbe.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsMvcConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("https://frontend-seolyeon.vercel.app","https://frontend-psi-one-31.vercel.app","http://localhost:3000") // 프론트 주소 명시
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true) // ★ 필수! credentials: 'include'와 함께 써야 함
//                .exposedHeaders("Set-Cookie");
//    }
//}
