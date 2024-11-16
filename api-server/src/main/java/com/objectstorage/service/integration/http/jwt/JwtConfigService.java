package com.objectstorage.service.integration.http.jwt;

import com.objectstorage.exception.JwtSecretKeyCreationFailureException;
import io.quarkus.runtime.Startup;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.objectstorage.service.state.StateService;
import lombok.SneakyThrows;

/**
 * Service used to perform diagnostics telemetry configuration operations.
 */
@Startup(value = 400)
@ApplicationScoped
public class JwtConfigService {
    /**
     * Performs ObjectStorage API Server jwt secret token creation.
     */
    @PostConstruct
    @SneakyThrows
    private void process() {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");

        keyGen.init(256);

        String jwtSecretKey = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());

        StateService.setJwtSecretKey(jwtSecretKey);
    }
}
