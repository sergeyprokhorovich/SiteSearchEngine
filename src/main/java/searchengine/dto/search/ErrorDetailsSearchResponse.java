package searchengine.dto.search;
import lombok.Getter;
import java.util.ArrayList;

@Getter
public class ErrorDetailsSearchResponse {

    private final SearchResponse searchResponse;
    public ErrorDetailsSearchResponse(String error){
        searchResponse = new SearchResponse();
        searchResponse.setResult(false);
        searchResponse.setError(error);
        searchResponse.setCount(0);
        searchResponse.setData(new ArrayList<>());
    }
}
