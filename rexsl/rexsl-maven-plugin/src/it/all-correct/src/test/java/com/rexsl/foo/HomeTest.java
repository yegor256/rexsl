package com.rexsl.foo;

import com.rexsl.test.JaxbConverter;
import org.junit.*;
import org.xmlmatchers.XmlMatchers;

public class HomeTest {
    @Test
    public void testXmlContents() {
        Home home = new Home();
        Assert.assertThat(
            JaxbConverter.the(home),
            XmlMatchers.hasXPath("/page/text[contains(.,'world')]")
        );
    }
}
