import org.apache.log4j.BasicConfigurator;
import org.infinispan.Cache;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class InfinispanTest {

    protected Cache<Long, Book> getCache() {
        return InfinispanCache.getInstance().getCache("test1");
    }

    protected void printResults(List result) {
        System.out.println("**************************************************************");

        for (Object thing : result) {
            System.out.println(thing + "\n");
        }

        System.out.println("\nTOTAL: " + result.size());
        System.out.println("**************************************************************");
    }

    @Test
    public void testSimpleCache() throws Exception {

        InfinispanCache.getInstance().init();

        BasicConfigurator.configure();

        getCache().clear();

        for (int i = 0; i < 1000; i++) {
            Book book = new Book();
            book.setId(i);
            book.setTitle("Title " + i);
            book.setDescription("Description " + i);
            book.setRating(i % 5);
            getCache().put(book.getId(), book);
        }

        Assert.assertEquals(getCache().size(), 1000);

        /////////////////////////////////////////////////////////////////////////////////
        ///
        /// Now do some queries


        QueryFactory qf = Search.getQueryFactory(getCache());
        org.infinispan.query.dsl.Query query;

        /////////////////////////////////////////////////////////////////////////////////
        query = qf.from(Book.class).build();
        printResults(query.list());
        Assert.assertEquals(query.getResultSize(), 1000);


        /////////////////////////////////////////////////////////////////////////////////
        query = qf.from(Book.class)
                .having("id").gte(900).
                        toBuilder().build();

     //   printResults(query.list());
        Assert.assertEquals(100, query.getResultSize());


        /////////////////////////////////////////////////////////////////////////////////
        query = qf.from(Book.class)
                .having("id").between(5, 100).
                        toBuilder().build();

     //   printResults(query.list());
        Assert.assertEquals(96, query.getResultSize());

        /////////////////////////////////////////////////////////////////////////////////
        query = qf.from(Book.class)
                .having("description").eq("Description 1").
                        toBuilder().build();

//        printResults(query.list());
        Assert.assertEquals(1, query.getResultSize());

        /////////////////////////////////////////////////////////////////////////////////
        query = qf.from(Book.class)
                .having("description").like("%00%").toBuilder().build();

        printResults(query.list());
        Assert.assertEquals(9, query.getResultSize());


        InfinispanCache.getInstance().shutdown();
    }
}
