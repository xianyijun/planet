package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.registry.api.NotifyListener;
import cn.xianyijun.planet.registry.api.Registry;
import cn.xianyijun.planet.utils.CollectionUtils;
import cn.xianyijun.planet.utils.ConcurrentHashSet;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import cn.xianyijun.planet.utils.StringUtils;
import cn.xianyijun.planet.utils.UrlUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The type Abstract registry.
 */
@Slf4j
@Data
public abstract class AbstractRegistry implements Registry {

    private static final char URL_SEPARATOR = ' ';

    private static final String URL_SPLIT = "\\s+";

    private final Properties properties = new Properties();

    private final ExecutorService registryCacheExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("RpcSaveRegistryCache", true));

    private final boolean syncSaveFile;

    private final AtomicLong lastCacheChanged = new AtomicLong();

    private final Set<URL> registered = new ConcurrentHashSet<>();

    private final ConcurrentMap<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();

    private final ConcurrentMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<>();

    private URL registryUrl;

    private File file;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * Instantiates a new Abstract registry.
     *
     * @param url the url
     */
    public AbstractRegistry(URL url) {
        setUrl(url);
        // 启动文件保存定时器
        syncSaveFile = url.getParameter(Constants.REGISTRY_FILESAVE_SYNC_KEY, false);
        String filename = url.getParameter(Constants.FILE_KEY, System.getProperty("user.home") + "/.rpc/rpc-registry-" + url.getParameter(Constants.APPLICATION_KEY) + "-" + url.getAddress() + ".cache");
        File file = null;
        if (ConfigUtils.isNotEmpty(filename)) {
            file = new File(filename);
            if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid registry store file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                }
            }
        }
        this.file = file;
        loadProperties();
        notify(url.getBackupUrls());
    }

    /**
     * Notify.
     *
     * @param urlList the url list
     */
    protected void notify(List<URL> urlList) {
        if (urlList == null || urlList.isEmpty()){
            return;
        }
        getSubscribed().entrySet().stream().filter(Objects::nonNull).forEach(entry ->{
            URL url = entry.getKey();
            if (!UrlUtils.isMatch(url, urlList.get(0))) {
                return;
            }
            Set<NotifyListener> notifyListeners = entry.getValue();
            if (notifyListeners != null && !notifyListeners.isEmpty()){
                notifyListeners.stream().filter(Objects::nonNull).forEach(notifyListener -> notify(url, notifyListener,filterEmpty(url, urlList)));
            }
        });
    }

    /**
     * Notify.
     *
     * @param url      the url
     * @param listener the listener
     * @param urls     the urls
     */
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null){
            throw  new  IllegalArgumentException("notify url can not be null");
        }
        if (listener == null){
            throw  new  IllegalArgumentException("listener  can not be null");
        }
        if (CollectionUtils.isEmpty(urls) && !Constants.ANY_VALUE.equals(url.getServiceInterface())){
            return;
        }

        Map<String, List<URL>> result = new HashMap<>();

        urls.stream().filter(Objects::nonNull).filter(p -> UrlUtils.isMatch(url, p)).forEach(p ->{
            String category = p.getParameter(Constants.CATEGORY_KEY,Constants.DEFAULT_CATEGORY);
            List<URL> categoryList = result.get(category);
            if (categoryList == null) {
                categoryList = new ArrayList<>();
                result.put(category, categoryList);
            }
            categoryList.add(p);
        });

        if (result.size() == 0) {
            return;
        }
        Map<String, List<URL>> categoryNotified = notified.get(url);
        if (categoryNotified == null) {
            notified.putIfAbsent(url, new ConcurrentHashMap<>());
            categoryNotified = notified.get(url);
        }

        for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<URL> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            saveProperties(url);
            listener.notify(categoryList);
        }
    }

    private void saveProperties(URL url) {
        if (file == null) {
            return;
        }

        try {
            StringBuilder buf = new StringBuilder();
            Map<String, List<URL>> categoryNotified = notified.get(url);
            if (categoryNotified != null) {
                for (List<URL> us : categoryNotified.values()) {
                    for (URL u : us) {
                        if (buf.length() > 0) {
                            buf.append(URL_SEPARATOR);
                        }
                        buf.append(u.toFullString());
                    }
                }
            }
            properties.setProperty(url.getServiceKey(), buf.toString());
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                registryCacheExecutor.execute(new SaveProperties(version));
            }
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
    }


    /**
     * Filter empty list.
     *
     * @param url  the url
     * @param urls the urls
     * @return the list
     */
    protected static List<URL> filterEmpty(URL url, List<URL> urls) {
        if (urls == null || urls.size() == 0) {
            List<URL> result = new ArrayList<>(1);
            result.add(url.setProtocol(Constants.EMPTY_PROTOCOL));
            return result;
        }
        return urls;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url can not be null");
        }
        this.registryUrl = url;
    }

    @Override
    public URL getUrl() {
        return registryUrl;
    }

    @Override
    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        Set<URL> destroyRegistered = new HashSet<>(getRegistered());

        if (!destroyRegistered.isEmpty()){
            destroyRegistered.stream().filter(Objects::nonNull).filter(url -> url.getParameter(Constants.DYNAMIC_KEY,true)).forEach(this::unRegister);
        }
        Map<URL, Set<NotifyListener>> destroySubscribed = new HashMap<>(getSubscribed());
        if (!destroyRegistered.isEmpty()){
            destroySubscribed.entrySet().stream().filter(Objects::nonNull).forEach(entry -> entry.getValue().stream().forEach(listener ->unSubscribe(entry.getKey(),listener)));
        }
    }

    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url can not be null");
        }
        registered.add(url);
    }

    @Override
    public void unRegister(URL url) {
        if (url == null){
            throw new IllegalArgumentException("unRegister url can not be null");
        }
        registered.remove(url);
    }

    @Override
    public List<URL> lookup(URL url) {
        List<URL> result = new ArrayList<>();
        Map<String, List<URL>> notifiedUrls = getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<URL> urls : notifiedUrls.values()) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        } else {
            final AtomicReference<List<URL>> reference = new AtomicReference<>();
            NotifyListener listener = urls -> reference.set(urls);
            subscribe(url, listener);
            List<URL> urls = reference.get();
            if (urls != null && urls.size() > 0) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }


    private void loadProperties() {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                properties.load(in);
                if (log.isInfoEnabled()) {
                    log.info("Load registry store file " + file + ", data: " + properties);
                }
            } catch (Throwable e) {
                log.warn("Failed to load registry store file " + file, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Do save properties.
     *
     * @param version the version
     */
    public void doSaveProperties(long version) {
        if (version < lastCacheChanged.get()) {
            return;
        }
        if (file == null) {
            return;
        }
        // 保存
        try {
            File lockFile = new File(file.getAbsolutePath() + ".lock");
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
            try {
                FileChannel channel = raf.getChannel();
                try {
                    FileLock lock = channel.tryLock();
                    if (lock == null) {
                        throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file, please config: rpc.registry.file=xxx.properties");
                    }
                    // 保存
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        try (FileOutputStream outputFile = new FileOutputStream(file)) {
                            properties.store(outputFile, "rpc Registry Cache");
                        }
                    } finally {
                        lock.release();
                    }
                } finally {
                    channel.close();
                }
            } finally {
                raf.close();
            }
        } catch (Throwable e) {
            if (version < lastCacheChanged.get()) {
                return;
            } else {
                registryCacheExecutor.execute(new SaveProperties(lastCacheChanged.incrementAndGet()));
            }
            log.warn("Failed to save registry store file, cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void unSubscribe(URL url, NotifyListener listener) {
        if (url == null){
            throw new IllegalArgumentException("unSubscribe url can not be null");
        }
        if (listener == null){
            throw new IllegalArgumentException("unSubscribe listener can not be null");
        }

        Set<NotifyListener> listeners = subscribed.get(url);

        if (listeners != null){
            listeners.remove(listener);
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url can not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener can not be null");
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, new ConcurrentHashSet<>());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    /**
     * Recover.
     *
     * @throws Exception the exception
     */
    protected void recover() throws Exception {
        // register
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            for (URL url : recoverRegistered) {
                register(url);
            }
        }
        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    /**
     * Gets cache urls.
     *
     * @param url the url
     * @return the cache urls
     */
    public List<URL> getCacheUrls(URL url) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)){
                continue;
            }
            if (key.equals(url.getServiceKey()) && (Character.isLetter(key.charAt(0)) || key.charAt(0) == '_')) {
                String[] arr = value.trim().split(URL_SPLIT);
                List<URL> urls = new ArrayList<>();
                for (String u : arr) {
                    urls.add(URL.valueOf(u));
                }
                return urls;
            }
        }
        return null;
    }


    // =====================SaveProperties ==================

    private class SaveProperties implements Runnable {
        private long version;

        private SaveProperties(long version) {
            this.version = version;
        }
        @Override
        public void run() {
            doSaveProperties(version);
        }
    }

}
