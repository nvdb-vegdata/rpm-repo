
package no.kantega.rpmrepo;

import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.jaxrsapi.ApplicationBuilder;

import javax.ws.rs.core.Application;
import java.io.File;

@Plugin
public class ReposervicePlugin {

    @Export
    final Application repoResource;

    public ReposervicePlugin(@Config String repoDirPath, ApplicationBuilder appBuilder) {
        repoResource = appBuilder.application()
                .singleton(new RepoResource(new File(repoDirPath)))
                .build();
    }

}
