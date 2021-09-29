package tech.badprogrammer.swayamscraper.dto;

import lombok.Data;

@Data
public class CourseDto {

    private int     id;
    private String  courseId;
    private String  title;
    private String  url;
    private String  pictureUrl;
    private String  summary;
    private String  category;
    private String  instructorName;
    private String  instructorInstitute;
    private boolean enrolled;
    private boolean openForRegistration;
    private boolean showInExplorer;
    private String  startDate;
    private String  endDate;
    private String  examDate;
    private String  enrollmentEndDate;
    private String  estimatedWorkload;
    private String  tags;
    private boolean featured;
    private int     credits;
    private int     weeks;
    private String  nodeCode;
    private String  ncCode;
}
