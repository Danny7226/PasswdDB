package org.secretdb.module;

import dagger.Component;
import jakarta.inject.Singleton;
import org.secretdb.servlet.http.ListServlet;
import org.secretdb.servlet.http.ReadWriteServlet;

@Singleton
@Component(modules = {
        DaggerModule.class
})
public interface SecretDBComponent {
    void inject(ListServlet listServlet);
    void inject(ReadWriteServlet readWriteServlet);
}
