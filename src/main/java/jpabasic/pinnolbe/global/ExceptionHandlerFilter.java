package jpabasic.pinnolbe.global;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try{
            filterChain.doFilter(request, response);
        }catch(CustomException e){
            log.info("TokenException handler filter");
            setErrorResponse(HttpStatus.UNAUTHORIZED,response,e);
        }catch(RuntimeException e){
            log.info("RuntimeException handler filter");
            setErrorResponse(HttpStatus.FORBIDDEN,response,e);
        }catch(Exception e){
            log.info("Exception handler filter");
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,response,e);
        }
    }

    private void setErrorResponse(HttpStatus status,HttpServletResponse response,Exception e){
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String message=e.getMessage();
        if(e instanceof CustomException customEx){
            message=customEx.getErrorCode().getMessage();
            status=customEx.getErrorCode().getHttpStatus();
            response.setStatus(status.value());
        }

        String body = String.format("{\"code\": %d, \"message\": \"%s\"}", status.value(), message);
        try{
            response.getWriter().write(body);
        }catch(IOException ioException){
            log.error("failed to write error response",ioException);
        }
    }
}
