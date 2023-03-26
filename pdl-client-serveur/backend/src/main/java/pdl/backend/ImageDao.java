package pdl.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

// mvn clean install
// mvn --projects backend spring-boot:run

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public ImageDao() throws Exception {
    File imgFile;
    try {
      imgFile = new ClassPathResource("images").getFile();
      if (imgFile.exists()) {
        FilesMethodes.loadArboFiles(images, imgFile);
      } else {
        throw new Exception("Repertory image doesn't exist.");
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    return Optional.ofNullable(images.get(id));
  }

  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  @Override
  public void create(final Image img) {
    images.put(img.getId(), img);
  }

  @Override
  public void update(final Image img, final String[] params) {
    img.setName(Objects.requireNonNull(params[0], "Name cannot be null"));

    images.put(img.getId(), img);
  }

  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }
}
