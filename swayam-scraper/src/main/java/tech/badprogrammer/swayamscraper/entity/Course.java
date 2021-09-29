package tech.badprogrammer.swayamscraper.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@lombok.Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int     id;
    @Column(name = "course_id", nullable = false)
    private String  courseId;
    @Column(name = "title", nullable = false)
    private String  title;
    @Column(name = "url", nullable = false)
    private String  url;
    @Column(name = "picture_url", nullable = false, length = 500)
    private String  pictureUrl;
    @Column(name = "summary")
    private String  summary;
    @Column(name = "category")
    private String  category;
    @Column(name = "instructor_name", nullable = false)
    private String  instructorName;
    @Column(name = "instructor_institute")
    private String  instructorInstitute;
    @Column(name = "enrolled", nullable = false)
    private boolean enrolled;
    @Column(name = "open_for_registration", nullable = false)
    private boolean openForRegistration;
    @Column(name = "show_in_explorer", nullable = false)
    private boolean showInExplorer;
    @Column(name = "start_date")
    private Date    startDate;
    @Column(name = "end_date")
    private Date    endDate;
    @Column(name = "exam_date")
    private Date    examDate;
    @Column(name = "enrollment_end_date")
    private Date    enrollmentEndDate;
    @Column(name = "estimated_workload")
    private String  estimatedWorkload;
    @Column(name = "tags")
    private String  tags;
    @Column(name = "featured", nullable = false)
    private boolean featured;
    @Column(name = "credits")
    private int     credits;
    @Column(name = "weeks")
    private int     weeks;
    @Column(name = "node_code")
    private String  nodeCode;
    @Column(name = "nc_code")
    private String  ncCode;
}
