package ru.jango.j0loader.test;

import java.net.URI;

import ru.jango.j0loader.part.StringPart;

public class Const {

    public static final String BASE = "http://192.168.1.2/j0Loader/";

    public static final URI IMG_SMALL = URI.create(BASE + "small.jpg");
    public static final URI IMG_NORMAL = URI.create(BASE + "normal.jpg");
    public static final URI IMG_LARGE = URI.create(BASE + "large.jpg");
    public static final URI IMG_HUGE = URI.create(BASE + "huge.jpg");
    public static final URI IMG_FAKE = URI.create(BASE + "fake.jpg");


    public static final URI PARAMED_LOADER_TEST_SCRIPT = URI.create(BASE + "ParamedLoader_test.php");
    public static final String PARAM_BMP = "bmp";
    public static final String PARAM_TEST_NAME = "test_name";

    public static final StringPart PART_TEST_SIMPLE_REQUEST = new StringPart(PARAM_TEST_NAME, "simple_request");

    public static final StringPart PART1 = new StringPart("p1", "param1");
    public static final StringPart PART2 = new StringPart("p2", "some param2");
    public static final StringPart PART3 = new StringPart("p3", "*some param3*");

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
