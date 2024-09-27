package searchengine.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "user-properties")
public class UserProperties {
        private String nameUserAgent;
        private String referUserSite;
        private Integer pageLimit;
}
