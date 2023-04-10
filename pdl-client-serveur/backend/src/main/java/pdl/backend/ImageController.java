package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import java.util.Arrays;
import java.util.stream.Collectors;

import boofcv.io.image.ConvertBufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import boofcv.struct.image.GrayU8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.InvocationTargetException;
import boofcv.struct.image.Planar;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() {
    List<My_Image> images = imageDao.retrieveAll();
    ArrayNode nodes = mapper.createArrayNode();
    for (My_Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getType().toString());
      objectNode.put("size", image.getSize());
      objectNode.put("color", image.isColor());
      nodes.add(objectNode);
    }
    return nodes;
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id,
      /*
       * @RequestParam(name = "edit", required = false) long edit,
       */ @RequestParam(name = "algorithm", required = false) String algorithm,
      @RequestParam(name = "p1", required = false) Long p1,
      @RequestParam(name = "p2", required = false) Long p2,
      @RequestParam(name = "tableau", required = false) String[] tableauParam) {

    List<Integer> tableau = null;
    if (tableauParam != null) {
      tableau = Arrays.stream(tableauParam)
          .map(Integer::parseInt)
          .collect(Collectors.toList());
    }

    // System.out.println("algorithm = " + algorithm + " value = " + p1 + " value =
    // " + p2 + " tableau = " + tableau);
    Optional<My_Image> image = imageDao.retrieve(id);

    if (!image.isPresent()) {
      return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
    }

    try {
      if (algorithm != null) {
        // System.out.println("Image Traitement request");

        LevelProcessing color = new LevelProcessing();
        Method getMethod = selectMethodeFromParam(p1, p2, tableau, algorithm, color);

        Planar<GrayU8> input = getPlanarFromImage(image);
        InputStream inputStream;

        try {

          Planar<GrayU8> output = callMethodeFromParam(p1, p2, tableau, algorithm, getMethod, input);

          if (p1 == -1 && p2 == -1 && tableau != null && !(tableau.isEmpty())) {
            BufferedImage outputBytes = ConvertBufferedImage.convertTo(output, null, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
              ImageIO.write(outputBytes, "png", baos);
            } catch (IOException e) {
              throw e;
            }
            byte[] imageBytes = baos.toByteArray();

            My_Image img = NamePngImg(image.get(), algorithm, outputBytes, imageBytes);

            inputStream = new ByteArrayInputStream(imageBytes);
            imageDao.create(img);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(inputStream)); // Return the modified image

          } else {
            inputStream = updateImage(image, output);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(inputStream)); // Return the modified image
          }

        } catch (IllegalAccessException e) {
          return new ResponseEntity<>("Not resolving traitement function name: " + e.getMessage(),
              HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
          return new ResponseEntity<>("Illegal argument: " + e.getMessage(),
              HttpStatus.BAD_REQUEST);
        } catch (InvocationTargetException e) {
          return new ResponseEntity<>("Exception from called methode: " + e.getMessage(),
              HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
          return new ResponseEntity<>("Exception from called methode: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } else {
        // System.out.println("Simple Image Request");

        InputStream inputStream = new ByteArrayInputStream(image.get().getData());
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(new InputStreamResource(inputStream));
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Optional<My_Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      imageDao.delete(image.get());
      return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    String contentType = file.getContentType();
    if (!FilesMethodes.extensionCheck(contentType)) {
      return new ResponseEntity<>("Only JPEG JPG PNG file format supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      String originalFileName = file.getOriginalFilename(); // récupérer le nom de fichier original
      String extension = FilenameUtils.getExtension(originalFileName); // récupérer l'extension de fichier
      Path filePath = Files.createTempFile("temp", "." + extension); // créer le fichier temporaire avec l'extension
                                                                     // correcte
      Files.write(filePath, file.getBytes());
      File newFile = new File(filePath.getParent().toString(), originalFileName); // créer un nouveau fichier avec le
                                                                                  // nom d'origine
      Files.move(filePath, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING); // renommer le fichier temporaire
      My_Image img = FilesMethodes.createImage(newFile, file.getBytes());
      imageDao.create(img);
    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    return new ResponseEntity<>("Image uploaded", HttpStatus.CREATED);
  }

  // fonctions auxilières

  private Method selectMethodeFromParam(Long p1, Long p2, List<Integer> tableau, String algorithm,
      LevelProcessing color) throws Exception {

    if (algorithm.equals("Edit")){
      
      System.out.println("Edit");
      return color.getClass().getMethod(algorithm, Planar.class, Planar.class, List.class);

    }else {
    
      if (p1 == -1 && p2 == -1 && tableau != null && tableau.isEmpty()) {
        // System.out.println("0 parametre");
        return color.getClass().getMethod(algorithm, Planar.class);

      } else if (p1 != -1 && p2 == -1 && tableau != null && tableau.isEmpty()) {
        // System.out.println("1 parametre");
        return color.getClass().getMethod(algorithm, Planar.class, Long.class);

      } else if (p1 == -1 && p2 == -1 && tableau != null && !(tableau.isEmpty())) {
        // System.out.println("1 tableau");
        return color.getClass().getMethod(algorithm, Planar.class, List.class);

      }else if (p1 != -1 && p2 == -1 && tableau != null && !(tableau.isEmpty())) {
        //System.out.println("1 parametre && 1 tableau");
        return color.getClass().getMethod(algorithm, Planar.class, Long.class, List.class);

      } else {
        // System.out.println("2 parametres");
        return color.getClass().getMethod(algorithm, Planar.class, Long.class, Long.class);
      }
    }
  }

  private Planar<GrayU8> callMethodeFromParam(Long p1, Long p2, List<Integer> tableau, String algorithm,
  Method getMethod, Planar<GrayU8> input) throws Exception {

  
    if (algorithm.equals("Edit")){
      System.out.println("Edit");

      Optional<My_Image> image2 = imageDao.retrieve(p1);

      if (!image2.isPresent()) {
        return null;
      }

      Planar<GrayU8> input2 = getPlanarFromImage(image2);

      return (Planar<GrayU8>) getMethod.invoke(getMethod, input, input2, tableau);

    }else {        
      if (p1 == -1 && p2 == -1 && tableau != null && tableau.isEmpty()) {
        // System.out.println("0 parametre");
        return (Planar<GrayU8>) getMethod.invoke(getMethod, input);

      } else if (p1 != -1 && p2 == -1 && tableau != null && tableau.isEmpty()) {
        // System.out.println("1 parametre");
        return (Planar<GrayU8>) getMethod.invoke(getMethod, input, p1);

      } else if (p1 == -1 && p2 == -1 && tableau != null && !(tableau.isEmpty())) {
        // System.out.println("1 tableau");
        return (Planar<GrayU8>) getMethod.invoke(getMethod, input, tableau);

      }else if (p1 != -1 && p2 == -1 && tableau != null && !(tableau.isEmpty())) {
        //System.out.println("1 parametre && 1 tableau");
        return (Planar<GrayU8>) getMethod.invoke(getMethod, input, p1 ,tableau);

      }  else {
        // System.out.println("2 parametres");
        return (Planar<GrayU8>) getMethod.invoke(getMethod, input, p1, p2);

      }
    }
  }

  private Planar<GrayU8> getPlanarFromImage(Optional<My_Image> image) throws IOException {
    Planar<GrayU8> input;
    if (image.get().isColor()) {
      input = ConvertBufferedImage.convertFromPlanar(
          ImageIO.read(new ByteArrayInputStream(image.get().getData())),
          null, true, GrayU8.class);
    } else {
      input = new Planar<>(GrayU8.class, ImageIO.read(new ByteArrayInputStream(image.get().getData())).getWidth(),
          ImageIO.read(new ByteArrayInputStream(image.get().getData())).getHeight(), 3);
      GrayU8 tmp = ConvertBufferedImage.convertFromSingle(
          ImageIO.read(new ByteArrayInputStream(image.get().getData())),
          null, GrayU8.class);
      LevelProcessing.GrayU8ToPlanarGrayU8(tmp, input);
    }
    return input;
  }

  private InputStream updateImage(Optional<My_Image> image, Planar<GrayU8> output) throws IOException {
    // on sauvegarde l'image
    BufferedImage outputBytes = ConvertBufferedImage.convertTo(output, null, true);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(outputBytes, image.get().getType(), baos);
    } catch (IOException e) {
      throw e;
    }
    byte[] imageBytes = baos.toByteArray();

    InputStream inputStream = new ByteArrayInputStream(imageBytes);

    String[] params = { image.get().getName() };

    imageDao.update(new My_Image(image.get().getName(), imageBytes, image.get().getId(), image.get().getType(),
        image.get().getSize(), image.get().isColor()), params);
    return inputStream;
  }

  private My_Image NamePngImg(My_Image image, String algo, BufferedImage outputBytes, byte[] imageBytes) throws Exception {
    My_Image img;
    switch (algo) {
      case "Cut":
        img = FilesMethodes.createImageFromBuffurerImage(
            FilesMethodes.getNameWithoutExtension(image.getName()) + "_Cut.png", "png",
            outputBytes, imageBytes);
        break;
      case "Bottom":
        img = FilesMethodes.createImageFromBuffurerImage(
            FilesMethodes.getNameWithoutExtension(image.getName()) + "_Bottomed.png", "png",
            outputBytes, imageBytes);
        break;

      default:
        throw new Exception();
    }
    return img;
  }

}
