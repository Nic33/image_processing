package pdl.backend;

public class My_Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;
  private String type;
  private String size;
  private boolean isColor;

  public My_Image(final String name, final byte[] data, String type, String size, boolean isColor) {
    id = count++;
    this.name = name;
    this.data = data;
    this.type = type;
    this.size = size;
    this.isColor = isColor;
  }

  public My_Image(final String name, final byte[] data, final long myid, String type, String size, boolean isColor) {
    id = myid;
    this.name = name;
    this.data = data;
    this.type = type;
    this.size = size;
    this.isColor = isColor;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }

  public String getType() {
    return type;
  }

  public String getSize() {
    return size;
  }

  public boolean isColor() {
    return isColor;
  }
}
