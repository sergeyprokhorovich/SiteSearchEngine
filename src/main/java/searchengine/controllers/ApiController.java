package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.UrlPage;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteRequest;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public StatisticsResponse statistics() {
        return statisticsService.getStatistics();
    }
    @GetMapping("/startIndexing")
    public IndexingResponse startIndexing() {
        return indexingService.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public IndexingResponse stopIndexing() {
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public IndexingResponse startIndexingOnePage(@ModelAttribute UrlPage urlPage){
        return indexingService.startIndexingOnePage(urlPage.getUrl());
    }

    @GetMapping("/search")
    public SearchResponse searchByQuery (@ModelAttribute SiteRequest siteRequest){
        return searchService.findPagesByQuery(siteRequest);
    }
}
