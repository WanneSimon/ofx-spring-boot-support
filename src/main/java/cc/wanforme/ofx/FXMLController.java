package cc.wanforme.ofx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * controller in *.fxml<br>
 * this annotation is not necessary, you can use @Component as well.
 * It's just a mark so we can get controller beans from bean factory.
 * <br>
 * 这只是一个的标记，它完全可以使用 @Component 替代。
 * 目的就是为了让 spring 管理 fxml 中的 controller
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface FXMLController {

//	String value();
}
