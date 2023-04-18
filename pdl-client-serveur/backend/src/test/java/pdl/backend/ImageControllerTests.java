package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public static void reset() {
		// reset Image class static counter
		ReflectionTestUtils.setField(My_Image.class, "count", Long.valueOf(0));
	}

	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@Order(2)
	public void getImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/1000000")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(3)
	public void getImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(4)
	public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
		this.mockMvc.perform(delete("/images")).andDo(print()).andExpect(status().isMethodNotAllowed());
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(delete("/images/1000000")).andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	@Order(6)
	public void deleteImageShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(delete("/images/0")).andDo(print()).andExpect(status().isOk());
	}

	@Test
	@Order(7)
	public void createImageShouldReturnSuccess() throws Exception {
		final ClassPathResource imgFile = new ClassPathResource("images/fire_truck.jpeg");
		MockMultipartFile multipartFile = new MockMultipartFile("file", "fire_truck.jpeg", "image/jpeg",
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile)).andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@Order(8)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "fire_truck.jpeg", "text/plain", "Test".getBytes());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile)).andDo(print())
				.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	@Order(9)
	public void testRetrieveImageInSubFolder() throws Exception { // Wrong test (test add image and not if the backend
																	// load at it start images files)
		final ClassPathResource imgFile = new ClassPathResource("images/test/hollywood.jpeg");
		final ClassPathResource imgFile2 = new ClassPathResource("images/test/subTest/android.jpeg");
		final ClassPathResource pdfFile = new ClassPathResource("images/test/test.pdf");
		MockMultipartFile multipartFile = new MockMultipartFile("file", "hollywood.jpg", "image/jpeg",
				imgFile.getInputStream());
		MockMultipartFile multipartFile2 = new MockMultipartFile("file", "android.jpg", "image/jpeg",
				imgFile2.getInputStream());
		MockMultipartFile multipartFilePdf = new MockMultipartFile("file", "test.pdf", "file/pdf",
				pdfFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile)).andDo(print())
				.andExpect(status().isCreated());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile2)).andDo(print())
				.andExpect(status().isCreated());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFilePdf)).andDo(print())
				.andExpect(status().isUnsupportedMediaType());
	}

	/*
	 * @Test
	 * 
	 * @Order(10)
	 * public void getImagefromEmptyFolder() throws Exception {
	 * mockMvc.perform(MockMvcRequestBuilders.get("/images/empty")).andExpect(
	 * MockMvcResultMatchers.status().isNotFound());
	 * }
	 */

}
