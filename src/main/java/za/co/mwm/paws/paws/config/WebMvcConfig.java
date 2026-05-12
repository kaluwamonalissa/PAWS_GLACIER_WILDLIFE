package za.co.mwm.paws.paws.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String UPLOADS_HANDLER = "/uploads/**";

    private final String uploadsDir;

    public WebMvcConfig(@Value("${paws.uploads.dir:uploads}") final String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        final String absolutePath =
                Paths.get(uploadsDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler(UPLOADS_HANDLER)
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
