package org.eclipse.edc.plugins.autodoc.tasks;

import org.gradle.api.artifacts.Dependency;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A dependency that is located in a remote repository, such as Maven Central
 */
public class HttpSource extends DependencySource {
    private final HttpClient httpClient;


    /**
     * @param dependency the dependency in question
     * @param uri        the location where the physical file exists
     * @param classifier what type of dependency we have, e.g. sources, sources, manifest etc
     * @param type       file extension
     */
    public HttpSource(Dependency dependency, URI uri, String classifier, String type) {
        super(dependency, uri, classifier, type);
        httpClient = HttpClient.newHttpClient();

    }

    /**
     * A HEAD request is performed to check if the file is actually present at the remote location.
     */
    @Override
    public boolean exists() {
        var headRequest = HttpRequest.newBuilder()
                .uri(uri())
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            var response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens an input stream to the remote file. If the remote file does not exist, {@code null} is returned.
     * @throws RuntimeException if the HTTP request raises an {@link IOException} or an {@link InterruptedException}
     */
    @Override
    public InputStream inputStream() {
        var request = HttpRequest.newBuilder().uri(uri()).GET().build();
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(response.statusCode() == 200) {
            return response.body();
        }
        return null;
    }
}
