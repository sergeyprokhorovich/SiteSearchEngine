package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private List<DetailedStatisticsItem> detailed;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setPages((int) pageRepository.count());
        total.setLemmas((int) lemmaRepository.count());
        detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            setDetailed(site);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private void setDetailed(Site site){
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl());
        if (siteEntity != null) {
            item.setPages((int) pageRepository.countPagesBySite(siteEntity));
            item.setLemmas((int) lemmaRepository.countLemmaBySite(siteEntity));
            if (siteEntity.getStatus() !=null ){
            item.setStatus(siteEntity.getStatus().toString());}
            if (siteEntity.getLastError() != null) {
                item.setError(siteEntity.getLastError());
            }
            item.setStatusTime(siteEntity.getDate().toEpochMilli());
        }
        detailed.add(item);
    }
}
