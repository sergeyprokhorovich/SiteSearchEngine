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

public interface LemmaRepository extends JpaRepository<LemmaEntity,Long>  {
    @Modifying
    @Transactional
    @Query("DELETE FROM LemmaEntity")
    void clearEntityTable();
    @Query("SELECT l FROM LemmaEntity l JOIN l.siteEntity s WHERE l.lemma = :lemma AND s = :siteEntity")
    LemmaEntity findLemmaByLemmaAndSiteEntity(@Param("lemma") String lemma, @Param("siteEntity") SiteEntity siteEntity);
    @Query("SELECT COUNT(l) FROM LemmaEntity l WHERE l.siteEntity = :siteEntity")
    long countLemmaBySite(@Param("siteEntity") SiteEntity siteEntity);
    @Query("SELECT l FROM LemmaEntity l JOIN l.siteEntity s WHERE l.lemma = :lemma AND s.url = :siteUrl")
    LemmaEntity findLemmaByLemmaAndSiteUrl(@Param("lemma") String lemma, @Param("siteUrl") String siteUrl);

}

