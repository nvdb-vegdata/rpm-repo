
package no.kantega.rpmrepo;

import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.jaxrsapi.ApplicationBuilder;

import javax.ws.rs.core.Application;

@Plugin
public class ReposervicePlugin {

    @Export
    final Application repoResource;

    public ReposervicePlugin(ApplicationBuilder appBuilder) {
        repoResource = appBuilder.application()
                .singleton(new RepoResource())
                .build();
    }

}
