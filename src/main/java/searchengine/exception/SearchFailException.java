package searchengine.exception;

import lombok.Getter;
import searchengine.dto.search.ErrorDetailsSearchResponse;

@Getter
public class SearchFailException extends RuntimeException  {
    private final ErrorDetailsSearchResponse errorDetailsSearchResponse;
       public SearchFailException(ErrorDetailsSearchResponse errorDetailsSearchResponse)  {
        this.errorDetailsSearchResponse = errorDetailsSearchResponse;
   }
}
