package jpabasic.pinnolbe.global.logging.aop;

import jakarta.servlet.http.HttpServletRequest;
import jpabasic.pinnolbe.global.logging.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SlackService slackService;

    //1. Controller: 요청 시작과 끝 지점
    @Around("execution(* jpabasic.pinnolbe.controller..*(..))")
    public Object handleControllerLog(ProceedingJoinPoint joinPoint) throws Throwable{
        long start=System.currentTimeMillis();

        //Context 초기화
        HttpServletRequest request=((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        LogContext context=new LogContext(request.getRemoteUser(),request.getMethod());
        LogHolder.set(context);

        try{
            Object result=joinPoint.proceed();
            long totalTime=System.currentTimeMillis()-start;

            //성공 시 전체 로그 채널로 전송
            slackService.sendFullLog(context,totalTime,"SUCCESS");
            return result;
        }catch(Exception e){
            //에러 발생 시 에러 채널 + 전체 채널 전송
            slackService.sendErrorLog(context,e,joinPoint.getSignature().getName());
            throw e;
        }finally{
            LogHolder.clear();
        }
    }

    //2. Service/Repository: 개별 메서드 실행 시간 측정
    @Around("execution(* jpabasic.pinnolbe.service..*(..)) || execution(* jpabasic.pinnolbe.repository..*(..))")
    public Object handleLayerLog(ProceedingJoinPoint joinPoint) throws Throwable{
        long start=System.currentTimeMillis();
        Object result= joinPoint.proceed();
        long executionTime=System.currentTimeMillis()-start;

        LogContext context = LogHolder.get();
        if (context != null) {
            context.methodDetails.add(String.format("[%s] - %dms", joinPoint.getSignature().toShortString(), executionTime));
        }
        return result;
    }
}
