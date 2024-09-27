package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;

@Getter
@Setter
@ToString
public class SearchResponse {
    Boolean result;
    String error;
    int count;
    ArrayList<SiteData> data;

}
