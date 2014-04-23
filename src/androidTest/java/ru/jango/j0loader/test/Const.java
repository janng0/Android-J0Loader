package ru.jango.j0loader.test;

import java.net.URI;

public class Const {

    public static final String BASE = "http://192.168.1.2/j0Loader/";

    public static final URI IMG_SMALL = URI.create(BASE + "small.jpg");
    public static final URI IMG_NORMAL = URI.create(BASE + "normal.jpg");
    public static final URI IMG_LARGE = URI.create(BASE + "large.jpg");
    public static final URI IMG_HUGE = URI.create(BASE + "huge.jpg");
    public static final URI IMG_FAKE = URI.create(BASE + "fake.jpg");

    public static final URI UPLOAD_SCRIPT = URI.create(BASE + "upload.php");
    public static final URI JSON_SCRIPT = URI.create(BASE + "json_load.php");

    public enum Img {
        SMALL(IMG_SMALL, 425, 554, 80492),
        NORMAL(IMG_NORMAL, 1600, 1000, 483849),
        LARGE(IMG_LARGE, 2048, 1122, 489453), // default dimens 2738x1500
        HUGE(IMG_HUGE, 2048, 833, 1343899), // default dimens 6150x2500
        FAKE(IMG_FAKE, -1, -1, -1);

        private URI uri;
        private int width;
        private int height;
        private long size;

        private Img(URI uri, int width, int height, long size) {
            this.uri = uri;
            this.width = width;
            this.height = height;
            this.size = size;
        }

        public URI getURI() { return uri; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSize() { return size; }
    }
}
