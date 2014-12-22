
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

import java.io.InputStream;

public class InfinispanCache {

    private DefaultCacheManager cacheManager = null;

    private static InfinispanCache theInstance = new InfinispanCache();

    private InfinispanCache() {

    }

    public void init() throws Exception {
        if (cacheManager == null) {
            // First, search it as a resource
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("default-infinispan.xml");
            if( is == null ) {
                throw new Exception("Infinispan initial configuration is not either a classpath resource or a valid file");
            }

            this.cacheManager = new DefaultCacheManager(is);
        }
    }

    public void shutdown() {
        if (cacheManager != null) {
            cacheManager.stop();
            cacheManager = null;
        }
    }

    public <K, T> Cache<K, T> getCache(String cacheId) {
        return cacheManager.getCache(cacheId);
    }

    public static InfinispanCache getInstance() {
        return theInstance;
    }
}
