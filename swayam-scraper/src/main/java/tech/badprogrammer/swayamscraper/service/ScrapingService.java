package tech.badprogrammer.swayamscraper.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.badprogrammer.swayamscraper.dto.CourseDto;
import tech.badprogrammer.swayamscraper.entity.Course;
import tech.badprogrammer.swayamscraper.repository.ScrapingRepository;
import tech.badprogrammer.swayamscraper.util.Span;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static tech.badprogrammer.swayamscraper.config.AsyncExecutionConfig.ASYNC_EXECUTOR;

@Service
@Slf4j
public class ScrapingService {

    private static final String url             = "https://swayam.gov.in/modules/gql/query?q={q}";
    private static final String upComingCourses = "{courseList(args: {includeClosed: true, filterText: \"\", category: \"\", status: \"Upcoming\", tags: \"\", duration: \"all\", examDate: \"all\", credits: \"all\", ncCode: \"all\",}) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}}}";
    private static final String onGoingCourses  = "{courseList(args: {includeClosed: true, filterText: \"\", category: \"\", status: \"Ongoing\", tags: \"\", duration: \"all\", examDate: \"all\", credits: \"all\", ncCode: \"all\",}) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}}}";
    private static final String allCourses      = "{courseList(args: {includeClosed: true, filterText: \"\", category: \"\", tags: \"\", duration: \"all\", examDate: \"all\", credits: \"all\", ncCode: \"all\", }) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}}}";
    private static final String sample          = "{courseList(args: {includeClosed: false, filterText: \"\", category: \"Multidisciplinary\", status: \"Upcoming\", tags: \"\", duration: \"all\", examDate: \"all\", credits: \"all\", ncCode: \"CEC\",}) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}}}";

    private static final String       NODE_TO_COURSE_MAP_NAME = "nodeToCourse";
    private static final ObjectMapper objectMapper            = new ObjectMapper();

    @Autowired
    private ScrapingRepository scrapingRepository;
    @Autowired
    private CourseService      courseService;
    @Autowired
    private ModelMapper        modelMapper;
    @Autowired
    private RestTemplate       restTemplate;

    @PostConstruct
    private void init() {
        createNodeToCourse();
        modelMapper.validate();
    }

    private CompletableFuture<List<CourseDto>> getAllCourses() {
        CompletableFuture<List<CourseDto>> result = courseService.getAll();
        return result;
    }

    private void saveAllCourses(List<Course> courses) {
        deleteAllCourses();
        List<Course> result = scrapingRepository.saveAll(courses);
        log.info("Successfully saved {} courses.", result.size());
    }

    private void deleteAllCourses() {
        log.info("Deleting all existing courses.");
        scrapingRepository.deleteAllInBatch();
        log.info("Successfully deleted all courses from database.");
    }

    @Async(ASYNC_EXECUTOR)
    public CompletableFuture<List<CourseDto>> scrapeAndGetAllCourses() {
        scrapeAndSave();
        return getAllCourses();
    }

    @Scheduled(cron = "@midnight")
    public void scrapeAndSave() {
        try (var s = Span.of("To scrape and save all courses", log)) {
            saveAllCourses(scrapeAllCourses());
        }
    }

