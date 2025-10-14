package com.maternity.config;

import com.maternity.model.*;
import com.maternity.repository.AdminRepository;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.MotherProfileRepository;
import com.maternity.repository.OrderRepository;
import com.maternity.repository.ReviewRepository;
import com.maternity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Profile("dev")
@Component
public class DataInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final MatronProfileRepository matronProfileRepository;
    private final MotherProfileRepository motherProfileRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, MatronProfileRepository matronProfileRepository,
                          MotherProfileRepository motherProfileRepository, OrderRepository orderRepository,
                          ReviewRepository reviewRepository, AdminRepository adminRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.matronProfileRepository = matronProfileRepository;
        this.motherProfileRepository = motherProfileRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing database with sample data...");

        // Create admin users
        createAdmin("admin", "admin123", "admin@maternity.com", "System Administrator", Admin.AdminRole.SUPER_ADMIN);
        createAdmin("manager", "manager123", "manager@maternity.com", "Manager", Admin.AdminRole.ADMIN);

        // Create mother users and profiles
        User mother1 = createUser("Demo Mother", "mother@test.com", "password", User.UserRole.MOTHER, "13800138000");
        createMotherProfile(mother1, LocalDate.now().plusDays(60), null,
            "123 Main St, Beijing", "First time mother, looking for experienced matron");

        User mother2 = createUser("Ms. Liu", "liu@test.com", "password", User.UserRole.MOTHER, "13800138001");
        createMotherProfile(mother2, null, LocalDate.now().minusDays(30),
            "456 Park Ave, Shanghai", "需要有经验的月嫂");

        // Create matron users
        User matronUser1 = createUser("Zhang Wei", "zhang@test.com", "password", User.UserRole.MATRON, "13900139001");
        User matronUser2 = createUser("Li Ming", "li@test.com", "password", User.UserRole.MATRON, "13900139002");
        User matronUser3 = createUser("Wang Fang", "wang@test.com", "password", User.UserRole.MATRON, "13900139003");
        User matronUser4 = createUser("Chen Xiu", "chen@test.com", "password", User.UserRole.MATRON, "13900139004");

        // Create matron profiles
        MatronProfile matron1 = createMatronProfile(
                matronUser1, 35, 8, 12000.0, "Beijing, Chaoyang District",
                "Experienced maternity matron with 8 years of professional experience. Specialized in newborn care and postpartum recovery.",
                Arrays.asList("Newborn Care", "Breastfeeding Support", "Postpartum Meals", "Baby Massage"),
                Arrays.asList("Certified Maternity Nurse", "Pediatric First Aid"),
                4.8, 45, true, null
        );

        MatronProfile matron2 = createMatronProfile(
                matronUser2, 42, 15, 15000.0, "Shanghai, Pudong",
                "Senior maternity matron with extensive experience in twins and premature baby care.",
                Arrays.asList("Twin Care", "Premature Baby Care", "Postpartum Recovery", "Nutrition Planning"),
                Arrays.asList("Senior Maternity Nurse", "Nutrition Specialist", "CPR Certified"),
                4.9, 78, true, null
        );

        MatronProfile matron3 = createMatronProfile(
                matronUser3, 38, 10, 13000.0, "Beijing, Haidian District",
                "Caring and professional maternity matron. Focused on creating a comfortable environment for both mother and baby.",
                Arrays.asList("Newborn Care", "Sleep Training", "Postpartum Meals", "Emotional Support"),
                Arrays.asList("Certified Maternity Nurse", "Sleep Consultant"),
                4.7, 52, false, LocalDate.now().plusDays(15)
        );

        MatronProfile matron4 = createMatronProfile(
                matronUser4, 40, 12, 14000.0, "Shenzhen, Nanshan",
                "Experienced in both traditional and modern childcare methods. Patient and attentive.",
                Arrays.asList("Newborn Care", "Traditional Methods", "Baby Massage", "Hygiene Care"),
                Arrays.asList("Certified Maternity Nurse", "Traditional Chinese Medicine"),
                4.8, 63, true, null
        );

        // Create orders
        createOrder(mother1, matron1, LocalDate.now().plusDays(30), LocalDate.now().plusDays(72),
                12000.0, Order.OrderStatus.CONFIRMED, "123 Main St, Beijing", null);

        createOrder(mother1, matron2, LocalDate.now().minusDays(10), LocalDate.now().plusDays(32),
                15000.0, Order.OrderStatus.IN_PROGRESS, "456 Park Ave, Shanghai", null);

        // Create reviews
        createReview(matron1, mother1, 5.0, "Excellent service! Very professional and caring.");
        createReview(matron1, mother2, 4.5, "Great experience. Highly recommend!");
        createReview(matron2, mother1, 5.0, "Amazing with twins! Very knowledgeable and patient.");

        log.info("Sample data initialized successfully!");
    }

    private Admin createAdmin(String username, String password, String email, String name, Admin.AdminRole role) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setName(name);
        admin.setRole(role);
        admin.setEnabled(true);
        Admin saved = adminRepository.save(admin);
        log.info("Created admin user: {} with role: {}", username, role);
        return saved;
    }

    private User createUser(String name, String email, String password, User.UserRole role, String phone) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    private MatronProfile createMatronProfile(User user, int age, int yearsOfExperience, Double pricePerMonth,
                                               String location, String bio, List<String> skills,
                                               List<String> certifications, Double rating, Integer reviewCount,
                                               Boolean isAvailable, LocalDate availableFrom) {
        MatronProfile profile = new MatronProfile();
        profile.setUser(user);
        profile.setAge(age);
        profile.setYearsOfExperience(yearsOfExperience);
        profile.setPricePerMonth(pricePerMonth);
        profile.setLocation(location);
        profile.setBio(bio);
        profile.setSkills(skills);
        profile.setCertifications(certifications);
        profile.setRating(rating);
        profile.setReviewCount(reviewCount);
        profile.setIsAvailable(isAvailable);
        profile.setAvailableFrom(availableFrom);
        return matronProfileRepository.save(profile);
    }

    private Order createOrder(User mother, MatronProfile matron, LocalDate startDate, LocalDate endDate,
                              Double totalPrice, Order.OrderStatus status, String address, String notes) {
        Order order = new Order();
        order.setMother(mother);
        order.setMatronProfile(matron);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        order.setAddress(address);
        order.setNotes(notes);
        return orderRepository.save(order);
    }

    private MotherProfile createMotherProfile(User user, LocalDate dueDate, LocalDate babyBirthDate,
                                              String address, String specialNeeds) {
        MotherProfile profile = new MotherProfile();
        profile.setUser(user);
        profile.setDueDate(dueDate);
        profile.setBabyBirthDate(babyBirthDate);
        profile.setAddress(address);
        profile.setSpecialNeeds(specialNeeds);
        return motherProfileRepository.save(profile);
    }

    private Review createReview(MatronProfile matron, User user, Double rating, String comment) {
        Review review = new Review();
        review.setMatronProfile(matron);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }
}
