import junit.framework.Assert;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by pzapataf on 2/8/16.
 */
public class InfinispanRemote {
    public static final String CACHE_NAME = "testcache";
    public static final String SCRIPT_CACHE = "___script_cache";

    public static final int MAX_ITEMS = 5000000;

    @Test
    public void testWriteToRemote() throws IOException {

        RemoteCacheManager cacheManager = getRemoteCacheManager();

        RemoteCache<String, Object> cache = cacheManager.getCache("default");

        Assert.assertNotNull(cache);

        long start = System.currentTimeMillis();
        for( int i = 0; i < MAX_ITEMS; i++) {
            System.out.println("Adding " + i);
            cache.putIfAbsent("Item_" + start + "_" + i, "A test item");
        }

        cacheManager.stop();
    }

    public void registerScript(String name, String body) throws IOException {
        RemoteCacheManager cacheManager = getRemoteCacheManager();
        RemoteCache<String, Object> cache = cacheManager.getCache(SCRIPT_CACHE);
        Assert.assertNotNull(cache);
        cache.put(name, body);

    }

    public Object execute(String name, Map<String, Object> params) throws IOException {
        RemoteCacheManager cacheManager = getRemoteCacheManager();
        RemoteCache<String, Object> cache = cacheManager.getCache(SCRIPT_CACHE);

        if( params == null ) {
            params = new HashMap<>();
        }

        Object result = cache.execute(name, params);

        System.out.println("RESULT for " + name + " = \n" + result + "\n*********************") ;

        return result;
    }

    @Test
    public void testCreateScripts() throws IOException {

        registerScript("multiply_v3.js",
                "// mode=local,language=javascript\n" +
                        "var s = 'Parameter a is of type ' + typeof a + ' and value ' + a +'\\n';\n" +
                        "   s += 'Parameter b is of type ' + typeof b + ' and value ' + b +'\\n';\n" +
                        "s;"
        );

        // Now test execution

        // Create the parameters for script execution
        Map<String, Object> params = new HashMap<>();
        params.put("a", "10");
        params.put("b", "20");

        execute("multiply_v3.js", params);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        registerScript("helloworld.js",
                "// mode=local,language=javascript\n" +
                        "var a = \"HI THERE!\" + cache.getCacheManager().getAddress();\n" +
                        "a;"
        );


        execute("helloworld.js", null);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        registerScript("distributed-execution.js",
                "// mode=distributed,language=javascript\n" +
                "cache.getCacheManager().getAddress().toString()"
        );


        execute("distributed-execution.js", null);
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        registerScript("failed-miserably.js",
                "// mode=local,language=javascript\n" +
                        "throw new Error('Failed on purpose');"
        );


        execute("failed-miserably.js", null);

    }


    private RemoteCacheManager getRemoteCacheManager() throws IOException {
        Properties props = new Properties();
        props.put("jdg.host", "localhost");
        props.put("jdg.hotrod.port", "11222");

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host("localhost")
                .port(11222)
                .marshaller(new ProtoStreamMarshaller());




        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());

        SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);

        serCtx.registerProtoFiles(FileDescriptorSource.fromResources("/something.proto"));
        serCtx.registerMarshaller(new SomethingMarshaller());


        return remoteCacheManager;
    }

    @Test
    public void testRemoteQueries() throws IOException {
        RemoteCacheManager cacheManager = getRemoteCacheManager();

        RemoteCache<String, Object> cache = cacheManager.getCache("newCache3");

        for( int i = 0; i < 10; i++) {
            Something something = new Something("thing"+i, "valueofthing"+i);
            cache.put(something.getKey(), something);

            System.out.println(something);
        }

        QueryFactory qf = Search.getQueryFactory(cache);

        Query query = qf.from(Something.class)
                .having("value").eq("valueofthing5")
                .toBuilder().build();

        List list = query.list();
        System.out.println("RESULT SIZE: " + list.size());

        Assert.assertTrue(list.size() > 0);

    }
}
