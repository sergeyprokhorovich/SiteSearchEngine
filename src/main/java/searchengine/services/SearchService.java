package searchengine.services;

import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteRequest;

public interface SearchService {
    SearchResponse findPagesByQuery(SiteRequest siteRequest);
}
