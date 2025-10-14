package com.maternity.config;

import com.maternity.model.Admin;
import com.maternity.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Production Data Initializer
 *
 * Creates admin user in production environment if it doesn't exist.
 * This initializer is idempotent - safe to run multiple times.
 *
 * Admin credentials are read from environment variables:
 * - ADMIN_USERNAME (default: admin)
 * - ADMIN_PASSWORD (default: admin123)
 */
@Profile("prod")
@Component
public class ProductionDataInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductionDataInitializer.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    public ProductionDataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking production admin user initialization...");

        // Check if admin user already exists (idempotent check)
        if (adminRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user '{}' already exists, skipping creation", adminUsername);
            return;
        }

        // Create admin user
        log.info("Creating admin user '{}'...", adminUsername);
        Admin admin = new Admin();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail("admin@maternity.com");
        admin.setName("System Administrator");
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);

        adminRepository.save(admin);
        log.info("Admin user '{}' created successfully", adminUsername);
    }
}
