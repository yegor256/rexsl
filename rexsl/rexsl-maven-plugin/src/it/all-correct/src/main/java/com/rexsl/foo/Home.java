package com.rexsl.foo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class Home {
  @XmlElement(name = "text")
  public String getText() {
    return "Hello, world!";
  }
}
