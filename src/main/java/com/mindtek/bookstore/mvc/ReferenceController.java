package com.mindtek.bookstore.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reference")
public class ReferenceController {

  @GetMapping
  public String index() {
    return "reference/index";
  }

  @GetMapping("/rest-basics")
  public String restBasics() {
    return "reference/rest-basics";
  }

  @GetMapping("/this-api")
  public String thisApi() {
    return "reference/this-api";
  }

  @GetMapping("/idempotency")
  public String idempotency() {
    return "reference/idempotency";
  }

  @GetMapping("/contract")
  public String contract() {
    return "reference/contract";
  }

  @GetMapping("/ci-cd")
  public String ciCd() {
    return "reference/ci-cd";
  }
}
