package ru.jango.j0loader.test;

import java.net.URI;

import ru.jango.j0loader.param.StringParam;

public class Const {

    public static final String BASE = "http://192.168.1.3/j0Loader/";

    public static final URI IMG_SMALL = URI.create(BASE + "small.jpg");
    public static final URI IMG_NORMAL = URI.create(BASE + "normal.jpg");
    public static final URI IMG_LARGE = URI.create(BASE + "large.jpg");
    public static final URI IMG_HUGE = URI.create(BASE + "huge.jpg");
    public static final URI IMG_FAKE = URI.create(BASE + "fake.jpg");

    public static final URI PARAMED_LOADER_TEST_SCRIPT = URI.create(BASE + "ParamedLoader_test.php");
    public static final URI JSON_LOADER_TEST_SCRIPT = URI.create(BASE + "JSONLoader_test.php");

    public static final String PARAM_TEST_NAME = "test_name";
    public static final StringParam PARAM_SHORT_TEXT_REQUEST = new StringParam(PARAM_TEST_NAME, "short_text_request");
    public static final StringParam PARAM_LONG_TEXT_REQUEST = new StringParam(PARAM_TEST_NAME, "long_text_request");
    public static final StringParam PARAM_BITMAP_REQUEST = new StringParam(PARAM_TEST_NAME, "bitmap_request");
    public static final StringParam PARAM_MIXED_REQUEST = new StringParam(PARAM_TEST_NAME, "mixed_request");

    public static final StringParam PART1 = new StringParam("p1", "param1");
    public static final StringParam PART2 = new StringParam("p2", "some param2");
    public static final StringParam PART3 = new StringParam("p3", "*школоло param3*");
    public static final StringParam PART4_LONG = new StringParam("p4_long", genLongString(255));

    public static String genLongString(int len) {
        final StringBuilder sb = new StringBuilder();
        final byte[] tmp = new byte[1];
        for (int i=0; i<len; i++) {
            tmp[0] = (byte) (Math.random()*255);
            sb.append(new String(tmp));
        }

        return sb.toString();
    }

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
