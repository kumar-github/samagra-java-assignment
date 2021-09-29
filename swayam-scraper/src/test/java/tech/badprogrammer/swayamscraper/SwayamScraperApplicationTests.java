package tech.badprogrammer.swayamscraper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import tech.badprogrammer.swayamscraper.controller.CourseController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SwayamScraperApplicationTests {

    private static final String URL = "http://localhost:8001/api/swayamScraper/v1/courseScraper";

    @Autowired
    private CourseController courseController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        Assertions.assertThat(courseController).isNotNull();
    }

    @Test
    public void testGetCourse() throws Exception {
        this.mockMvc.perform(get(URL))
                    .andDo(print())
                    .andExpect(status().isOk());
    }

    @Test
    public void testPostCourse() throws Exception {
        this.mockMvc.perform(post(URL))
                    .andDo(print())
                    .andExpect(status().isOk());
    }
}
