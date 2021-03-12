package io.pivotal.pal.tracker.allocations;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final Map<Long, ProjectInfo> projectsCache = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String registrationServerEndpoint;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo projectInfo = restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class);

        projectsCache.put(projectId, projectInfo);
        logger.info("Putting project into cache with id {}", projectId);

        return projectInfo;
    }

    public ProjectInfo getProjectFromCache (long projectId, Throwable cause) {
        logger.info("Getting project from cache with id {} with cause {}", projectId, cause);

        return projectsCache.get(projectId);
    }
}
