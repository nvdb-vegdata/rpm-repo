package no.kantega.rpmrepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

class RepoServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RepoServlet.class);

    private final String servletPath;
    private final File repoDir;

    public RepoServlet(String servletPath, File repoDir) {
        this.servletPath = servletPath;
        this.repoDir = repoDir;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullPath = req.getRequestURI()
                .replace("..", "");
        String path = fullPath
                .replace(servletPath, "");


        log.info("Request: " + fullPath);
        if(log.isDebugEnabled()) {
            log.debug("Headers: " + getHeaders(req));
        }

        File repoPath = new File(repoDir, path);
        if(!repoPath.exists()) {
            log.info(repoPath.getAbsolutePath() + " did not exist");
            resp.setStatus(404);
        } else if(repoPath.isDirectory()) {
            log.info("Listing directory " + repoPath.getAbsolutePath());
            listContent(repoPath, fullPath, resp);
        } else {
            streamFile(repoPath, req.getServletContext(), resp);
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headername = headerNames.nextElement();
            headers.put(headername, req.getHeader(headername));
        }
        return headers;
    }

    private void streamFile(File repoFile, ServletContext servletContext, HttpServletResponse resp) throws IOException {
        String mimeType = getMimeType(repoFile, servletContext);
        log.info("Streaming file {} with mimeType {}", repoFile.getAbsolutePath(), mimeType);
        resp.setHeader("Content-Type", mimeType);
        resp.setHeader("Content-Length", String.valueOf(repoFile.length()));
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + repoFile.getName() + "\"");

        try(var fis = new FileInputStream(repoFile)) {
            fis.transferTo(resp.getOutputStream());
        }
    }

    private String getMimeType(File repoFile, ServletContext servletContext) {
        String name = repoFile.getName();
        String mimeType = servletContext.getMimeType(name);
        if(nonNull(mimeType)) {
            return mimeType;
        }
        if(name.endsWith(".rpm")) {
            return "application/x-rpm";
        } else if(name.endsWith(".gz")) {
            return "application/x-gzip";
        } else if(name.endsWith(".bz2")) {
            return "application/bzip2";
        } else if(name.endsWith(".md5")) {
            return "application/x-checksum";
        } else if(name.endsWith(".sha1")) {
            return "application/x-checksum";
        } else if(name.endsWith(".asc")) {
            return "text/plain";
        } else if(name.endsWith(".key")) {
            return "application/octet-stream";
        }
        return mimeType;
    }

    private void listContent(File repoPath, String fullPath, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "text/html; charset=UTF-8");
        try (OutputStreamWriter out = new OutputStreamWriter(resp.getOutputStream())) {
            out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
                    "<html>" +
                    "<head><title>Index of " + fullPath + "</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h1>Index of " + fullPath + "</h1>\n" +
                    "<pre>Name Last modified Size</pre><hr/>\n" +
                    "<pre>");
            File[] files = repoPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    out.write("<a href=\"");
                    out.write(file.getName());
                    out.write("\">");
                    out.write(file.getName());
                    out.write("</a> ");
                    out.write(new Date(file.lastModified()).toString());
                    out.write("&nbsp");
                    out.write(String.valueOf(file.length()));
                    out.write(" bytes\n");
                }
            }
            out.write("</pre></body></html>");
        }
    }
}
