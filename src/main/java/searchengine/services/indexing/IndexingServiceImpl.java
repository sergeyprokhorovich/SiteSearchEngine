package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ErrorDetailsIndexingResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exception.IndexingFailException;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final SitesList sitesList;
    private final SiteParserLauncher siteParserLauncher;
    private final IndexingOnePage indexingOnePage;

    public IndexingResponse startIndexingOnePage(String url){
        if (!indexingOnePage.start(url)) {
            String errorMsg = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
            ErrorDetailsIndexingResponse error = new ErrorDetailsIndexingResponse(errorMsg, false);
            throw new IndexingFailException(error);
        }
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexingRun()) {
            ErrorDetailsIndexingResponse error = new ErrorDetailsIndexingResponse("Индексация уже запущена", false);
            throw new IndexingFailException(error);
        }
        siteParserLauncher.clearRepositories();
        List<Site> sites = sitesList.getSites();
        for (Site site : sites) {
            try {
                siteParserLauncher.start(site);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (isIndexingRun()) {
            List<Site> sites = sitesList.getSites();
            for (Site site : sites) {
                siteParserLauncher.stop(site);
            }
            IndexingResponse response = new IndexingResponse();
            response.setResult(true);
            return response;
        }
        ErrorDetailsIndexingResponse error = new ErrorDetailsIndexingResponse("Индексация не запущена", false);
        throw new IndexingFailException(error);
    }

    private boolean isIndexingRun() {
        List<Site> sites = sitesList.getSites();
        for (Site site : sites) {
            Optional<SiteEntity> optionalSiteEntity = Optional.ofNullable(siteRepository.findByUrl(site.getUrl()));
            if (optionalSiteEntity.isPresent()){
               return optionalSiteEntity.get().getStatus()==Status.INDEXING;
            }
        }
        return false;
    }


}
