import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import java.io.Serializable;

@Indexed
public class Book implements Serializable {
    @Field(analyze=Analyze.NO)
    private long id;

    @Field
    private String title;

    @Field
    private String description;

    @Field
    private int rating;

    public Book() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                "\n title='" + title + '\'' +
                "\n description='" + description + '\'' +
                "\n rating=" + rating +
                "\n}";
    }
}
