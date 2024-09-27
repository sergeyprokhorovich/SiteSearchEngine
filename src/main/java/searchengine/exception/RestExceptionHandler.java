package searchengine.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.indexing.ErrorDetailsIndexingResponse;
import searchengine.dto.search.ErrorDetailsSearchResponse;
import searchengine.dto.search.SearchResponse;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler({IndexingFailException.class})
    public ResponseEntity<ErrorDetailsIndexingResponse> handleCustomExceptionIndexing (IndexingFailException ex) {
        ErrorDetailsIndexingResponse details= ex.getErrorDetailsIndexingResponse();
        ErrorDetailsIndexingResponse myErrorResponse = new ErrorDetailsIndexingResponse(details.error(),details.status());
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR).
                contentType(MediaType.APPLICATION_JSON).
                body(myErrorResponse);
    }

    @ExceptionHandler({SearchFailException.class})
    public ResponseEntity<SearchResponse> handleCustomExceptionSearch (SearchFailException ex) {
        ErrorDetailsSearchResponse details= ex.getErrorDetailsSearchResponse();
        return ResponseEntity.ok(details.getSearchResponse());
    }

}
