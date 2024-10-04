package searchengine.model;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Index;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "page")
@NoArgsConstructor
public class PageEntity {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id" )
    private SiteEntity siteEntity;

    @NotNull
    @Column(name = "path")
    private String path;

    @NotNull
    @Column (name = "code")
    private Integer code;

    @NotNull
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;
}
