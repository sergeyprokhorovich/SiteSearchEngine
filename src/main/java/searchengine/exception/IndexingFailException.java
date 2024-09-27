package searchengine.exception;

import lombok.Getter;
import searchengine.dto.indexing.ErrorDetailsIndexingResponse;

@Getter
public class IndexingFailException extends RuntimeException {
    private final ErrorDetailsIndexingResponse errorDetailsIndexingResponse;
    public IndexingFailException(ErrorDetailsIndexingResponse errorDetailsIndexingResponse)  {
       this.errorDetailsIndexingResponse = errorDetailsIndexingResponse;
    }
}
