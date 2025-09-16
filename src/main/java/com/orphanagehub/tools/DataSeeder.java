package com.orphanagehub.tools;

import com.orphanagehub.dao.*;
import com.orphanagehub.model.*;
import com.orphanagehub.util.PasswordUtil;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Data seeding tool for testing and development.
 * Fixed version that works with existing DAO methods.
 * 
 * @author OrphanageHub Team
 * @version 5.0
 * @since 2025-09-16
 */
public class DataSeeder {
    
    private static final Random random = new Random();
    private static final String ADMIN_PASSWORD = "Admin@2025!";
    private static final String DEFAULT_PASSWORD = "Password123!";
    
    // Sample data arrays
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Lisa",
        "Robert", "Mary", "William", "Patricia", "Thomas", "Jennifer", "Charles"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez"
    };
    
    // Real South African orphanage data
    private static final String[] ORPHANAGE_NAMES = {
        "Hope Children's Home", "Sunshine Shelter", "Angels Haven",
        "Rainbow House", "Little Stars Orphanage", "Safe Harbor Home",
        "Bright Future House", "New Beginnings Center", "Grace House"
    };
    
    private static final String[] CITIES = {
        "Cape Town", "Johannesburg", "Durban", "Pretoria", "Port Elizabeth",
        "Bloemfontein", "East London", "Polokwane", "Nelspruit"
    };
    
    private static final String[] PROVINCES = {
        "Western Cape", "Gauteng", "KwaZulu-Natal", "Eastern Cape",
        "Free State", "Limpopo", "Mpumalanga", "Northern Cape", "North West"
    };
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                 DATABASE SEEDER TOOL v5.0                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose seeding mode:");
        System.out.println("1. Essential Only (Admin + Orphanages + Resources)");
        System.out.println("2. With Sample Users (adds test donors/volunteers)");
        System.out.println("3. Full Seed (everything including sample donations)");
        System.out.print("\nSelect option (1/2/3): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1" -> seedEssential();
            case "2" -> seedWithSampleUsers(false);
            case "3" -> seedWithSampleUsers(true);
            default -> {
                System.out.println("Invalid choice. Cancelled.");
                return;
            }
        }
    }
    
    /**
     * Seeds only essential data - admin and orphanages with their resources
     */
    public static void seedEssential() {
        System.out.println("\n🌱 Seeding ESSENTIAL data only...\n");
        
        try {
            // 1. Create admin (REQUIRED)
            Integer adminId = createOrUpdateAdminUser();
            System.out.println("✓ Admin user ready");
            
            // 2. Get existing orphanages to avoid duplicates
            OrphanageDAO orphanageDAO = new OrphanageDAO();
            List<Integer> existingOrphanageIds = new ArrayList<>();
            
            // Get count of existing orphanages (we'll handle this carefully)
            Try<io.vavr.collection.List<Orphanage>> existingTry = orphanageDAO.findAll();
            int existingCount = 0;
            if (existingTry.isSuccess()) {
                existingCount = existingTry.get().size();
                // Collect existing IDs
                existingTry.get().forEach(o -> existingOrphanageIds.add(o.orphanageId()));
            }
            
            System.out.println("  ℹ Found " + existingCount + " existing orphanages");
            
            // 3. Create orphanages with managers (only if needed)
            int targetCount = 5;
            Integer[] orphanageIds;
            
            if (existingCount < targetCount) {
                int toCreate = targetCount - existingCount;
                Integer[] newIds = createUniqueOrphanages(toCreate, existingCount);
                
                // Combine existing and new IDs
                List<Integer> allIds = new ArrayList<>(existingOrphanageIds);
                for (Integer id : newIds) {
                    if (id != null) allIds.add(id);
                }
                orphanageIds = allIds.toArray(new Integer[0]);
                
                System.out.println("✓ Created " + toCreate + " new orphanages");
            } else {
                // Use existing orphanages
                orphanageIds = existingOrphanageIds.toArray(new Integer[0]);
                System.out.println("  ℹ Using existing orphanages");
            }
            
            // 4. Create resource requests (always safe to add more)
            if (orphanageIds.length > 0) {
                int requestCount = 15;
                createResourceRequests(orphanageIds, requestCount);
                System.out.println("✓ Created " + requestCount + " resource requests");
                
                // 5. Create volunteer opportunities
                int opportunityCount = 10;
                createVolunteerOpportunities(orphanageIds, opportunityCount);
                System.out.println("✓ Created " + opportunityCount + " volunteer opportunities");
            }
            
            printSuccessMessage(false, false);
            
        } catch (Exception e) {
            System.err.println("❌ Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Seeds essential data plus sample users for testing
     */
    public static void seedWithSampleUsers(boolean fullSeed) {
        System.out.println("\n🌱 Seeding database with sample users...\n");
        
        try {
            // First do essential seeding
            seedEssential();
            
            // Then add sample users
            System.out.println("\n📝 Adding sample users...");
            
            // Create sample donors
            int donorCount = fullSeed ? 10 : 3;
            Integer[] donorIds = createDonors(donorCount);
            System.out.println("✓ " + donorCount + " sample donors created");
            
            // Create sample volunteers
            int volunteerCount = fullSeed ? 10 : 3;
            Integer[] volunteerIds = createVolunteers(volunteerCount);
            System.out.println("✓ " + volunteerCount + " sample volunteers created");
            
            // Get orphanage IDs
            OrphanageDAO orphanageDAO = new OrphanageDAO();
            List<Integer> orphanageIdList = new ArrayList<>();
            Try<io.vavr.collection.List<Orphanage>> orphanagesTry = orphanageDAO.findAll();
            if (orphanagesTry.isSuccess()) {
                orphanagesTry.get().forEach(o -> orphanageIdList.add(o.orphanageId()));
            }
            Integer[] orphanageIds = orphanageIdList.toArray(new Integer[0]);
            
            // Create sample donations (only in full seed)
            if (fullSeed && orphanageIds.length > 0 && donorIds.length > 0) {
                int donationCount = 20;
                createDonations(donorIds, orphanageIds, donationCount);
                System.out.println("✓ " + donationCount + " sample donations created");
                
                // Create volunteer applications
                if (volunteerIds.length > 0) {
                    createVolunteerApplications(volunteerIds, orphanageIds);
                    System.out.println("✓ Sample volunteer applications created");
                }
            }
            
            printSuccessMessage(true, fullSeed);
            
        } catch (Exception e) {
            System.err.println("❌ Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Original seed method for backwards compatibility
     */
    public static void seed(boolean fullSeed) {
        if (fullSeed) {
            seedWithSampleUsers(true);
        } else {
            seedEssential();
        }
    }
    
    private static Integer createOrUpdateAdminUser() {
        UserDAO userDAO = new UserDAO();
        
        // Check if admin already exists
        Try<Option<User>> existing = userDAO.findByUsername("admin");
        if (existing.isSuccess() && existing.get().isDefined()) {
            System.out.println("  ℹ Admin user already exists");
            return existing.get().get().userId();
        }
        
        User admin = new User(
            null,
            "admin",
            PasswordUtil.hash(ADMIN_PASSWORD),
            "admin@orphanagehub.org",
            "Admin",
            LocalDateTime.now(),
            Option.none(),
            Option.of("System Administrator"),
            Option.of("0821234567"),
            Option.none(),
            Option.of(LocalDate.of(1990, 1, 1)),
            Option.of("123 Admin Street"),
            Option.of("Cape Town"),
            Option.of("Western Cape"),
            Option.of("8000"),
            "Active",
            true,
            Option.none(),
            Option.none(),
            Option.none(),
            Option.none(),
            Option.of("System administrator account"),
            Option.of("Seeder"),
            Option.none(),
            Option.none()
        );
        
        return userDAO.create(admin).get().userId();
    }
    
    private static Integer[] createDonors(int count) {
        if (count == 0) return new Integer[0];
        
        UserDAO userDAO = new UserDAO();
        Integer[] ids = new Integer[count];
        
        for (int i = 0; i < count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String username = "donor" + (i + 1);
            
            // Check if already exists
            Try<Option<User>> existing = userDAO.findByUsername(username);
            if (existing.isSuccess() && existing.get().isDefined()) {
                ids[i] = existing.get().get().userId();
                continue;
            }
            
            User donor = new User(
                null,
                username,
                PasswordUtil.hash(DEFAULT_PASSWORD),
                username + "@example.com",
                "Donor",
                LocalDateTime.now().minusDays(random.nextInt(365)),
                Option.none(),
                Option.of(firstName + " " + lastName),
                Option.of("08" + (20000000 + random.nextInt(80000000))),
                Option.none(),
                Option.of(LocalDate.now().minusYears(20 + random.nextInt(40))),
                Option.of(random.nextInt(999) + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " Street"),
                Option.of(CITIES[random.nextInt(CITIES.length)]),
                Option.of(PROVINCES[random.nextInt(PROVINCES.length)]),
                Option.of(String.valueOf(1000 + random.nextInt(9000))),
                "Active",
                true,
                Option.none(),
                Option.none(),
                Option.none(),
                Option.none(),
                Option.of("Sample donor for testing"),
                Option.of("Seeder"),
                Option.none(),
                Option.none()
            );
            
            ids[i] = userDAO.create(donor).get().userId();
        }
        
        return ids;
    }
    
    private static Integer[] createVolunteers(int count) {
        if (count == 0) return new Integer[0];
        
        UserDAO userDAO = new UserDAO();
        Integer[] ids = new Integer[count];
        
        for (int i = 0; i < count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String username = "volunteer" + (i + 1);
            
            // Check if already exists
            Try<Option<User>> existing = userDAO.findByUsername(username);
            if (existing.isSuccess() && existing.get().isDefined()) {
                ids[i] = existing.get().get().userId();
                continue;
            }
            
            User volunteer = new User(
                null,
                username,
                PasswordUtil.hash(DEFAULT_PASSWORD),
                username + "@example.com",
                "Volunteer",
                LocalDateTime.now().minusDays(random.nextInt(365)),
                Option.none(),
                Option.of(firstName + " " + lastName),
                Option.of("08" + (20000000 + random.nextInt(80000000))),
                Option.none(),
                Option.of(LocalDate.now().minusYears(18 + random.nextInt(30))),
                Option.of(random.nextInt(999) + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " Street"),
                Option.of(CITIES[random.nextInt(CITIES.length)]),
                Option.of(PROVINCES[random.nextInt(PROVINCES.length)]),
                Option.of(String.valueOf(1000 + random.nextInt(9000))),
                "Active",
                true,
                Option.none(),
                Option.none(),
                Option.none(),
                Option.none(),
                Option.of("Sample volunteer for testing"),
                Option.of("Seeder"),
                Option.none(),
                Option.none()
            );
            
            ids[i] = userDAO.create(volunteer).get().userId();
        }
        
        return ids;
    }
    
    /**
     * Create orphanages with unique registration numbers
     */
    private static Integer[] createUniqueOrphanages(int count, int startIndex) {
        UserDAO userDAO = new UserDAO();
        OrphanageDAO orphanageDAO = new OrphanageDAO();
        List<Integer> successfulIds = new ArrayList<>();
        
        int attempts = 0;
        int created = 0;
        
        while (created < count && attempts < count * 3) {
            attempts++;
            int index = startIndex + created;
            
            try {
                // Create staff user for this orphanage
                String orphanageName = ORPHANAGE_NAMES[index % ORPHANAGE_NAMES.length];
                if (index >= ORPHANAGE_NAMES.length) {
                    orphanageName += " " + (index / ORPHANAGE_NAMES.length + 1);
                }
                
                String username = "staff" + (index + 1);
                
                // Check if staff already exists
                Try<Option<User>> existingStaff = userDAO.findByUsername(username);
                Integer staffId;
                
                if (existingStaff.isSuccess() && existingStaff.get().isDefined()) {
                    staffId = existingStaff.get().get().userId();
                } else {
                    User staff = new User(
                        null,
                        username,
                        PasswordUtil.hash(DEFAULT_PASSWORD),
                        username + "@orphanage.org",
                        "OrphanageRep",
                        LocalDateTime.now().minusDays(random.nextInt(365)),
                        Option.none(),
                        Option.of("Staff Member " + (index + 1)),
                        Option.of("08" + (30000000 + random.nextInt(70000000))),
                        Option.none(),
                        Option.of(LocalDate.now().minusYears(25 + random.nextInt(20))),
                        Option.of(random.nextInt(999) + " Staff Street"),
                        Option.of(CITIES[index % CITIES.length]),
                        Option.of(PROVINCES[index % PROVINCES.length]),
                        Option.of(String.valueOf(1000 + random.nextInt(9000))),
                        "Active",
                        true,
                        Option.none(),
                        Option.none(),
                        Option.none(),
                        Option.none(),
                        Option.of("Orphanage staff account"),
                        Option.of("Seeder"),
                        Option.none(),
                        Option.none()
                    );
                    
                    Try<User> staffResult = userDAO.create(staff);
                    if (staffResult.isFailure()) {
                        System.err.println("Failed to create staff user: " + staffResult.getCause().getMessage());
                        continue;
                    }
                    staffId = staffResult.get().userId();
                }
                
                // Create orphanage with truly unique registration number
                String city = CITIES[index % CITIES.length];
                String province = getProvinceForCity(city);
                
                // Generate unique registration using timestamp + random + index
                long timestamp = System.currentTimeMillis();
                int randomNum = random.nextInt(99999);
                String registrationNumber = String.format("NPO-%d-%05d-%d", timestamp, randomNum, index);
                String taxNumber = String.format("PBO-%d-%05d", timestamp, randomNum);
                
                Orphanage orphanage = new Orphanage(
                    null,
                    Option.of(orphanageName),
                    Option.of(registrationNumber),
                    Option.of(taxNumber),
                    (100 + index) + " Care Street",
                    city,
                    province,
                    Option.of(String.valueOf(1000 + random.nextInt(9000))),
                    orphanageName + " Director",
                    "contact_" + registrationNumber.substring(4, 10) + "@orphanage.org",
                    "08" + (30000000 + random.nextInt(70000000)),
                    Option.none(),
                    Option.of("www." + orphanageName.toLowerCase().replace(" ", "").replace("'", "") + ".org.za"),
                    Option.of("Providing comprehensive care and shelter to orphaned children in " + city),
                    Option.of("To create a nurturing environment where every child can thrive"),
                    Option.of("A world where every child has a loving family"),
                    Option.of(LocalDate.now().minusYears(5 + random.nextInt(15))),
                    Option.of(30 + random.nextInt(70)),
                    Option.of(20 + random.nextInt(30)),
                    Option.of(0),
                    Option.of(18),
                    true,
                    true,
                    Option.of(index % 2 == 0 ? "Standard Bank" : "FNB"),
                    Option.of(String.valueOf(1000000000L + timestamp % 1000000)),
                    Option.of(index % 2 == 0 ? "051001" : "250655"),
                    LocalDateTime.now(),
                    created < 3 ? "Verified" : "Pending",
                    created < 3 ? Option.of(LocalDateTime.now()) : Option.none(),
                    Option.none(),
                    Option.none(),
                    staffId,
                    "Active",
                    Option.none(),
                    Option.none(),
                    Option.none(),
                    Option.none(),
                    Option.none(),
                    Option.none()
                );
                
                Try<Orphanage> result = orphanageDAO.create(orphanage);
                if (result.isSuccess()) {
                    successfulIds.add(result.get().orphanageId());
                    created++;
                    
                    // Small delay to ensure unique timestamps
                    Thread.sleep(10);
                } else {
                    System.err.println("Failed to create orphanage: " + result.getCause().getMessage());
                }
                
            } catch (Exception e) {
                System.err.println("Error creating orphanage: " + e.getMessage());
            }
        }
        
        return successfulIds.toArray(new Integer[0]);
    }
    
    /**
     * Backwards compatibility method
     */
    private static Integer[] createOrphanages(int count) {
        return createUniqueOrphanages(count, 0);
    }
    
    private static void createResourceRequests(Integer[] orphanageIds, int count) {
        if (orphanageIds.length == 0) return;
        
        ResourceRequestDAO requestDAO = new ResourceRequestDAO();
        
        String[] resourceTypes = {"Food", "Clothing", "Educational", "Medical", "Furniture"};
        String[] urgencyLevels = {"Critical", "High", "Medium", "Low"};
        
        for (int i = 0; i < count; i++) {
            Integer orphanageId = orphanageIds[i % orphanageIds.length];
            String resourceType = resourceTypes[i % resourceTypes.length];
            
            ResourceRequest request = new ResourceRequest(
                null,
                orphanageId,
                resourceType,
                getResourceDescription(resourceType),
                10.0 + random.nextInt(90),
                Option.of(getUnit(resourceType)),
                urgencyLevels[random.nextInt(urgencyLevels.length)],
                LocalDateTime.now().minusDays(random.nextInt(30)),
                Option.of(LocalDate.now().plusDays(7 + random.nextInt(60))),
                random.nextBoolean() ? "Open" : "In Progress",
                Option.none(),
                Option.none(),
                Option.none(),
                Option.of(100.0 + random.nextInt(5000)),
                Option.none(),
                Option.none(),
                Option.none(),
                1, // Created by admin
                Option.none(),
                Option.none()
            );
            
            Try<ResourceRequest> result = requestDAO.create(request);
            if (result.isFailure()) {
                System.err.println("Failed to create resource request: " + result.getCause().getMessage());
            }
        }
    }
    
    private static void createDonations(Integer[] donorIds, Integer[] orphanageIds, int count) {
        if (donorIds.length == 0 || orphanageIds.length == 0) return;
        
        DonationDAO donationDAO = new DonationDAO();
        
        for (int i = 0; i < count; i++) {
            Integer donorId = donorIds[random.nextInt(donorIds.length)];
            Integer orphanageId = orphanageIds[random.nextInt(orphanageIds.length)];
            
            try {
                if (random.nextBoolean()) {
                    // Monetary donation
                    Donation donation = Donation.createMonetary(
                        donorId,
                        orphanageId,
                        100.0 + random.nextInt(5000),
                        "Credit Card"
                    );
                    donationDAO.create(donation);
                } else {
                    // Item donation
                    String[] types = {"Food", "Clothing", "Toys", "Books"};
                    String type = types[random.nextInt(types.length)];
                    
                    Donation donation = Donation.createItem(
                        donorId,
                        orphanageId,
                        type,
                        getItemDescription(type),
                        1.0 + random.nextInt(50),
                        "items"
                    );
                    donationDAO.create(donation);
                }
            } catch (Exception e) {
                // Continue on error
            }
        }
    }
    
    private static void createVolunteerOpportunities(Integer[] orphanageIds, int count) {
        if (orphanageIds.length == 0) return;
        
        VolunteerOpportunityDAO opportunityDAO = new VolunteerOpportunityDAO();
        
        String[] categories = {"Teaching", "Sports", "Arts", "Healthcare", "Maintenance"};
        String[] titles = {
            "Math Tutor", "Soccer Coach", "Art Teacher", "Nurse Assistant", "Garden Helper"
        };
        
        for (int i = 0; i < count; i++) {
            Integer orphanageId = orphanageIds[i % orphanageIds.length];
            int catIndex = i % categories.length;
            
            // Make title unique
            String uniqueTitle = titles[catIndex] + " Needed (#" + System.currentTimeMillis() % 10000 + ")";
            
            VolunteerOpportunity opportunity = VolunteerOpportunity.createBasic(
                orphanageId,
                uniqueTitle,
                "Help us with " + categories[catIndex].toLowerCase() + " activities",
                categories[catIndex],
                1 // Created by admin
            );
            
            Try<Void> result = opportunityDAO.create(opportunity);
            if (result.isFailure()) {
                System.err.println("Failed to create volunteer opportunity: " + result.getCause().getMessage());
            }
            
            try {
                Thread.sleep(5); // Small delay for uniqueness
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
    
    private static void createVolunteerApplications(Integer[] volunteerIds, Integer[] orphanageIds) {
        // Can be implemented if needed
    }
    
    private static void printSuccessMessage(boolean withSampleUsers, boolean fullSeed) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              ✅ DATABASE SEEDING SUCCESSFUL!                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        System.out.println("\n📝 LOGIN CREDENTIALS:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("ADMIN:");
        System.out.println("  Username: admin");
        System.out.println("  Password: " + ADMIN_PASSWORD);
        
        System.out.println("\nORPHANAGE STAFF:");
        System.out.println("  Username: staff1, staff2, staff3...");
        System.out.println("  Password: " + DEFAULT_PASSWORD);
        
        if (withSampleUsers) {
            System.out.println("\nSAMPLE USERS (for testing):");
            System.out.println("  Donors: donor1, donor2, donor3...");
            System.out.println("  Volunteers: volunteer1, volunteer2, volunteer3...");
            System.out.println("  Password: " + DEFAULT_PASSWORD);
        }
        
        System.out.println("\n💡 Ready to run: make run");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    // Helper methods
    private static String getProvinceForCity(String city) {
        return switch (city) {
            case "Cape Town" -> "Western Cape";
            case "Johannesburg", "Pretoria" -> "Gauteng";
            case "Durban" -> "KwaZulu-Natal";
            case "Port Elizabeth", "East London" -> "Eastern Cape";
            case "Bloemfontein" -> "Free State";
            case "Polokwane" -> "Limpopo";
            case "Nelspruit" -> "Mpumalanga";
            default -> "Gauteng";
        };
    }
    
    private static String getResourceDescription(String type) {
        return switch (type) {
            case "Food" -> "Non-perishable food items and fresh produce";
            case "Clothing" -> "Children's clothing in good condition";
            case "Educational" -> "School supplies and textbooks";
            case "Medical" -> "First aid supplies and medications";
            case "Furniture" -> "Beds, desks, and chairs for children";
            default -> "General supplies needed";
        };
    }
    
    private static String getUnit(String type) {
        return switch (type) {
            case "Food" -> "kg";
            case "Clothing" -> "items";
            case "Educational" -> "sets";
            case "Medical" -> "boxes";
            case "Furniture" -> "pieces";
            default -> "units";
        };
    }
    
    private static String getItemDescription(String type) {
        return switch (type) {
            case "Food" -> "Canned goods and dry foods";
            case "Clothing" -> "Winter jackets and shoes";
            case "Toys" -> "Educational toys and games";
            case "Books" -> "Children's story books";
            default -> "Various items";
        };
    }
}