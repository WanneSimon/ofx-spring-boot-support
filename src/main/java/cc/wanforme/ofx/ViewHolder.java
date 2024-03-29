package cc.wanforme.ofx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * all fxml file associate classes
 */
@Component
public class ViewHolder implements ApplicationContextAware{
	// I do not find a proper way to inject, so make it static 
	private static volatile ViewHolder instance = null;
	private ApplicationContext context;
	
	public ViewHolder() {
		boolean re = false;
		synchronized (ViewHolder.class) {
			if(instance == null) {
				instance = this;
				re = true;
			}
		}
		
		if(!re) {
			throw new RuntimeException("View Holder has been created!");
		}
	}
	
	/** fxml and associate classes <br>
	 * A *.fxml file can bind multi class in design in the beginning, but now it doesn't make any sense.<br>
	 * 在一开始的设计中，一个 fxml 文件可以绑定多个 class ，但后来发现没有意义。
	 * */
	private Map<String, List<BaseView>> baseViewCache = new ConcurrentHashMap<>();
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public ApplicationContext getContext() {
		return context;
	}

	/** get by Class <br>
	 * 根据具体的子类获取
	 * @param clazz
	 * @return
	 */
	public <T extends BaseView> T getBaseView(Class<T> clazz) {
		return context.getBean(clazz);
	}
	public <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}
	
	/** get by name <br>
	 * 直接从 spring 容器中获取
	 * @param clazz
	 * @return
	 */
	public <T extends BaseView> T getBaseView(String name, Class<T> clazz) {
		return context.getBean(name, clazz);
	}
	
	
	/** get by Class and name when clazz is parent class <br>
	 * 当 clazz 是父类的时候，配合 name 获取
	 * @param clazz
	 * @return
	 */
	public <T extends BaseView> T getBaseView(Class<T> clazz, String name) {
		try {
			return context.getBean(clazz);
		} catch (NoUniqueBeanDefinitionException e) {
			Map<String, T> beans = context.getBeansOfType(clazz);
			Set<String> keys = beans.keySet();
			Optional<String> opt = keys.parallelStream()
					.filter(el -> Objects.equals(name, el))
					.findAny();
			
			if(opt.isPresent()) {
				return beans.get(opt.get());
			}
			
			throw new NoSuchBeanDefinitionException(name);
		}
		
	}
	
	/** refresh all BaseViews' (fxml bean) cache <br>
	 * 刷新缓存
	 * */
	public void refreshBaseViewCache() {
		synchronized (baseViewCache) {
			Map<String, BaseView> map = context.getBeansOfType(BaseView.class);
			Iterator<Entry<String, BaseView>> iterator = map.entrySet().iterator();
			
//			Map<String, BaseView> newCache = beans.stream().collect(Collectors.toMap(e -> {
//				FXMLView anno = e.getClass().getAnnotation(FXMLView.class);
//				String path = anno.value();
//				return path;
//			}, e -> e));
//			baseViewCache.clear();
//			baseViewCache = newCache;
			
			baseViewCache.clear();
			while (iterator.hasNext()) {
				Entry<String,BaseView> entry = iterator.next();
				String name = entry.getKey();
				BaseView view = entry.getValue();
				
				view.setBeanName(name);
				
				Class<? extends BaseView> clazz = view.getClass();
				FXMLView anno = clazz.getAnnotation(FXMLView.class);
				Optional<String> pathOpt = Optional.ofNullable(anno)
						.map(FXMLView::path);
				if(!pathOpt.isPresent()) {
					throw new RuntimeException("fxml file isn't specific. " + clazz.getCanonicalName());
				}
				
				String path = pathOpt.get();
				
				List<BaseView> views = baseViewCache.computeIfAbsent(path, 
						e -> new ArrayList<>(1));
				views.add(view);
			}
			
		}
	}
	
	/**
	 * get the first associate-class <br>
	 * 获取 fxml 绑定的第一个 class 实例（不定）
	 * <pre>
	 * BaseView view = ViewHolder.getBaseViewByFxml("/fxml/hello.fxml", false);
	 * </pre>
	 * @param fxml
	 * @param refreshCache refresh all BaseViews' cache
	 * @return
	 */
	public BaseView getBaseViewByFxml(String fxml, boolean refreshCache) {
		if(refreshCache) {
			this.refreshBaseViewCache();
		}
		List<BaseView> list = baseViewCache.get(fxml);
		if(list == null || list.isEmpty()) {
			throw new NoSuchBeanDefinitionException(BaseView.class, "fxml: " + fxml);
		}
		return list.get(0);
	}
	
	/**
	 * get the associate-class by fxml and bean name <br>
	 * 根据 fxml 和 spring 容器中名字获取
	 * <pre>
	 * BaseView view = ViewHolder.getBaseViewByFxml("/fxml/hello.fxml", "Hello", false);
	 * </pre>
	 * @param fxml
	 * @param name bean name
	 * @param refreshCache refresh all BaseViews' cache
	 * @deprecated use {@link #getBaseView(String, Class)} instead
	 * @return
	 */
	@Deprecated
	public BaseView getBaseViewByFxml(String fxml, String name, boolean refreshCache) {
		if(refreshCache) {
			this.refreshBaseViewCache();
		}
		
		List<BaseView> list = baseViewCache.get(fxml);
		if(list == null || list.isEmpty()) {
			throw new NoSuchBeanDefinitionException(BaseView.class, "fxml: " + fxml);
		}
		
		for (BaseView v : list) {
			if(Objects.equals(name, v.getBeanName())) {
				return v;
			}
		}
		
		throw new NoSuchBeanDefinitionException(BaseView.class, "name: " +name+ ",fxml: " + fxml);
	} 
	
	public static ViewHolder get() {
		return instance;
	}
}
