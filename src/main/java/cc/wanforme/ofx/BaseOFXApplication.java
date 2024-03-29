package cc.wanforme.ofx;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class BaseOFXApplication extends Application {
	
	private static String[] launchArgs = {};
	private static Class<? extends BaseView> launchView;
	private static Class<? extends BaseOFXApplication> fxApp;
	private static Class<?> springApp;

	private static ConfigurableApplicationContext springContext;
	
	/** launch application ( SpringBoot and FXApplication )
	 * @param app 
	 * @param mainView 
	 * @param args 
	 */
	public static void launchOFX(Class<? extends BaseOFXApplication> app,
								 Class<?> springApp,
								 Class<? extends BaseView> mainView, String[] args) {
		if (app == springApp) {
			throw new RuntimeException("Fx main application's class can't be the same as spring application's class");
		}

		BaseOFXApplication.fxApp = app;
		BaseOFXApplication.springApp = springApp;
		launchArgs  = args;
		launchView = mainView;
		launch(app, launchArgs);
	}
	
	public BaseOFXApplication() {}
	public BaseOFXApplication(String[] args) {
		launchArgs = args;
	}
	
	@Override
	public void init() throws Exception {
		springContext = startSpringApp(springApp, launchArgs);
	}
	
	@Override
	public void start(Stage pStage) throws Exception {
		Assert.notNull(launchView, "The main view does not set");
		
		BaseView view = ViewHolder.get().getBaseView(launchView);

		Scene scene = view.getScene();
		scene(scene);
		
		pStage.setScene(scene);
		stage(pStage);
		pStage.show();

		// close spring web if fx closed
		pStage.setOnCloseRequest((e) -> {
			springContext.close();
		});

		afterShow();
	}

	/** 启动 spring 程序 */
	protected ConfigurableApplicationContext startSpringApp(Class<?> clazz, String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(clazz);
		return builder.headless(false).web(WebApplicationType.SERVLET).run(args);
	}
	
	/** custom style for scene <br>
	 * 自定义场景的设置和样式
	 * @param scene
	 */
	protected abstract void scene(Scene scene);

	/** custom style for primary stage
	 * 自定义舞台的设置和样式
	 * @param primaryStage
	 */
	protected abstract void stage(Stage primaryStage);

	/** do something after stage.show() */
	protected void afterShow() {
	}

	public static ConfigurableApplicationContext getSpringContext() {
		return springContext;
	}

}
