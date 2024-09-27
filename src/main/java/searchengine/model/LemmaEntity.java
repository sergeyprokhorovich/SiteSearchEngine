package searchengine.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "lemma")
@NoArgsConstructor
public class LemmaEntity {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity siteEntity;

    @Column(name = "lemma")
    @NotNull
    private String lemma;

    @Column(name = "frequency")
    @NotNull
    private Integer frequency;

}
