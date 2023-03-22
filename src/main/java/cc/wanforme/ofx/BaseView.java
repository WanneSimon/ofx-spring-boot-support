package cc.wanforme.ofx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/*
 * container for fxml
 */
public abstract class BaseView {
	
	private FXMLLoader loader;
	private Pane pane; // the root of scene
 	private Scene scene;

	// spring bean name (auto set by spring)
	private String beanName;
	// load fxml when bean created
	private boolean loadEager = false;
	// view holder;
	//private ViewHolder holder;
	
	public BaseView() {
		FXMLLoader cust = customFXMLLoader();
		loader = cust == null ? defaultFXMLLoader() : cust;
		
		loader.setControllerFactory(clazz -> {
			return ViewHolder.get().getBean(clazz);
		});
		
		loadEager = this.isLoadEager();
		if(loadEager) {
			this.loadFxml();
		}
	}
	
	
	/** custom fxml loader
	 * 自定义 fxml 的加载器
	 * @return
	 */
	public FXMLLoader customFXMLLoader() {
		return defaultFXMLLoader();
	}
	
	/** load fxml when bean created <br>
	 * 在 bean 创建的时候立即加载 fxml 文件
	 * @return default false
	 */
	public boolean isLoadEager() {
		return false;
	}
	
	/**
	 * fxml loaded 
	 */
	public void loaded() {};
	
	/**
	 * load fxml file
	 * */
	protected synchronized void loadFxml() {
		if(this.pane != null && this.scene != null) {
			return;
		}
		
		if(this.pane == null) {
			Class<? extends BaseView> clazz = getClass();
			FXMLView anno = clazz.getAnnotation(FXMLView.class);
			Optional<String> pathOpt = Optional.ofNullable(anno)
					.map(FXMLView::path);
			if(!pathOpt.isPresent()) {
				throw new RuntimeException("fxml file isn't specific. " + clazz.getCanonicalName());
			}
			
			String path = pathOpt.get();
			try {
				InputStream is = this.loadFXMLResource(path);
				Object load = loader.load(is);
				this.pane = (Pane) load;
			} catch (IOException e) {
				throw new RuntimeException("fxml file load failed. " + clazz.getCanonicalName(), e);
			}
			scene = new Scene(this.pane);
			this.loaded();
		} else { // impossible
			scene = new Scene(this.pane);
		}		
	}
	
	public FXMLLoader getLoader() {
		return loader;
	}
	public Pane getPane() {
		return getPane(false);
	}
	
	/** get Pane , regenerate a pane if 'recreate' is true
	 * @param recreate
	 * @return
	 */
	public Pane getPane(boolean recreate) {
		if(recreate || pane == null) {
			// TODO destroy old
			//if(recreate && pane!=null) {
			//}
			this.loadFxml();
		}
		return pane;
	}
	
//	private AtomicBoolean sceneLock = new AtomicBoolean(false) ;
	public Scene getScene() {
//		if(scene == null) {
//			if(!sceneLock.compareAndSet(false, true)) {
//				if(scene == null) {
//					Pane p = getPane();
//					scene = new Scene(p);
//				}
//				sceneLock.set(false);
//			}
//		}
		if(scene == null) {
			this.loadFxml();
		}
		return scene;
	}
	
	/** spring bean name (auto set by spring) */
	public String getBeanName() {
		return beanName;
	}
	void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	/**
	 * if you want to change how to load fxml file, override this method.
	 * @param relativePath
	 * @return
	 * @throws IOException 
	 */
	protected InputStream loadFXMLResource(String path) throws IOException {
		return loadResource(path);
	}
	
	/** load resource. order: <br>
	 * 1. {project}/{path} <br>
	 * 2. {project}/src/main/resources/{path} <br>
	 * 1. {jar-classpath}/{path} <br>
	 * @param relativePath
	 * @return
	 * @throws IOException 
	 */
	public static InputStream loadResource(String path) throws IOException {
		File location = new ApplicationHome().getDir();
		File file = new File(location.getAbsolutePath() + "/" + path);
		
		if( file.exists() && file.isFile()) {
			return new FileInputStream(file);
		} else {
			file = new File(location.getAbsolutePath() + "/src/main/resources/" + path);
			if( file.exists() && file.isFile()) {
				return new FileInputStream(file);
			}
			
			// 读取jar包内部的资源
			ClassPathResource resource = new ClassPathResource(path);
			return resource.getInputStream();
		}
	}
	
	private FXMLLoader defaultFXMLLoader() {
		// loader can't  be reused, so create one
		return new FXMLLoader();
	}
}
