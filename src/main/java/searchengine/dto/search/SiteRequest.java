package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteRequest {
    String query;
    String site;
    Integer offset;
    Integer limit;
}
