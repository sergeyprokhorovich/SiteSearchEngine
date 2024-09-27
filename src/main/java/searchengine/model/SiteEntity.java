package searchengine.model;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "site")
@NoArgsConstructor
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column (name = "status")
    @NotNull
    private Status status;

    @Column (name = "status_time")
    @NotNull
    private Instant date;

    @Column (name = "last_error")
    private String lastError;

    @NotNull
    @Column (name = "url")
    private String url;

    @NotNull
    @Column (name = "name")
    private String name;
}
