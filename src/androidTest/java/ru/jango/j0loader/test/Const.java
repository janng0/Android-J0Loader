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
        SMALL(IMG_SMALL),
        NORMAL(IMG_NORMAL),
        LARGE(IMG_LARGE),
        HUGE(IMG_HUGE),
        FAKE(IMG_FAKE);

        private URI uri;

        private Img(URI uri) { this.uri = uri; }
        public URI getURI() { return uri; }
    }
}
