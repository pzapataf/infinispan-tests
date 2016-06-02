package demo;

import junit.framework.Assert;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by pzapataf on 2/8/16.
 */
public class DemoRemote {
    public static final String GRID_SERVER = "172.17.0.13";
    public static final String CACHE_NAME = "DEMOAPP_GRIDMAP";
    public static final int HEIGHT = 10;
    public static final int WIDTH  = 10;

    @Test
       public void initData() throws IOException {

        RemoteCacheManager cacheManager = getRemoteCacheManager();
        RemoteCache<Coords, Integer > cache = cacheManager.getCache(CACHE_NAME);

        Assert.assertNotNull(cache);

        Map<Coords, Integer> newImage = new HashMap<>();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                newImage.put(
                        new Coords(x, y),
                        0
                );
            }
        }

        Random r = new Random();

        // Add some random noise
        for( int i = 0; i < 100; i++) {
            newImage.put(
                new Coords(r.nextInt(WIDTH),r.nextInt(HEIGHT)),
                r.nextInt(255)
            );
        }

        cache.putAll(newImage);


        cacheManager.stop();
    }

    public void generateImage(Map<Coords, Integer> intensityMap) {
        BufferedImage off_Image =
                new BufferedImage(WIDTH, HEIGHT,
                        BufferedImage.TYPE_INT_RGB);

        for (Coords coords : intensityMap.keySet()) {
            int intensity = intensityMap.get(coords) == null ? 0 : intensityMap.get(coords);
            off_Image.setRGB(
                    coords.getX(),
                    coords.getY(),
                    (intensity << 16) | (intensity << 8) | intensity
            );
        }

        try {
            File outputfile = new File("/tmp/saved.png");
            ImageIO.write(off_Image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readData() throws IOException {

        RemoteCacheManager cacheManager = getRemoteCacheManager();
        RemoteCache<Coords, Integer> cache = cacheManager.getCache(CACHE_NAME);

        Assert.assertNotNull(cache);

        Set<Coords> keys = getKeysToRetrieve();

        Map<Coords, Integer> data = cache.getAll(keys);

        printData(data);

        generateImage(data);

                cacheManager.stop();
    }

    private void printData(Map<Coords, Integer> data) {
        System.out.println("-------------------------------------------------------------------------");
        System.out.print("       ");
        for (int x = 0; x < WIDTH; x++) {
            System.out.printf( "%02d ", x);
        }
        System.out.println("");

        for (int y = 0; y < HEIGHT; y++) {
            System.out.printf("%02d ", y);
            for (int x = 0; x < WIDTH; x++) {
                Integer intensity = data.get(
                        new Coords(x,y)
                );
                System.out.printf( "%02x ",
                        intensity
                );
            }
            System.out.println("\n");
        }
        System.out.println("-------------------------------------------------------------------------");
    }

    private Set<Coords> getKeysToRetrieve() {
        Set<Coords> keys = new HashSet<>();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                keys.add(new Coords(x, y));
            }
        }
        return keys;
    }

    @Test
    public void processData() throws IOException {

        RemoteCacheManager cacheManager = getRemoteCacheManager();
        RemoteCache<Coords, Integer> cache = cacheManager.getCache(CACHE_NAME);

        Assert.assertNotNull(cache);

        Set<Coords> keys = getKeysToRetrieve();

        Map<Coords, Integer> existingData= cache.getAll(keys);
        Map<Coords, Integer> newData= new HashMap<>();

        printData(existingData);

        // Move up
        for (Coords c : existingData.keySet()) {
            newData.put(
                    new Coords(c.getY()> 0 ? (c.getY()-1) : 0,c.getX()),
                    existingData.get(c));
        }

        printData(existingData);

        // Average data
        for (int y = 1; y < HEIGHT-2; y++) {
            for (int x = 1; x < WIDTH-2; x++) {
                Integer intensity =
                        (get(newData, x-1,y+1) +
                                get(newData, x,y+1) +
                                get(newData, x+1,y+1) +
                                get(newData, x - 1, y) +
                                get(newData, x, y) +
                                get(newData, x + 1, y) +
                                get(newData, x - 1, y - 1) +
                                get(newData, x, y - 1) +
                                get(newData, x+1,y-1)) / 9;

                //cache.putAsync(new Coords(x, y), intensity);
                newData.put(new Coords(x,y), intensity);
            }
        }

        // Update cache
        cache.putAll(newData);

        generateImage(newData);



                cacheManager.stop();
    }

    private int get(Map<Coords, Integer> data, int x, int y) {

        Integer value = data.get(new Coords(x,y));
        System.out.println("x=" + x + ", y=" + y + ", I=" + value);
        return value == null ? 0 : value.intValue();
    }

    private RemoteCacheManager getRemoteCacheManager() throws IOException {

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host(GRID_SERVER)
                .port(11222);

        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.build());

        //SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(remoteCacheManager);

        //serCtx.registerProtoFiles(FileDescriptorSource.fromResources("/something.proto"));
     //   serCtx.registerMarshaller(new SomethingMarshaller());

        return remoteCacheManager;
    }
}
