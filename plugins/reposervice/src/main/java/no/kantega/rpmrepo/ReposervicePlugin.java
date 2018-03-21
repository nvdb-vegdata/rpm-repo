
package no.kantega.rpmrepo;

import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;
import org.kantega.reststop.jaxrsapi.ApplicationBuilder;
import org.kantega.reststop.servlet.api.ServletBuilder;

import javax.servlet.Filter;
import javax.ws.rs.core.Application;
import java.io.*;

@Plugin
public class ReposervicePlugin {

    @Export
    final Application repoResource;
    @Export
    final Filter repoServlet;

    public ReposervicePlugin(@Config String repoDirPath,
                             ApplicationBuilder appBuilder,
                             ServletBuilder servletBuilder) {
        File repoDir = new File(repoDirPath);
        repoResource = appBuilder.application()
                .singleton(new RepoResource(repoDir))
                .build();
        String servletPath = "/rpm/noarch";
        repoServlet = servletBuilder.servlet(new RepoServlet(servletPath, repoDir), servletPath + "/*");
    }

}
