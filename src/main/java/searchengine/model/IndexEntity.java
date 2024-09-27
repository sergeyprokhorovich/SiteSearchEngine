package searchengine.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "lemma_index")
@NoArgsConstructor
public class IndexEntity {

@Id
@NotNull
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column (name = "id")
private Integer id;

@ManyToOne
@JoinColumn(name = "page_id" )
private PageEntity pageEntity;

@ManyToOne
@JoinColumn(name = "lemma_id" )
private LemmaEntity lemmaEntity;

@Column(name = "page_rank",nullable = false)
private Float rank;
}
