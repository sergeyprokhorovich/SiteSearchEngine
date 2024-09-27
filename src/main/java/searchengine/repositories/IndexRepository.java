package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntity,Long>  {
    @Modifying
    @Transactional
    @Query("DELETE FROM IndexEntity")
    void clearEntityTable();
    @Query("SELECT i FROM IndexEntity i " +
            "JOIN i.pageEntity p " +
            "JOIN p.siteEntity s " +
            "JOIN i.lemmaEntity l " +
                 "WHERE s.url = :siteUrl " +
                 "AND l = :lemmaEntity")
     List<IndexEntity> findIndexBySiteUrlAndLemmaEntity(@Param("siteUrl") String siteUrl, @Param("lemmaEntity") LemmaEntity lemmaEntity);

}


