package ru.jango.j0loader.test;

import android.test.AndroidTestCase;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ru.jango.j0loader.Request;
import ru.jango.j0loader.part.DataPart;
import ru.jango.j0loader.part.Part;
import ru.jango.j0loader.part.StringPart;

public class RequestTest extends AndroidTestCase {

    /**
     * 1) create new request without params list and test returns FALSE
     * 2) create simple params list (short StringParts) and check TRUE
     * 3) add DataPart and check FALSE
     * 4) remove BitmapPart and add simple Part, check FALSE
     * 5) remove simple Part and add long StringPart, check FALSE
     */
    public void testCanComposeParams() throws Exception {
        // 1
        final RequestWrapper request = genRequest();
        final List<Part> params = request.getRequestParams();
        request.setRequestParams(null);
        assertFalse(request.canComposeParams2());

        // 2
        request.setRequestParams(params);
        assertTrue(request.canComposeParams2());

        // 3
        params.add(new DataPart("some_name", "some_filename", new byte[5]));
        assertFalse(request.canComposeParams2());
        params.remove(3);

        // 4
        final Part part = new Part();
        part.setName("some_name");
        part.setRawData(new byte[5]);
        params.add(part);

        assertFalse(request.canComposeParams2());
        params.remove(3);

        // 5
        params.add(Const.PART4_LONG);
        assertFalse(request.canComposeParams2());
    }

    /**
     * Simple test - simple parts from Const class will test everything needed.
     */
    public void testGetComposedParams() throws Exception {
        final RequestWrapper request = genRequest();
        assertEquals("p1=param1&p2=some%20param2&p3=*%D1%88%D0%BA%D0%BE%D0%BB%D0%BE%D0%BB%D0%BE%20param3*",
                request.getComposedParams2());
    }

    /**
     * 1) simple test (params generate, uri generates)
     * 2) use Const.PART4_LONG and check params not generated
     * 3) use new StringPart with 230 chars so it will pass, but whole URI will fail length test
     */
    public void testGetComposedURI() throws Exception {
        // 1
        final RequestWrapper request = genRequest();
        assertEquals(URI.create(Const.BASE + "fake.php?p1=param1&p2=some%20param2&p3=*%D1%88%D0%BA%D0%BE%D0%BB%D0%BE%D0%BB%D0%BE%20param3*"),
                request.getComposedURI());

        // 2
        final List<Part> params = request.getRequestParams();
        params.add(Const.PART4_LONG);
        assertEquals(URI.create(Const.BASE + "fake.php"), request.getComposedURI());
        params.remove(3);

        // 3
        params.add(new StringPart("some_name", Const.genLongString(230)));
        assertEquals(URI.create(Const.BASE + "fake.php"), request.getComposedURI());
    }

    /**
     * 1) simple test - 3 short params and GET is allowed
     * 2) use DataPart and check method is POST
     * 2) use Const.PART4_LONG and check method is POST (one param is too long)
     * 3) use new StringPart with 230 chars so it will pass, but whole URI will fail length test
     *      and method will be POST
     */
    public void testGetRecommendedMethod() throws Exception {
        // 1
        final RequestWrapper request = genRequest();
        assertEquals(Request.Method.GET, request.getRecommendedMethod());

        // 3
        final List<Part> params = request.getRequestParams();
        params.add(new DataPart("some_name", "some_filename", new byte[5]));
        assertEquals(Request.Method.POST, request.getRecommendedMethod());
        params.remove(3);

        // 3
        params.add(Const.PART4_LONG);
        assertEquals(Request.Method.POST, request.getRecommendedMethod());
        params.remove(3);

        // 4
        params.add(new StringPart("some_name", Const.genLongString(230)));
        assertEquals(Request.Method.POST, request.getRecommendedMethod());
    }

    private RequestWrapper genRequest() {
        final List<Part> params = new ArrayList<Part>();
        params.add(Const.PART1);
        params.add(Const.PART2);
        params.add(Const.PART3);

        final RequestWrapper request = new RequestWrapper();
        request.setRequestParams(params);
        return request;
    }

    private class RequestWrapper extends Request {
        public RequestWrapper() { super(URI.create(Const.BASE + "fake.php")); }
        public boolean canComposeParams2() { return canComposeParams(); }
        public String getComposedParams2() { return getComposedParams(); }
    }

}
