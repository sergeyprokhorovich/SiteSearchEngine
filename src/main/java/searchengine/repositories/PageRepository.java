package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface PageRepository extends JpaRepository<PageEntity,Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM PageEntity")
    void clearEntityTable();
    @Query("SELECT p FROM PageEntity p JOIN p.siteEntity s WHERE p.path = :path AND s = :siteEntity")
    PageEntity findPageByPathAndSiteEntity(@Param("path") String lemma, @Param("siteEntity") SiteEntity siteEntity);
    @Query("SELECT COUNT(p) FROM PageEntity p WHERE p.siteEntity = :siteEntity")
    long countPagesBySite(@Param("siteEntity") SiteEntity siteEntity);
}
