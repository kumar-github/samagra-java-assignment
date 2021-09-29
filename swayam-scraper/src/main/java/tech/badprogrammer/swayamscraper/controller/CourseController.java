package tech.badprogrammer.swayamscraper.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.badprogrammer.swayamscraper.dto.CourseDto;
import tech.badprogrammer.swayamscraper.service.CourseService;
import tech.badprogrammer.swayamscraper.service.ScrapingService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/swayamScraper/v1/courseScraper")
@Api
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ScrapingService scrapingService;

    @ApiOperation(value = "GET All Courses.",
            notes = "Retrieves a list of Courses.",
            tags = {"Course"},
            response = CourseDto.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Courses found and returned."),
            @ApiResponse(code = 404, message = "No Course found.")
    })
    @GetMapping
    public CompletableFuture<List<CourseDto>> getAll() {
        CompletableFuture<List<CourseDto>> result = courseService.getAll();
        return result;
    }

    @ApiOperation(value = "Scrape and then GET All Courses.",
            notes = "Scrape and then retrieves a list of Courses.",
            tags = {"Course"},
            response = CourseDto.class,
            responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Courses found and returned."),
            @ApiResponse(code = 404, message = "No Course found.")
    })
    @PostMapping
    public CompletableFuture<List<CourseDto>> scrapeAndGetAll() {
        CompletableFuture<List<CourseDto>> result = scrapingService.scrapeAndGetAllCourses();
        return result;
    }
}
