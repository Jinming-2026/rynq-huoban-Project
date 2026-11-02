import com.yz.minded.common.BaseResponse;
import com.yz.minded.pojo.FollowRelationship;
import com.yz.minded.service.FollowRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Aspect
@Component
public class RegisterUserAspect {

    @Resource
    private FollowRelationshipService followRelationshipService;

    @Around("execution(* com.yz.minded.controller.UserController.userRegister(..))")
    public Object setDefaultFans(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();   // 【修复②】保留原返回体

        // 【修复③】安全提取 userId
        if (result instanceof BaseResponse<?> baseResponse
                && baseResponse.getData() instanceof Long userId
                && userId > 0) {
            try {
                FollowRelationship fr = new FollowRelationship();
                fr.setFollowerId(1L);
                fr.setFollowedId(userId);
                if (!followRelationshipService.save(fr)) {
                    log.warn("管理员默认关注失败，userId={}", userId);   // 不阻断注册
                }
            } catch (Exception e) {
                log.error("管理员默认关注异常，userId={}", userId, e);   // 不阻断注册
            }
        }
        return result;   // 【修复②】返回原 result，而不是 ResultUtils.success(userId)
    }
}