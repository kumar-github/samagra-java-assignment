package tech.badprogrammer.swayamscraper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.badprogrammer.swayamscraper.entity.Course;

@Repository
public interface ScrapingRepository extends JpaRepository<Course, Integer> {
}
