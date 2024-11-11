package com.objectstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Represents entry point for the ObjectStorage CLI application.
 */
@SpringBootApplication
public class CLI {
  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(App.class);

    System.exit(SpringApplication.exit(application.run(args)));
  }
}
