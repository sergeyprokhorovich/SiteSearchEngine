package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.SiteEntity;
import javax.transaction.Transactional;
import java.util.List;

public interface SiteRepository extends JpaRepository <SiteEntity,Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM SiteEntity")
    void clearEntityTable();
    SiteEntity findByUrl(String url);
    List<SiteEntity> findAll();
}
