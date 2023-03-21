package cc.wanforme.ofx;

import org.springframework.boot.SpringApplication;
import org.springframework.util.Assert;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class BaseOFXApplication extends Application {
	
	private static String[] launchArgs = {};
	private static Class<? extends BaseView> launchView;
	private static Class<? extends BaseOFXApplication> launchApp;
	
	/** launch application ( SpringBoot and FXApplication )
	 * @param app 
	 * @param mainView 
	 * @param args 
	 */
	public static void launchOFX(Class<? extends BaseOFXApplication> app, 
			Class<? extends BaseView> mainView, String[] args) {
		launchApp = app;
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
		startSpringApp(launchApp, launchArgs);
	}
	
	@Override
	public void start(Stage pStage) throws Exception {
		Assert.notNull(launchView, "The main view does not set");
		
		BaseView view = ViewHolder.get().getBaseView(launchView);
		
		Pane root = view.getPane();
		Scene scene = new Scene(root);
		scene(scene);
		
		pStage.setScene(scene);
		stage(pStage);
		pStage.show();
	}
	
	private static void startSpringApp(Class<?> clazz, String[] args) {
		SpringApplication.run(clazz, args);
	}
	
	/** custom style for scene <br>
	 * 自定义场景的设置和样式
	 * @param scene
	 */
	protected abstract void scene(Scene scene);

	/** custom style for primary stage
	 * 自定义舞台的设置和样式
	 * @param scene
	 */
	protected abstract void stage(Stage primaryStage);
	
}
