package searchengine.dto.indexing;

import lombok.Data;

@Data
public class PageDataResponse {
    private String url;
    private String path;
    private int code;
    private String content;
}
