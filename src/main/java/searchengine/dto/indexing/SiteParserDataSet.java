package searchengine.dto.indexing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.config.Site;
import searchengine.config.UserProperties;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class SiteParserDataSet {
    @Getter
    private final SiteRepository siteRepository;
    @Getter
    private final PageRepository pageRepository;
    @Getter
    private final IndexRepository indexRepository;
    @Getter
    private final LemmaRepository lemmaRepository;
    @Getter
    private String domain;
    @Getter
    @Setter
    private UserProperties userProperties;
    private final Set<String> linksInWork = Collections.synchronizedSet(new HashSet<>());
    public void setDomain(Site site) {
        this.domain = site.getUrl().replaceAll("/+$", "");
        linksInWork.add(domain);

    }
    public void addLink(String url){
        linksInWork.add(url);
    }
    public int getSize(){
        return linksInWork.size();
    }
    public boolean containLinksInWork(String url){return linksInWork.contains(url);}


}
