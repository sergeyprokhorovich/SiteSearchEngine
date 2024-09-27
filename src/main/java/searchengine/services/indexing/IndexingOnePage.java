package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.config.UserProperties;
import searchengine.dto.indexing.Page;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class IndexingOnePage {

      private final PageRepository pageRepository;
      private final SiteRepository siteRepository;
      private final UserProperties userAgent;
      private final SitesList sitesList;
      private final IndexingOfManyPages pageIndexing;
      private String domain;
      private String shortPath;
      private String path;
      private SiteEntity siteEntity;
      public boolean start(String urlPage)  {
         path = URLDecoder.decode(urlPage, StandardCharsets.UTF_8);
         setDomain(path);
         if (!isPageFromSites()) {return false;}
         setSiteEntity();
         setPath(path);
         ExecutorService executor = Executors.newSingleThreadExecutor();
         executor.execute(this::parse);
         return true;
      }

      private void setDomain(String url){
          StringBuilder sb = new StringBuilder();
          String[] strings = url.split("/");
          if(strings.length > 2) {
              for (int i = 0; i < 3; i++) {
                  sb.append("/");
                  sb.append(strings[i]);
              }
              sb.delete(0,1);
              this.domain = sb.toString();
          }else {
              this.domain = url;
          }
      }

      private void setPath(String url){
          StringBuilder sb = new StringBuilder();
          String[] strings = url.split("/");
          if(strings.length > 3) {
              for (int i = 3; i < strings.length; i++) {
                  sb.append("/");
                  sb.append(strings[i]);
              }
              this.shortPath = sb.toString();
          }else {
              this.shortPath = "/";
          }
      }

      private boolean isPageFromSites(){
          List<Site> sites = sitesList.getSites();
          for (Site site : sites) {
              String url = site.getUrl().endsWith("/") ? site.getUrl().replaceAll("/$", "") : site.getUrl();
              if (url.equals(domain)){
                  return true;
              }
          }
          return false;
      }

      private void parse(){
          Connection connection = Jsoup.connect(path).userAgent(userAgent.getNameUserAgent());
          connection.ignoreContentType(true);
          connection.timeout(10000);
          Document doc;
          try {
              doc = connection.get();
              String contentType = connection.response().header("Content-Type");
              if (contentType != null && contentType.startsWith("text/html")) {
                  Page page = new Page();
                  page.setContent(doc.html());
                  page.setCode(connection.response().statusCode());
                  page.setUrl(shortPath);
                  PageEntity pageEntity =  addPageRepository(page);
                  pageIndexing.indexingPage(pageEntity);
                  siteRepository.save(siteEntity);
              }
          } catch (IOException e) {
              siteEntity.setStatus(Status.FAILED);
              siteEntity.setLastError(e.getMessage());
              siteRepository.save(siteEntity);
          }
      }

    private PageEntity addPageRepository(Page page) {
        Optional<PageEntity> pageOptional = Optional.ofNullable(pageRepository.findPageByPathAndSiteEntity(page.getUrl(), siteEntity));
        PageEntity pageEntity;
        pageEntity = pageOptional.orElseGet(PageEntity::new);
        pageEntity.setPath(shortPath);
        pageEntity.setSiteEntity(siteEntity);
        pageEntity.setCode(page.getCode());
        pageEntity.setContent(page.getContent());
        return pageRepository.save(pageEntity);
    }
    private void setSiteEntity() {
        Optional<SiteEntity> siteEntityOptional = Optional.ofNullable(siteRepository.findByUrl(domain));
        if (siteEntityOptional.isPresent()) {
            siteEntity = siteEntityOptional.get();
        } else {
            SiteEntity siteEntityNew = new SiteEntity();
            siteEntityNew.setUrl(domain);
            List<Site> sites = sitesList.getSites();
            for (Site site : sites) {
                String url = site.getUrl().endsWith("/") ? site.getUrl().replaceAll("/$", "") : site.getUrl();
                if (url.equals(domain)) {
                    siteEntityNew.setName(site.getName());
                }
            }
            siteEntity = siteEntityNew;
        }
        siteEntity.setDate(Instant.now());
        siteEntity = siteRepository.save(siteEntity);
    }
}
