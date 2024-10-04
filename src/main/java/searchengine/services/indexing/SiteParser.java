package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.indexing.Page;
import searchengine.dto.indexing.SiteParserDataSet;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SiteParser extends RecursiveAction {
    private final int timetosleep = 3000;
    private final int timetoconnect = 10000;
    private final String parsUrl;
    private final SiteParserDataSet siteParseDataSet;
    private final IndexingOfManyPages indexingOfManyPages;
    @Override
    protected void compute()  {
        ForkJoinTask.invokeAll(createSubtasks());
    }
    private List<SiteParser> createSubtasks(){
        List<SiteParser> subtask = new ArrayList<>();
        try {
            List<String> listLinks = new ArrayList<>(parse());
            if(!listLinks.isEmpty()) {
                for (String url : listLinks) {
                    siteParseDataSet.addLink(url);
                    SiteParser task = new SiteParser(url, siteParseDataSet, indexingOfManyPages);
                    subtask.add(task);
                }
            }
        } catch (Exception ignored) {
        }
        return subtask;
    }
    private List<String> parse() throws IOException {
        List<String> listLink;
        try {
            TimeUnit.MILLISECONDS.sleep(timetosleep);
        } catch (InterruptedException ignored){}
        String userAgent = siteParseDataSet.getUserProperties().getNameUserAgent();
        Connection connection = Jsoup.connect(parsUrl).userAgent(userAgent);
        connection.ignoreContentType(true);
        connection.timeout(timetoconnect);
        Document doc;
            doc = connection.get();
            String contentType = connection.response().header("Content-Type");
            Elements links = doc.select("a");
            if (contentType != null && contentType.startsWith("text/html")) {
                Page page = new Page();
                page.setContent(doc.html());
                page.setCode(connection.response().statusCode());
                page.setUrl(parsUrl);
                PageEntity pageEntity =  addPageRepository(page);
                if (pageEntity != null) {
                    indexingOfManyPages.indexingPage(pageEntity);
                }
            }
            listLink = getLinks(links);
    return listLink;
    }
    private List<String> getLinks(Elements links) {
        List<String> listLink =  new ArrayList<>();
        if (links.isEmpty()) {
            return listLink;
        }
        for (Element link : links) {
            String url = link.attr("href");
            url = url.replaceAll("(?<=/{2})/+", "");
            url = url.endsWith("/") ? url.replaceAll("/$", "") : url;
            if (!linkMeetsTheCondition(url)){
                continue;
            }
            siteParseDataSet.addLink(url);
            if (linkIsValid(url) && !checkPageExist(url)) {
                listLink.add(url);
            }
            try {
            TimeUnit.MILLISECONDS.sleep(timetosleep);
            } catch (InterruptedException ignored){}
        }
        return listLink;
    }
    public boolean linkIsValid(String url){
        String msg = "При проверке URL  " + url + " ";
        try {
            TimeUnit.MILLISECONDS.sleep(timetosleep);
            String userAgent = siteParseDataSet.getUserProperties().getNameUserAgent();
            Connection connection = Jsoup.connect(url).userAgent(userAgent);
            connection.ignoreContentType(true);
            connection.get();
            String contentType = connection.response().header("Content-Type");
            if (contentType != null){
              return contentType.startsWith("text/html");
            }
        }catch (SocketException e) {
            System.out.println(msg+" Произошла ошибка сокета: " + e.getMessage());
        }catch (HttpStatusException e) {
            Page page = new Page();
            page.setContent("n/a");
            page.setCode(e.getStatusCode());
            page.setUrl(url);
            addPageRepository(page);
            System.out.println(msg+" Произошла ошибка HTTP: " + e.getStatusCode());
        } catch (IOException e) {
            System.out.println(msg+" Произошла ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(msg+" Произошла  ошибка  : " + e.getMessage());
        }
        siteParseDataSet.addLink(url);
        return false;
    }
    private PageEntity addPageRepository(Page page){
        SiteRepository siteRepository = siteParseDataSet.getSiteRepository();
        PageRepository pageRepository = siteParseDataSet.getPageRepository();
        SiteEntity siteEntity = siteRepository.findByUrl(siteParseDataSet.getDomain());
        Optional<PageEntity> pageOptional =  Optional.ofNullable(pageRepository.findPageByPathAndSiteEntity(page.getUrl(),siteEntity));
        if (pageOptional.isEmpty()){
            PageEntity pageEntity = new PageEntity();
            pageEntity.setPath(getShortUrl(page.getUrl()));
            pageEntity.setSiteEntity(siteEntity);
            pageEntity.setCode(page.getCode());
            pageEntity.setContent(page.getContent());
            return pageRepository.save(pageEntity);
        }
        return null;
    }
    private boolean checkPageExist(String url){
        PageRepository pageRepository = siteParseDataSet.getPageRepository();
        SiteRepository siteRepository = siteParseDataSet.getSiteRepository();
        SiteEntity siteEntity = siteRepository.findByUrl(siteParseDataSet.getDomain());
        Optional<PageEntity> pageOptional =  Optional.ofNullable(pageRepository.findPageByPathAndSiteEntity(url,siteEntity));
        return pageOptional.isPresent();
    }
    private boolean linkMeetsTheCondition(String url){
        int mySwitch = 0;
        if (!url.startsWith(siteParseDataSet.getDomain())) {
            mySwitch++;
        }
        if (url.contains("#") | checkPageExist(getShortUrl(url))) {
            mySwitch++;
        }
        if (siteParseDataSet.containLinksInWork(url)) {
            mySwitch++;
        }
        Integer countPage = siteParseDataSet.getUserProperties().getPageLimit();
        if (countPage !=null ) {
            if(siteParseDataSet.getSize() > countPage) {
               mySwitch++;
           }
        }
        return mySwitch == 0;
    }
    private String getShortUrl(String string){
        if (string.equals(siteParseDataSet.getDomain())){
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        String[] strings = string.split("/");
        for (int i=3;i < strings.length;i++) {
            sb.append("/");
            sb.append(strings[i]);
        }
        return sb.toString();
    }
}
