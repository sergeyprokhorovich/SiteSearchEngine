package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class Lemmatizator {
    private final LuceneMorphology luceneMorphology;
    private final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public Lemmatizator() throws IOException {
        this.luceneMorphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }
            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }
            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }


    public String getNormalForm(String word){
        String wordM = word.toLowerCase(Locale.ROOT).replaceAll("([^а-я\\s])", "");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordM.length(); i++) {
            char currentChar = wordM.charAt(i);
            if (Character.isLetter(currentChar) && Character.isLowerCase(currentChar)) {
                result.append(currentChar);
            }
        }
        wordM = result.toString();
        if (wordM.isBlank()) {
            return null;
        }
        List<String> wordBaseForms = luceneMorphology.getMorphInfo(wordM);
        if (anyWordBaseBelongToParticle(wordBaseForms)) {
            return null;
        }
        List<String> normalForms = luceneMorphology.getNormalForms(wordM);
        if (normalForms.isEmpty()) {
            return null;
        }
        return normalForms.get(0);
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");

    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
 }
