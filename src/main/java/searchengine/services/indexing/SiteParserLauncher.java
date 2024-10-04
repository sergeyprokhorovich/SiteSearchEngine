package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.UserProperties;
import searchengine.dto.indexing.SiteParserDataSet;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class SiteParserLauncher {
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final IndexingOfManyPages pageIndexing;
    private final UserProperties userAgent;
    public void start(Site site) throws IOException {
        setStatusIndexing(site);
        site.setStopIndexing(false);
        ForkJoinPool pool = new ForkJoinPool();
        SiteParserDataSet siteParseDataSet = new SiteParserDataSet(siteRepository,pageRepository,indexRepository,lemmaRepository);
        siteParseDataSet.setDomain(site);
        siteParseDataSet.setUserProperties(userAgent);
        String startingUrl = siteParseDataSet.getDomain();
        SiteParser siteParser = new SiteParser(startingUrl, siteParseDataSet, pageIndexing);
        pool.execute(siteParser);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            do {
                if (site.getStopIndexing()) {
                    pool.shutdown();
                    try {
                        if (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                            pool.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                       pool.shutdownNow();
                    }
                }
            } while (!siteParser.isDone());
            if(!site.getStopIndexing()){
                setStatusIndexed(siteParseDataSet.getDomain());
            }else{
                setStatusFailed(site);
            }
        });
    }
    public void clearRepositories(){
        indexRepository.clearEntityTable();
        lemmaRepository.clearEntityTable();
        pageRepository.clearEntityTable();
        siteRepository.clearEntityTable();
    }
    private void setStatusIndexed(String domain){
        SiteEntity siteEntity =  siteRepository.findByUrl(domain);
        siteEntity.setStatus(Status.INDEXED);
        siteEntity.setDate(Instant.now());
        siteRepository.save(siteEntity);
    }
    private void setStatusIndexing(Site site) {
        Optional<SiteEntity> siteEntityOptional = Optional.ofNullable(siteRepository.findByUrl(site.getUrl()));
        SiteEntity siteEntity;
        if (siteEntityOptional.isPresent()){
            siteEntity = siteEntityOptional.get();
            siteEntity.setStatus(Status.INDEXING);
        }else{
            siteEntity = new SiteEntity();
            siteEntity.setStatus(Status.INDEXING);
            siteEntity.setDate(Instant.now());
            siteEntity.setUrl(site.getUrl());
            siteEntity.setName(site.getName());
        }
        siteRepository.save(siteEntity);
    }
    private void setStatusFailed(Site site){
        Optional<SiteEntity> optionalSiteEntity = Optional.ofNullable(siteRepository.findByUrl(site.getUrl()));
        if (optionalSiteEntity.isPresent()){
            SiteEntity siteEntity = optionalSiteEntity.get();
            siteEntity.setDate(Instant.now());
            siteEntity.setStatus(Status.FAILED);
            siteEntity.setLastError("Индексация была прервана вручную");
            siteRepository.save(siteEntity);
        }
    }

    public void stop(Site site){
        site.setStopIndexing(true);
        setStatusFailed(site);
    }
}
