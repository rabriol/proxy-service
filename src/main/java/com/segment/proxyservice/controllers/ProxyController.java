package com.segment.proxyservice.controllers;

import com.segment.proxyservice.exceptions.KeyNotFoundException;
import com.segment.proxyservice.repository.CacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class ProxyController {
    private static Logger log = LoggerFactory.getLogger(ProxyController.class);

    private CacheRepository repository;

    public ProxyController(CacheRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/cache/{key}", produces = "text/plain")
    public ResponseEntity<String> get(@PathVariable("key") String key) {
        return ResponseEntity.ok(repository.get(key));
    }

    @ExceptionHandler({ KeyNotFoundException.class })
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        log.debug(String.format("Msg=%s", ex.getMessage()) );
        return ResponseEntity.notFound().build();
    }
}