    private List<Course> scrapeAllCourses() {
        //
        // Go to the internet and scrape but make sure you don’t overload and you have a fall-back plan,
        // I.e. you allow the available data to be returned if the load is too high and the wait is too long
        //
        log.info("Started scraping {} for courses.", url);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, allCourses);
        switch (responseEntity.getStatusCode()) {
            case OK: {
                var            response       = sanitize(responseEntity.getBody());
                CourseResponse courseResponse = jsonToCourseResponseInstance(response);
                List<CourseResponse.Node> courses = courseResponse.getData()
                                                                  .getCourseData()
                                                                  .getEdges()
                                                                  .stream()
                                                                  .map(CourseResponse.Edge::getNode)
                                                                  .collect(Collectors.toList());
                List<Course> result = courses.stream()
                                             .map(this::nodeToCourse)
                                             .collect(Collectors.toList());
                log.info("Successfully scraped {} courses from {}.", result.size(), url);
                return result;
            }
            case BAD_REQUEST: {
                /*
                 * Error code 400 received. Incorrect request.
                 */
                throw new UnsupportedOperationException("Invalid request sent to Server. Error: " + BAD_REQUEST.value());
            }
            case NOT_FOUND: {
                /*
                 * Error code 404 received. Resource not found.
                 */
                throw new RuntimeException("No courses found.");
            }
            case INTERNAL_SERVER_ERROR: {
                /*
                 * Error code 500 received. Unable to access Server
                 */
                throw new RuntimeException("Unable to access Server. Error: " + INTERNAL_SERVER_ERROR.value());
            }
            default: {
                throw new RuntimeException("Unable to retrieve courses from Server. Call returned status: " + responseEntity.getStatusCode());
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private String sanitize(String body) {
        return (body != null && !body.isEmpty()) ? body.substring(5) : body;
    }

    private void createNodeToCourse() {
        TypeMap<CourseResponse.Node, Course> nodeToCourseTypeMap = modelMapper.getTypeMap(CourseResponse.Node.class, Course.class, NODE_TO_COURSE_MAP_NAME);
        if (nodeToCourseTypeMap == null) {
            nodeToCourseTypeMap = modelMapper.createTypeMap(CourseResponse.Node.class, Course.class, NODE_TO_COURSE_MAP_NAME)
                                             .addMappings(mapper -> mapper.skip(Course::setId))
                                             .addMappings(mapper -> mapper.using(courseCategoryConverter())
                                                                          .map(CourseResponse.Node::getCategory, Course::setCategory))
                                             .addMapping(CourseResponse.Node::getId, Course::setCourseId);
        }
        assert nodeToCourseTypeMap != null;
    }

    private Course nodeToCourse(CourseResponse.Node node) {
        TypeMap<CourseResponse.Node, Course> nodeToCourseTypeMap = modelMapper.getTypeMap(CourseResponse.Node.class, Course.class, NODE_TO_COURSE_MAP_NAME);
        Course                               result              = nodeToCourseTypeMap.map(node);
        return result;
    }

    private static CourseResponse jsonToCourseResponseInstance(String json) {
        try {
            return objectMapper.readValue(new StringReader(json), CourseResponse.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private CourseResponse.CourseCategoryConverter courseCategoryConverter() {
        final CourseResponse.CourseCategoryConverter result = new CourseResponse.CourseCategoryConverter();
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @lombok.Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    private static class CourseResponse implements Serializable {

        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Data        data;
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private List<Error> errors;

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        private static class Data implements Serializable {

            @EqualsAndHashCode.Exclude
            @ToString.Exclude
            @JsonProperty("courseList")
            private CourseData courseData;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        private static class CourseData implements Serializable {

            @EqualsAndHashCode.Exclude
            @ToString.Exclude
            private List<Edge> edges;
            @EqualsAndHashCode.Exclude
            @ToString.Exclude
            private PageInfo   pageInfo;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        private static class Edge implements Serializable {

            @EqualsAndHashCode.Exclude
            @ToString.Exclude
            private Node node;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Node implements Serializable {

            private String         id;
            private String         title;
            private String         url;
            @JsonProperty("coursePictureUrl")
            private String         pictureUrl;
            @JsonProperty("explorerSummary")
            private String         summary;
            @EqualsAndHashCode.Exclude
            @ToString.Exclude
            private List<Category> category;
            @JsonProperty("explorerInstructorName")
            private String         instructorName;
            private String         instructorInstitute;
            private boolean        enrolled;
            private boolean        openForRegistration;
            private boolean        showInExplorer;
            private Date           startDate;
            private Date           endDate;
            private Date           examDate;
            private Date           enrollmentEndDate;
            private String         estimatedWorkload;
            private String         tags;
            private boolean        featured;
            private Integer        credits;
            private Integer        weeks;
            private String         nodeCode;
            private String         ncCode;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Category {

            private String name;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        private static class PageInfo implements Serializable {

            private String  endCursor;
            private boolean hasNextPage;
        }

        @lombok.Data
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = false)
        private static class Error implements Serializable {

            private String message;
        }

        public static class CourseCategoryConverter extends AbstractConverter<List<CourseResponse.Category>, String> {
            @Override
            public String convert(List<CourseResponse.Category> source) {
                return source != null && !source.isEmpty() ? source.get(0)
                                                                   .getName() : null;
            }
        }
    }
}
