package tech.badprogrammer.swayamscraper.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.badprogrammer.swayamscraper.config.ModelMapperConfig;
import tech.badprogrammer.swayamscraper.dto.CourseDto;
import tech.badprogrammer.swayamscraper.entity.Course;
import tech.badprogrammer.swayamscraper.repository.CourseRepository;
import tech.badprogrammer.swayamscraper.util.Span;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static tech.badprogrammer.swayamscraper.config.AsyncExecutionConfig.ASYNC_EXECUTOR;

@Service
@Slf4j
public class CourseService {

    private static final String ENTITY_TO_DTO_MAP_NAME = "courseEntityToDto";

    @Autowired
    private CourseRepository                           courseRepository;
    @Autowired
    private ModelMapper                                modelMapper;
    @Autowired
    private ModelMapperConfig.StringStrippingConverter stringStrippingConverter;
    @Autowired
    private ModelMapperConfig.DateToIsoConverter       dateToIsoConverter;

    @PostConstruct
    private void init() {
        createEntityToDto();
        modelMapper.validate();
    }

    @Async(ASYNC_EXECUTOR)
    public CompletableFuture<List<CourseDto>> getAll() {
        log.info("Get All Courses...");
        try (var s = Span.of("To fetch all courses", log)) {
            List<CourseDto> result = StreamSupport.stream(courseRepository.findAll()
                                                                          .spliterator(), true)
                                                  .map(this::entityToDto)
                                                  .collect(Collectors.toList());
            log.info("Successfully fetched {} courses from database.", result.size());
            return CompletableFuture.completedFuture(result);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private void createEntityToDto() {
        //
        // 1. Check if a type map already exists for the particular source and destination.
        // 2. Create a new type map if one does not exists already.
        // 3. Create a property map with the properties needed.
        // 4. Make sure we are not leaving this method without a type map.
        //
        TypeMap<Course, CourseDto> courseEntityToDtoTypeMap = modelMapper.getTypeMap(Course.class, CourseDto.class, ENTITY_TO_DTO_MAP_NAME);
        if (courseEntityToDtoTypeMap == null) {
            courseEntityToDtoTypeMap = modelMapper.createTypeMap(Course.class, CourseDto.class, ENTITY_TO_DTO_MAP_NAME)
                                                  .addMappings(mapper -> mapper.using(stringStrippingConverter)
                                                                               .map(Course::getTitle, CourseDto::setTitle))
                                                  .addMappings(mapper -> mapper.using(stringStrippingConverter)
                                                                               .map(Course::getCategory, CourseDto::setCategory))
                                                  .addMappings(mapper -> mapper.using(stringStrippingConverter)
                                                                               .map(Course::getInstructorName, CourseDto::setInstructorName))
                                                  .addMappings(mapper -> mapper.using(stringStrippingConverter)
                                                                               .map(Course::getInstructorInstitute, CourseDto::setInstructorInstitute))
                                                  .addMappings(mapper -> mapper.using(dateToIsoConverter)
                                                                               .map(Course::getStartDate, CourseDto::setStartDate))
                                                  .addMappings(mapper -> mapper.using(dateToIsoConverter)
                                                                               .map(Course::getEndDate, CourseDto::setEndDate))
                                                  .addMappings(mapper -> mapper.using(dateToIsoConverter)
                                                                               .map(Course::getExamDate, CourseDto::setExamDate))
                                                  .addMappings(mapper -> mapper.using(dateToIsoConverter)
                                                                               .map(Course::getEnrollmentEndDate, CourseDto::setEnrollmentEndDate))
                                                  .addMappings(mapper -> mapper.using(stringStrippingConverter)
                                                                               .map(Course::getNcCode, CourseDto::setNcCode));

        }
        assert courseEntityToDtoTypeMap != null;
    }

    private CourseDto entityToDto(Course entity) {
        final TypeMap<Course, CourseDto> entityToDto = modelMapper.getTypeMap(Course.class, CourseDto.class, ENTITY_TO_DTO_MAP_NAME);
        final CourseDto                  result      = entityToDto.map(entity);
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final Cache<Integer, CourseDto> courseCache = CacheBuilder.newBuilder()
                                                                             .maximumSize(5000)
                                                                             .initialCapacity(4000)
                                                                             .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                                                                             .expireAfterWrite(1, TimeUnit.HOURS)
                                                                             .build();
}
