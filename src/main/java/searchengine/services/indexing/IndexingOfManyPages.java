package searchengine.services.indexing;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingOfManyPages {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Lemmatizator lemmatizator;
    public synchronized void indexingPage(PageEntity pageEntity){
           if (pageEntity.getCode() == 200) {
                Document doc = Jsoup.parse(pageEntity.getContent());
                Map<String, Integer> lemMap = lemmatizator.collectLemmas(doc.text());
                SiteEntity siteEntity = pageEntity.getSiteEntity();
                for (Map.Entry<String, Integer> entry : lemMap.entrySet()) {
                    String lemma = entry.getKey();
                    int frequency = entry.getValue();
                    Optional<LemmaEntity> optionalLemmaEntity = Optional.ofNullable(lemmaRepository.findLemmaByLemmaAndSiteEntity(lemma,siteEntity));
                    LemmaEntity lemmaEntity;
                    if (optionalLemmaEntity.isPresent()) {
                        lemmaEntity = optionalLemmaEntity.get();
                        increaseLemma(lemmaEntity);
                    } else {
                        lemmaEntity = createLemma(lemma, siteEntity);
                    }
                    createIndex(lemmaEntity, pageEntity, frequency);
                }
            }
    }
    private synchronized LemmaEntity createLemma(String lemma, SiteEntity siteEntity){
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(1);
        lemmaEntity.setSiteEntity(siteEntity);
        return lemmaRepository.save(lemmaEntity);
    }

    private synchronized void createIndex(LemmaEntity lemmaEntity,PageEntity pageEntity,int frequency){
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setRank((float) frequency);
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setPageEntity(pageEntity);
        indexRepository.save(indexEntity);
    }

    private synchronized void increaseLemma(LemmaEntity lemmaEntity){
        lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
        lemmaRepository.save(lemmaEntity);
    }
}



