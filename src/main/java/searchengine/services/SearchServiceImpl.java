package searchengine.services;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.search.*;
import searchengine.exception.SearchFailException;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.indexing.Lemmatizator;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final SitesList sitesList;
    private final Lemmatizator lemmatizator;
    private List<IndexEntity> indexEntityResponseList;
    private Map<String,List<IndexEntity>> indexEntitiesBySite;
    private Map<String,Float> absoluteRelevance;
    private ArrayList<SiteData> data;
    private int offset;
    private int limit;
    private boolean anErrorWasThrown;
    @Override
    public SearchResponse findPagesByQuery(SiteRequest siteRequest) {
        String query = siteRequest.getQuery();
        String site = siteRequest.getSite();
        offset = siteRequest.getOffset() != null ? siteRequest.getOffset() : 0;
        limit = siteRequest.getLimit() != null ? siteRequest.getLimit() : 20;
        List<String> siteUrls = getSiteUrls(site);
        List<String> addedLemmaList = new ArrayList<>();
        resetMyVariables();
        for (String siteUrl : siteUrls) {
            if (anErrorWasThrown){
                break;
            }
            if (!siteIsIndexing(siteUrl)){
                anErrorWasThrown = true;
                ErrorDetailsSearchResponse error = new ErrorDetailsSearchResponse("Cайт "+ siteUrl +" не проиндексирован," +
                        " дальнейший поиск невозможен");
                throw new SearchFailException(error);
            }
            Map<Integer, LemmaEntity> mapLemma;
            mapLemma = getLemmaEntitiesByQuery(query, siteUrl);
            if (mapLemma == null) {
                continue;
            }
            if (mapLemma.isEmpty()) {
                continue;
            }
            addedLemmaList = addIndexEntityResponseListByLemma(mapLemma);
        }
        if (!indexEntitiesBySite.isEmpty()) {
            return createSearchResponse(addedLemmaList);
        } else {
            ErrorDetailsSearchResponse error = new ErrorDetailsSearchResponse("Нет страниц отвечающих условию поиска");
            throw new SearchFailException(error);
        }
   }

   private void  resetMyVariables(){
       data =  new ArrayList<>();
       indexEntityResponseList = new ArrayList<>();
       absoluteRelevance = new HashMap<>();
       indexEntitiesBySite = new HashMap<>();
       anErrorWasThrown = false;
   }

   private boolean siteIsIndexing(String url){
        SiteEntity siteEntity = siteRepository.findByUrl(url);
       if(siteEntity != null) {
       return siteEntity.getStatus().equals(Status.INDEXED);}
       return false;
   }
   private List<String> addIndexEntityResponseListByLemma(Map<Integer, LemmaEntity> mapLemma){
        Map<Integer, LemmaEntity> sortedMapLemma = sortMapLemmaEntityByKey(mapLemma);
        List<String> lemmaList = new ArrayList<>();
        boolean itFirstLemma = true;
        for (Map.Entry<Integer, LemmaEntity> entry : sortedMapLemma.entrySet()) {
            lemmaList.add(entry.getValue().getLemma());
            addIndexToIndexEntitiesBySite(entry.getValue(),itFirstLemma);
            itFirstLemma = false;
        }
        return lemmaList;
    }
   private SearchResponse createSearchResponse(List<String> lemmaList){
        fillSiteData(lemmaList);
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(true);
        searchResponse.setCount( indexEntityResponseList.size());
        searchResponse.setData(data);
        return searchResponse;
    }
   private void fillSiteData(List<String> lemmaList){
        Float maxAbsoluteRelevancePages =  Collections.max(absoluteRelevance.values());
        indexEntitiesBySite.values().forEach(indexEntityResponseList::addAll);
        sortIndexEntityResponseListByRelativeRelevance();
        int pag = offset;
        int andOfPag = Math.min(indexEntityResponseList.size(), pag + limit);
        for (int i = pag; i < andOfPag; i++) {
            SiteData siteData = new SiteData();
            IndexEntity indexEntity = indexEntityResponseList.get(i);
            Snippet snippet = getSnippet(indexEntity.getPageEntity(), lemmaList);
            String pageUrl = getUrlByIndexEntity(indexEntity);
            siteData.setRelevance( String.valueOf(absoluteRelevance.get(pageUrl)/maxAbsoluteRelevancePages));
            siteData.setSiteName(indexEntity.getLemmaEntity().getSiteEntity().getName());
            siteData.setSnippet(snippet.getText());
            siteData.setUri(indexEntity.getPageEntity().getPath());
            siteData.setTitle(snippet.getTitle());
            siteData.setSite(indexEntity.getLemmaEntity().getSiteEntity().getUrl() + siteData.getUri());
            data.add(siteData);
        }
    }
   private String getUrlByIndexEntity(IndexEntity indexEntity){
        return  indexEntity.getLemmaEntity().getSiteEntity().getUrl()
                + indexEntity.getPageEntity().getPath();
    }
   private void sortIndexEntityResponseListByRelativeRelevance(){
        Float maxAbsoluteRelevancePages =  Collections.max(absoluteRelevance.values());
        if (!indexEntityResponseList.isEmpty()){
            Comparator<IndexEntity> comparator = Comparator.comparingDouble(entity ->{
            String pageUrl = getUrlByIndexEntity(entity);
            return (double)  absoluteRelevance.get(pageUrl)/maxAbsoluteRelevancePages;});
        indexEntityResponseList.sort(comparator.reversed());
        }
   }
   private void addIndexToIndexEntitiesBySite(LemmaEntity lemmaEntity,boolean itFirstLemma){
        String siteUrl = lemmaEntity.getSiteEntity().getUrl();
        List<IndexEntity> indexEntityList = indexRepository.findIndexBySiteUrlAndLemmaEntity(siteUrl, lemmaEntity);
        if (itFirstLemma){
            indexEntitiesBySite.put(siteUrl,indexEntityList);
        } else {
            indexEntitiesBySite.replace(siteUrl,getIntersection(indexEntitiesBySite.get(siteUrl),indexEntityList));
        }
        calculateAndPutValueToAbsoluteRelevance(indexEntityList,itFirstLemma);
    }
   private void calculateAndPutValueToAbsoluteRelevance(List<IndexEntity> indexEntities,boolean itFirstLemma) {
        for (IndexEntity indexEntity : indexEntities) {
            Float rank = indexEntity.getRank();
            String pageUrl = getUrlByIndexEntity(indexEntity);
            if (absoluteRelevance.containsKey(pageUrl)) {
                absoluteRelevance.replace(pageUrl, absoluteRelevance.get(pageUrl) + rank);
            } else {
                if (itFirstLemma) {
                    absoluteRelevance.put(pageUrl, rank);
                }
            }
        }
    }
   private List<IndexEntity> getIntersection(List<IndexEntity> first, List <IndexEntity> second){
        List<IndexEntity> intersectionListIndexEntities = new ArrayList<>();
        if(first.isEmpty() | second.isEmpty()){
            return intersectionListIndexEntities;
        }
        for (IndexEntity entityFirst:first){
            for(IndexEntity entitySecond:second){
               String  firstPageUrl = entityFirst.getPageEntity().getPath();
               String  secondPageUrl = entitySecond.getPageEntity().getPath();
                if ( firstPageUrl.equals(secondPageUrl)){
                    intersectionListIndexEntities.add(entityFirst);
                }
            }
        }
        return intersectionListIndexEntities;
    }
   private  Map <Integer,LemmaEntity> getLemmaEntitiesByQuery(String query, String siteUrl) {
        Map <Integer,LemmaEntity> mapLemma = new LinkedHashMap<>();
        Map<String, Integer> lemMap = lemmatizator.collectLemmas(query);
            for (Map.Entry<String, Integer> entry : lemMap.entrySet()) {
                String lemma = entry.getKey();
                Optional <LemmaEntity> optionalLemmaEntity = Optional.ofNullable(lemmaRepository.findLemmaByLemmaAndSiteUrl(lemma,siteUrl));
                if (optionalLemmaEntity.isEmpty()) {
                    return null;
                }
                LemmaEntity lemmaEntity = optionalLemmaEntity.get();
                int frequency = lemmaEntity.getFrequency();
                boolean writeOk = false;
                do{
                    if (mapLemma.containsKey(frequency)){
                        if (mapLemma.get(frequency).getLemma().equals(lemmaEntity.getLemma())){
                            writeOk = true;
                        } else {
                            frequency++;
                        }
                    } else {
                        mapLemma.put(frequency,lemmaEntity);
                        writeOk = true;
                    }
                } while (!writeOk);
            }
        return mapLemma;
   }
   private List<String> getSiteUrls(String siteUrl){
        List<String> siteUrls = new ArrayList<>();
        if(siteUrl == null){
            for (Site itemSite:sitesList.getSites()){
                siteUrls.add(itemSite.getUrl());
            }
        } else {
            siteUrls.add(siteUrl);
        }
        return siteUrls;
    }
   private Map<Integer,LemmaEntity> sortMapLemmaEntityByKey(Map<Integer,LemmaEntity> unsortedMap){
        List<Map.Entry<Integer, LemmaEntity>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort(Map.Entry.comparingByKey());
        Map<Integer, LemmaEntity> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, LemmaEntity> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
   }
   private Snippet getSnippet(PageEntity pageEntity, List<String>  lemmaList)  {
     Snippet snippet = new Snippet();
     StringBuilder text = new StringBuilder();
     List<String> stringList = new ArrayList<>();
     int  fragmentLength = 70;
     Document doc = Jsoup.parse(pageEntity.getContent());
     String[] russianText = doc.text().split("\\s+");
     for (int i = 0; i < russianText.length; i++) {
         String russianWord = russianText[i];
         String lemmaOfWord = lemmatizator.getNormalForm(russianWord);
         if (wordContainsLemmaList(lemmaOfWord, lemmaList) & stringList.size() <  fragmentLength) {
             stringList.addAll(addTextSnippet(russianText,i,lemmaList));
         }
     }
     for (String str : stringList) {
         text.append(str);
     }
     snippet.setTitle(doc.title());
     snippet.setText(text.toString());
     return snippet;
    }
   private boolean wordContainsLemmaList(String word,List<String> wordList){
        for (String item:wordList){
            if (item.equals(word)){
                return true;
            }
        }
        return false;
   }
   private List<String> addTextSnippet(String[] textArray,int index,List<String> lemmaList){
        List<String> listString = new ArrayList<>();
        int numberOfWords = 8;
        int start = Math.max(index - numberOfWords, 0);
        int end = Math.min(index + numberOfWords, textArray.length);
        listString.add("..." );
        for (int i = start; i < end; i++ ){
            String lemmaOfWord = lemmatizator.getNormalForm(textArray[i]);
            if (wordContainsLemmaList(lemmaOfWord,lemmaList)){
                listString.add(" <b>");
                listString.add(textArray[i]);
                listString.add(" </b>");
            } else {
                listString.add(textArray[i]);
            }
            listString.add(" ");
        }
        listString.add("... " );
        return listString;
   }
}
