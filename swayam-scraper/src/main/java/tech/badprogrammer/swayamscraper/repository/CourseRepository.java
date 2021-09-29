package tech.badprogrammer.swayamscraper.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.badprogrammer.swayamscraper.entity.Course;

@Repository
@Transactional(readOnly = true)
public interface CourseRepository extends ReadOnlyRepository<Course, Integer> {
}
