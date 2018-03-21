package no.kantega.rpmrepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

@Path("rpm")
public class RepoResource {

    private static final Logger log = LoggerFactory.getLogger(RepoResource.class);
    private final File repoDir;

    public RepoResource(File repoDir) {
        this.repoDir = repoDir;
    }

    @Path("upload/{app}/{version}/{file}")
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadRpm(@Context HttpServletRequest request,
                              @PathParam("app") String app,
                              @PathParam("version") String version,
                              @PathParam("file") String file) {
        var versionDir = new File(repoDir, version);
        versionDir.mkdirs();
        log.info("Got file {} {} {}", app, version, file);
        try(var os = new FileOutputStream(new File(versionDir,  file))) {
            long bytesTransfered = request.getInputStream().transferTo(os);
            long expectedLength = Long.parseLong(request.getHeader("Content-Length"));
            if(bytesTransfered != expectedLength) {
                log.info("Content length {} != {}", bytesTransfered, expectedLength);
                return Response.status(Response.Status.EXPECTATION_FAILED)
                        .entity("Bytes transfered (" + bytesTransfered + ") and Content-Length ("
                        + expectedLength + ") differ").build();
            }
            ProcessBuilder p = new ProcessBuilder(List.of("createrepo", this.repoDir.getAbsolutePath()))
                    .inheritIO()
                    .redirectErrorStream(true);
            Process start = p.start();
            start.onExit().thenAccept(process -> log.info("createrepo done"));
            getOutput(start);

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.accepted().build();
    }

    private static void getOutput(Process p) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            log.info(line);
        }
    }
}
