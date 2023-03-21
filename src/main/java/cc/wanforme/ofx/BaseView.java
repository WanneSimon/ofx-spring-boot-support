package cc.wanforme.ofx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

/*
 * container for fxml
 */
public abstract class BaseView {
	private static final FXMLLoader DEFAULT_LOADER = new FXMLLoader();
	
	private FXMLLoader loader;
	private Pane pane;

	// spring bean name
	private String beanName;
	// load fxml when bean created
	private boolean loadEager = false;
	// view holder;
	//private ViewHolder holder;
	
	public BaseView() {
		FXMLLoader cust = customFXMLLoader();
		loader = cust == null ? DEFAULT_LOADER : cust;
		
		loader.setControllerFactory(clazz -> {
			return ViewHolder.get().getBean(clazz);
		});
		
		if(loadEager) {
			this.loadFxml();
		}
	}
	
	
	/** custom fxml loader
	 * 自定义 fxml 的加载器
	 * @return
	 */
	public FXMLLoader customFXMLLoader() {
		return DEFAULT_LOADER;
	}
	
	/** load fxml when bean created <br>
	 * 在 bean 创建的时候立即加载 fxml 文件
	 * @param loadEager default false
	 */
	public void setLoadEager(boolean loadEager) {
		this.loadEager = loadEager;
	}
	
	/**
	 * fxml loaded 
	 */
	public void loaded() {};
	
	/**
	 * load fxml file
	 * */
	protected synchronized void loadFxml() {
		if(this.pane != null) {
			return;
		}
		
		Class<? extends BaseView> clazz = getClass();
		FXMLView anno = clazz.getAnnotation(FXMLView.class);
		if(anno == null) {
			throw new RuntimeException("fxml file isn't specific. " + clazz.getCanonicalName());
		}
		
		String path = anno.value();
		try {
			InputStream is = this.loadResource(path);
			Object load = loader.load(is);
			this.pane = (Pane) load;
		} catch (IOException e) {
			throw new RuntimeException("fxml file load failed. " + clazz.getCanonicalName(), e);
		}
		
		this.loaded();
	}
	
	public FXMLLoader getLoader() {
		return loader;
	}
	public Pane getPane() {
		if(pane == null) {
			this.loadFxml();
		}
		return pane;
	}
//	public void setPane(Pane pane) {
//		this.pane = pane;
//	}
	
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
	private InputStream loadResource(String path) throws IOException {
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
		
//		ClassPathResource resource = new ClassPathResource(path);
//		return resource.getInputStream();
	}
	
}