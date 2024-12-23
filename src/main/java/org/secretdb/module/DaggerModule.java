package org.secretdb.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.secretdb.dao.SecretDBFactory;

@Module
public class DaggerModule {

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    public SecretDBFactory provideSecretDBFactory(final ObjectMapper objectMapper) {
        return new SecretDBFactory(objectMapper);
    }
}
