package no.kantega.rpmrepo;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Path("rpm")
public class RepoResource {

    @Path("upload/{version}/{file}")
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadRpm(@Context HttpServletRequest request,
                              @PathParam("version") String version,
                              @PathParam("file") String file) {

        try(var os = new FileOutputStream(new File("/tmp/" + file))) {
            long bytesTransfered = request.getInputStream().transferTo(os);
            long expectedLength = Long.parseLong(request.getHeader("Content-Length"));
            if(bytesTransfered != expectedLength) {
                return Response.status(Response.Status.EXPECTATION_FAILED)
                        .entity("Bytes transfered (" + bytesTransfered + ") and Content-Length ("
                        + expectedLength + ") differ").build();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Response.ok().build();
    }
}
