import com.yz.minded.contant.UserConstant;
import com.yz.minded.pojo.User;
import com.yz.minded.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class UpdateLoginUserAspect {

    @Resource
    private UserService userService;

    @After("controllerMethods()")
    public void updateLoginUser(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = null;
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest req) { request = req; break; }
        }
        if (request == null) return;   // 【修复】局部变量，无静态字段

        Object obj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (!(obj instanceof User loginUser)) return;

        User user = userService.getById(loginUser.getId());
        if (user == null) return;
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userService.getSafetyUser(user));
    }

    @Pointcut("execution(* com.yz.minded.controller.UserController.userLogin(..))" +
            "|| execution(* com.yz.minded.controller.UserController.updateUser(..))" +
            "|| execution(* com.yz.minded.controller.UserTagController.updateTags(..))" +
            "|| execution(* com.yz.minded.controller.UserController.upload(..))")
    public void controllerMethods() {}
}